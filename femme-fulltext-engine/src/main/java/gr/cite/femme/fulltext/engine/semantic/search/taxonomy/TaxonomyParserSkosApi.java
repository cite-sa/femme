package gr.cite.femme.fulltext.engine.semantic.search.taxonomy;

import com.google.common.io.Resources;
import org.semanticweb.skos.SKOSAnnotationProperty;
import org.semanticweb.skos.SKOSConcept;
import org.semanticweb.skos.SKOSConceptScheme;
import org.semanticweb.skos.SKOSCreationException;
import org.semanticweb.skos.SKOSDataset;
import org.semanticweb.skos.SKOSLiteral;
import org.semanticweb.skos.SKOSObjectProperty;
import org.semanticweb.skosapibinding.SKOSManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TaxonomyParserSkosApi implements TaxonomyParser{
	private static final Logger logger = LoggerFactory.getLogger(TaxonomyParserSkosApi.class);
	
	private URI taxonomyUri;
	private final SKOSManager manager = new SKOSManager();
	private SKOSDataset dataset;
	
	public TaxonomyParserSkosApi(URI taxonomyUri) throws SKOSCreationException {
		this.taxonomyUri = taxonomyUri;
		this.dataset = this.manager.loadDataset(this.taxonomyUri);
	}
	
	public List<SkosConcept> parse() {
		return this.dataset.getSKOSConcepts().stream().map(this::getSkosConcept).collect(Collectors.toList());
		//return this.dataset.getSKOSConceptSchemes().stream().map(this::getConceptsInScheme).flatMap(Collection::stream).collect(Collectors.toList());
		
		/*System.out.println("");
		System.out.println("---------------------");
		System.out.println("");
		System.out.println("Ontology loaded!");
		for (SKOSConceptScheme scheme : dataset.getSKOSConceptSchemes()) {
			System.out.println("ConceptScheme: " + scheme.getURI());
			// i can get all the concepts from this scheme
			for (SKOSConcept conceptsInScheme : dataset.getSKOSConcepts()) {
				System.err.println("\tConcepts: " + conceptsInScheme.getURI());
				for (SKOSAnnotation anno : conceptsInScheme.getSKOSAnnotations(dataset)) {
					System.err.print("\t\tAnnotation: " + anno.getURI() + "-> ");
					if (anno.isAnnotationByConstant()) {
						if (anno.getAnnotationValueAsConstant().isTyped()) {
							SKOSTypedLiteral con = anno.getAnnotationValueAsConstant().getAsSKOSTypedLiteral();
							System.err.print(con.getLiteral() + " Type: " + con.getDataType().getURI());
						} else {
							SKOSUntypedLiteral con = anno.getAnnotationValueAsConstant().getAsSKOSUntypedLiteral();
							System.err.print(con.getLiteral());
							if (con.hasLang()) {
								System.err.print("@" + con.getLang());
							}
						}
						System.err.println("");
					} else {
						System.err.println(anno.getAnnotationValue().getURI().toString());
					}
				}
			}
		}*/
	}
	
	private List<SkosConcept> getConceptsInScheme(SKOSConceptScheme scheme) {
		return scheme.getConceptsInScheme(this.dataset).stream().map(concept -> {
			SkosConcept skosConcept = getSkosConcept(concept);
			skosConcept.setConceptScheme(scheme.getURI());
			return skosConcept;
		}).collect(Collectors.toList());
	}
	
	private SkosConcept getSkosConcept(SKOSConcept concept) {
		SkosConcept skosConcept = new SkosConcept();
		
		skosConcept.setUri(concept.getURI());
		skosConcept.setTaxonomyUri(this.taxonomyUri);
		
		try {
			skosConcept.setPrefLabel(getConstantsInRelation(concept, this.manager.getSKOSDataFactory().getSKOSPrefLabelProperty()));
			skosConcept.setAltLabel(getConstantsInRelation(concept, this.manager.getSKOSDataFactory().getSKOSAltLabelProperty()));
		} catch (NullPointerException e) {
			logger.warn(e.getMessage(), e);
		}
		
		skosConcept.setNarrower(getConceptsInRelation(concept, this.manager.getSKOSDataFactory().getSKOSNarrowerProperty()));
		skosConcept.setBroader(getConceptsInRelation(concept, this.manager.getSKOSDataFactory().getSKOSBroaderProperty()));
		skosConcept.setRelated(getConceptsInRelation(concept, this.manager.getSKOSDataFactory().getSKOSRelatedProperty()));
		
		return skosConcept;
	}
	
	private List<String> getConstantsInRelation(SKOSConcept concept, SKOSAnnotationProperty property) {
		return concept.getSKOSRelatedConstantByProperty(dataset, property).stream()
			.map(SKOSLiteral::getLiteral).collect(Collectors.toList());
	}
	
	private List<URI> getConceptsInRelation(SKOSConcept concept, SKOSObjectProperty property) {
		return concept.getSKOSRelatedEntitiesByProperty(dataset, property).stream()
				   .map(skosEntity -> skosEntity.isSKOSConcept() ? skosEntity.getURI() : null).filter(Objects::nonNull)
				   .collect(Collectors.toList());
	}
	
	public static void main(String[] args) throws SKOSCreationException, URISyntaxException, UnknownHostException {
		TaxonomyParserSkosApi parser = new TaxonomyParserSkosApi(Resources.getResource("stw.ttl").toURI());
		//TaxonomyParserSkosApi parser = new TaxonomyParserSkosApi(new URI("http://vocab.nerc.ac.uk/collection/A02/current/"));
		List<SkosConcept> concepts = parser.parse();
		System.out.println(concepts);
	}
}
