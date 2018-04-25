package gr.cite.femme.semantic.search.resources;

import gr.cite.femme.fulltext.core.FulltextDocument;
import gr.cite.femme.fulltext.engine.FemmeFulltextException;
import gr.cite.femme.semantic.search.model.SemanticDocument;
import gr.cite.femme.semantic.search.repository.ElasticsearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("admin")
public class SemanticSearchAdminResource {
    private static final Logger logger = LoggerFactory.getLogger(SemanticSearchAdminResource.class);

    private ElasticsearchRepository repository;

    @Inject
    public SemanticSearchAdminResource(ElasticsearchRepository repository) {
        this.repository = repository;
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
        try {
            this.repository.insert(doc);
            logger.info(doc.getFulltextDocument().getElementId() + " successfully indexed");
        } catch (FemmeFulltextException e) {
            logger.error(e.getMessage(), e);
            throw new WebApplicationException(e);
        }
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
}
