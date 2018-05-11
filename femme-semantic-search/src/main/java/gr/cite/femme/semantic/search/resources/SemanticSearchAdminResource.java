package gr.cite.femme.semantic.search.resources;

import com.google.common.io.Resources;
import gr.cite.femme.semantic.search.SemanticSearchException;
import gr.cite.femme.semantic.search.model.SemanticDocument;
//import gr.cite.femme.semantic.search.repository.ElasticsearchRepository;
import gr.cite.femme.semantic.search.taxonomy.TaxonomyParser;
import gr.cite.femme.semantic.search.taxonomy.TaxonomyRepository;
import org.semanticweb.skos.SKOSCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.util.List;

@Component
@Path("admin")
public class SemanticSearchAdminResource {
	private static final Logger logger = LoggerFactory.getLogger(SemanticSearchAdminResource.class);
	
	private TaxonomyRepository taxonomyRepository;
	//private ElasticsearchRepository repository;
	
	@Inject
	public SemanticSearchAdminResource(TaxonomyRepository taxonomyRepository) {
		this.taxonomyRepository = taxonomyRepository;
		//this.repository = repository;
	}
	
	@GET
	@Path("ping")
	@Produces(MediaType.TEXT_PLAIN)
	public Response ping() {
		return Response.ok("pong").build();
	}
	
	@POST
	@Path("elements")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insert(SemanticDocument doc) {
		/*try {
			this.repository.insert(doc);
			logger.info(doc.ge().getElementId() + " successfully indexed");
		} catch (SemanticSearchException e) {
			logger.error(e.getMessage(), e);
			throw new WebApplicationException(e);
		}
		return Response.ok("done").build();*/
		return Response.ok("done").build();
	}
	
	@DELETE
	@Path("elements/{id}")
	public Response delete(@PathParam("id") String id) {
//        try {
//            this.repository.delete(id);
//        } catch (FemmeFulltextException e) {
//            logger.error(e.getMessage(), e);
//            throw new WebApplicationException(e);
//        }
		return Response.ok().build();
	}
	
	@DELETE
	@Path("elements")
	public Response deleteByElementId(@QueryParam("elementId") String elementId, @QueryParam("metadatumId") String metadatumId) {
		if (elementId == null && metadatumId == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity("No elementId or metadatumId specified").build();
		}

//        try {
//            if (metadatumId != null) {
//                this.repository.deleteByMetadatumId(metadatumId);
//            } else {
//                this.repository.deleteByElementId(elementId);
//            }
//        } catch (FemmeFulltextException e) {
//            logger.error(e.getMessage(), e);
//            throw new WebApplicationException(e);
//        }
		
		return Response.ok().build();
	}
	
	@POST
	@Path("taxonomies")
	public Response storeTaxonony() {
		TaxonomyParser parser;
		try {
			parser = new TaxonomyParser(Resources.getResource("stw.ttl").toURI());
		} catch (URISyntaxException | SKOSCreationException e) {
			logger.error(e.getMessage(), e);
			throw new WebApplicationException(e);
		}
		this.taxonomyRepository.storeConcepts(parser.parse());
		return Response.ok("taxonomies").build();
	}
}
