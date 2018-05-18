package gr.cite.femme.fulltext.engine.semantic.search.taxonomy;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.semanticweb.skos.SKOSCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

public class TaxonomyParserJena implements TaxonomyParser {
	private static final Logger logger = LoggerFactory.getLogger(TaxonomyParserSkosApi.class);
	
	private URI taxonomyUri;
	
	public TaxonomyParserJena(URI taxonomyUri) {
		this.taxonomyUri = taxonomyUri;
	}
	
	public List<SkosConcept> parse() {
		// Create a model and read into it from file
		// "data.ttl" assumed to be Turtle.
		parseModel();
		
		// Create a dataset and read into it from file
		// "data.trig" assumed to be TriG.
		//parseDataset();
		
		// Read into an existing Model
		//RDFDataMgr.read(model, "data2.ttl") ;
		
		return null;
	}
	
	private void parseModel() {
		Model model = RDFDataMgr.loadModel(this.taxonomyUri.toString(), RDFLanguages.RDFXML) ;
		//model.write(System.out);
		ResIterator iterator = model.listSubjects();
		while (iterator.hasNext()) {
			Resource node = iterator.next();
			System.out.println(node.toString());
		}
	}
	
	private void parseDataset() {
		Dataset dataset = RDFDataMgr.loadDataset(this.taxonomyUri.toString(), RDFLanguages.RDFXML);
		//model.write(System.out);
		Iterator<String> iterator = dataset.listNames();
		while (iterator.hasNext()) {
			String str = iterator.next();
			System.out.println(str);
		}
	}
	
	public static void main(String[] args) throws URISyntaxException {
		TaxonomyParserJena parser = new TaxonomyParserJena(new URI("https://vocab.nerc.ac.uk/collection/B12/current/"));
		//Map<URI, SkosConcept> concepts = parser.parse();
		List<SkosConcept> concepts = parser.parse();
		System.out.println(concepts);
		
		//System.out.println(concepts.get(new URI("http://zbw.eu/stw/descriptor/18861-0")));
	}
}
