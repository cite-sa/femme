package gr.cite.femme.engine.metadata.xpath;

import gr.cite.commons.converter.XmlJsonConverter;
import gr.cite.commons.metadata.analyzer.json.JSONSchemaAnalyzer;
import gr.cite.commons.utils.hash.HashGenerationException;
import gr.cite.femme.core.exceptions.MetadataIndexException;
import gr.cite.femme.core.model.Metadatum;
import gr.cite.femme.engine.metadata.xpath.core.IndexableMetadatum;
import gr.cite.femme.engine.metadata.xpath.core.MetadataSchema;
import gr.cite.femme.engine.metadata.xpath.datastores.api.MetadataIndexDatastore;
import gr.cite.femme.engine.metadata.xpath.datastores.api.MetadataSchemaIndexDatastore;
import gr.cite.femme.engine.metadata.xpath.elasticsearch.ElasticMetadataIndexDatastore;
import gr.cite.femme.engine.metadata.xpath.elasticsearch.utils.QueryNode;
import gr.cite.femme.engine.metadata.xpath.elasticsearch.utils.Tree;
import gr.cite.femme.engine.metadata.xpath.grammar.XPathLexer;
import gr.cite.femme.engine.metadata.xpath.grammar.XPathParser;
import gr.cite.femme.engine.metadata.xpath.mongodb.MongoMetadataSchemaIndexDatastore;
import gr.cite.femme.engine.metadata.xpath.parser.visitors.MongoXPathVisitor;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MetadataXPath {
	
	private static final Logger logger = LoggerFactory.getLogger(MetadataXPath.class);
	
	private MetadataIndexDatastore metadataIndexDatastore;
	private MetadataSchemaIndexDatastore metadataSchemaIndexDatastore;
	private ReIndexingProcess reIndexingProcess;
	
	private boolean reIndexingInProgress = false;
	//private ReIndexingProcess reIndexer;
	
	public MetadataXPath() {
		this.metadataSchemaIndexDatastore = new MongoMetadataSchemaIndexDatastore();
		try {
			this.metadataIndexDatastore = new ElasticMetadataIndexDatastore(this.metadataSchemaIndexDatastore);
		} catch (UnknownHostException | MetadataIndexException e) {
			logger.error("Metadata insert datastore initialization failed", e);
		}
		this.reIndexingProcess = this.metadataIndexDatastore.retrieveReIndexer(this.metadataSchemaIndexDatastore);
	}
	
	public MetadataXPath(MetadataSchemaIndexDatastore metadataSchemaIndexDatastore, MetadataIndexDatastore metadataIndexDatastore) {
		this.metadataSchemaIndexDatastore = metadataSchemaIndexDatastore;
		this.metadataIndexDatastore = metadataIndexDatastore;
		this.reIndexingProcess = this.metadataIndexDatastore.retrieveReIndexer(this.metadataSchemaIndexDatastore);
	}
	
	public void close() throws IOException {
		this.metadataIndexDatastore.close();
		this.metadataSchemaIndexDatastore.close();
	}
	
	public void index(Metadatum metadatum) throws UnsupportedOperationException, MetadataIndexException {
		String metadatumJson;
		if (MediaType.APPLICATION_XML.equals(metadatum.getContentType()) || MediaType.TEXT_XML.equals(metadatum.getContentType())) {
			try {
				metadatumJson = XmlJsonConverter.xmlToFemmeJson(metadatum.getValue());
			} catch (XMLStreamException e) {
				throw new MetadataIndexException(e.getMessage(), e);
			}
		} else if (metadatum.getContentType().toLowerCase().contains("json")) {
			try {
				metadatumJson = XmlJsonConverter.jsonToFemmeJson(metadatum.getValue());
			} catch (IOException e) {
				throw new MetadataIndexException(e.getMessage(), e);
			}
		} else {
			throw new UnsupportedOperationException("Metadata indexing is not yet supported for media type [" + metadatum.getContentType() + "]");
		}
		
		MetadataSchema metadataSchema;
		try {
			metadataSchema = new MetadataSchema(JSONSchemaAnalyzer.analyze(metadatumJson));
		} catch (HashGenerationException | IOException e) {
			throw new MetadataIndexException("Metadata schema analysis failed", e);
		}
		this.metadataSchemaIndexDatastore.index(metadataSchema);
		
		IndexableMetadatum indexableMetadatum = new IndexableMetadatum();
		indexableMetadatum.setMetadataSchemaId(metadataSchema.getId());
		indexableMetadatum.setMetadatumId(metadatum.getId());
		indexableMetadatum.setElementId(metadatum.getElementId());
		indexableMetadatum.setOriginalContentType(metadatum.getContentType());
		indexableMetadatum.setValue(metadatumJson);
		indexableMetadatum.setCreated(metadatum.getSystemicMetadata().getCreated());
		indexableMetadatum.setModified(metadatum.getSystemicMetadata().getModified());
		
		this.metadataIndexDatastore.insert(indexableMetadatum, metadataSchema);
		if (this.reIndexingProcess.reIndexingInProgress()) {
			this.reIndexingProcess.index(metadatum);
		}
	}
	
	public void deIndex(String metadatumId) throws UnsupportedOperationException, MetadataIndexException {
		this.metadataIndexDatastore.delete(metadatumId);
	}
	
	public void deIndexAll(String elementId) throws UnsupportedOperationException, MetadataIndexException {
		this.metadataIndexDatastore.deleteByElementId(elementId);
	}
	
	public ReIndexingProcess beginReIndexing() throws MetadataIndexException {
		if (! this.reIndexingProcess.reIndexingInProgress()) {
			this.reIndexingProcess.begin();
			
			return this.reIndexingProcess;
		} else {
			throw new MetadataIndexException("Reindexing already in progress");
		}
	}
	
	public void endReIndexing() throws MetadataIndexException {
		if (this.reIndexingProcess.reIndexingInProgress()) {
			this.reIndexingProcess.end();
		} else {
			throw new MetadataIndexException("No reindexing in progress");
		}
	}
	
	public List<Metadatum> xPath(String xPath) throws MetadataIndexException {
		return xPath(Collections.emptyList(), xPath, true);
	}
	
	public List<Metadatum> xPath(List<String> elementIds, String xPath, boolean lazyPayload) throws MetadataIndexException {
		long start, end;
		
		ParseTree tree = buildParseTree(xPath);
		
		start = System.currentTimeMillis();
		Tree<QueryNode> queryTree = translateXPath(tree);
		end = System.currentTimeMillis();
		logger.info("[" + xPath + "] Query parse time: " + (end - start) + " ms");
		
		start = System.currentTimeMillis();
		List<IndexableMetadatum> xPathResult = this.metadataIndexDatastore.query(elementIds, queryTree, lazyPayload);
		end = System.currentTimeMillis();
		logger.info("[" + xPath + "] ElasticSearch query time: " + (end - start) + " ms");
		
		start = System.currentTimeMillis();
		List<Metadatum> metadata = transformXPathResultToMetadata(xPathResult);
		end = System.currentTimeMillis();
		logger.info("[" + xPath + "] IndexableMetadatum to Metadatum transformation time: " + (end - start) + " ms");
		
		return metadata;
	}
	
	private XPathParser.XpathContext buildParseTree(String xPath) {
		CharStream stream = CharStreams.fromString(xPath);
		XPathLexer lexer = new XPathLexer(stream);
		XPathParser parser = new XPathParser(new CommonTokenStream(lexer));
		return parser.xpath();
	}
	
	private Tree<QueryNode> translateXPath(ParseTree tree) {
		MongoXPathVisitor visitor = new MongoXPathVisitor(metadataSchemaIndexDatastore);
		return visitor.visit(tree);
	}
	
	private List<Metadatum> transformXPathResultToMetadata(List<IndexableMetadatum> xPathResult) {
		return xPathResult.stream().map(indexableMetadatum -> {
			Metadatum metadatum = new Metadatum();
			
			metadatum.setId(indexableMetadatum.getMetadatumId());
			metadatum.setElementId(indexableMetadatum.getElementId());
			metadatum.setContentType(indexableMetadatum.getOriginalContentType());
			
			
			metadatum.setValue(indexableMetadatum.getValue());
			
			return metadatum;
		}).collect(Collectors.toList());
	}
}
