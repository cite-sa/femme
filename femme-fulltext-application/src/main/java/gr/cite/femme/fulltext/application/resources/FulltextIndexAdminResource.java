package gr.cite.femme.fulltext.application.resources;

import com.google.common.io.Resources;
import gr.cite.femme.fulltext.core.FulltextDocument;
import gr.cite.femme.fulltext.engine.FulltextIndexEngine;
import gr.cite.femme.fulltext.engine.FemmeFulltextException;
import gr.cite.femme.fulltext.engine.semantic.search.taxonomy.TaxonomyParserSkosApi;
import org.elasticsearch.common.Strings;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.semanticweb.skos.SKOSCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

@Component
@Path("admin")
public class FulltextIndexAdminResource {
	private static final Logger logger = LoggerFactory.getLogger(FulltextIndexAdminResource.class);

	private FulltextIndexEngine engine;

	@Inject
	public FulltextIndexAdminResource(FulltextIndexEngine engine) {
		this.engine = engine;
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
	public Response insert(FulltextDocument doc) {
		try {
			this.engine.insert(doc);
			logger.info(doc.getElementId() + " successfully indexed");
		} catch (IOException | FemmeFulltextException e) {
			logger.error(e.getMessage(), e);
			throw new WebApplicationException(e);
		}
		
		return Response.ok("done").build();
	}

	@DELETE
	@Path("elements/{id}")
	public Response delete(@PathParam("id") String id) {
		try {
			this.engine.delete(id);
		} catch (FemmeFulltextException e) {
			logger.error(e.getMessage(), e);
			throw new WebApplicationException(e);
		}
		return Response.ok().build();
	}

	@DELETE
	@Path("elements")
	public Response deleteByElementId(@QueryParam("elementId") String elementId, @QueryParam("metadatumId") String metadatumId) {
		if (elementId == null && metadatumId == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity("No elementId or metadatumId specified").build();
		}

		try {
			if (metadatumId != null) {
				this.engine.deleteByMetadatumId(metadatumId);
			} else {
				this.engine.deleteByElementId(elementId);
			}
		} catch (FemmeFulltextException e) {
			logger.error(e.getMessage(), e);
			throw new WebApplicationException(e);
		}

		return Response.ok().build();
	}
	
	@POST
	@Path("taxonomies")
	public Response enableTaxonony(@QueryParam("enable") String taxonomyName, @QueryParam("uri") URI uri) {
		URI taxonomyUri = null;
		TaxonomyParserSkosApi parser;
		try {
			if (!Strings.isNullOrEmpty(taxonomyName)) {
				taxonomyUri = Resources.getResource(taxonomyName).toURI();
			} else if (uri != null) {
				taxonomyUri = uri;
			}
			
			if (taxonomyUri != null) {
				parser = new TaxonomyParserSkosApi(taxonomyUri);
			} else {
				throw new WebApplicationException("Error while parsing taxonomy");
			}
		} catch (URISyntaxException | SKOSCreationException e) {
			logger.error(e.getMessage(), e);
			throw new WebApplicationException("Error while parsing taxonomy", e);
		}
		this.engine.storeConcepts(parser.parse());
		return Response.ok("taxonomies").build();
	}
	
	@PUT
	@Path("taxonomies")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response storeTaxonony(@FormDataParam("file") InputStream taxonomyInputStream, @FormDataParam("file") FormDataContentDisposition taxonomyFileDetails) {
		String tempDirectory = System.getProperty("java.io.tmpdir");
		System.out.println("OS current temporary directory is " + tempDirectory);
		
		File tempTaxonomyFile = new File(tempDirectory + "/" + taxonomyFileDetails.getFileName());
		
		try {
			int read = 0;
			byte[] bytes = new byte[1024];
			
			OutputStream out = new FileOutputStream(tempTaxonomyFile);
			while ((read = taxonomyInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			tempTaxonomyFile.delete();
			logger.error("Error while uploading file [" + taxonomyFileDetails.getFileName() + "]" + ". Please try again", e);
			throw new WebApplicationException("Error while uploading file [" + taxonomyFileDetails.getFileName() + "]" + ". Please try again");
		}
		
		TaxonomyParserSkosApi parser;
		try {
			parser = new TaxonomyParserSkosApi(tempTaxonomyFile.toURI());
		} catch (Exception e) {
			tempTaxonomyFile.delete();
			logger.error(e.getMessage(), e);
			throw new WebApplicationException("Error while parsing taxonomy [" + taxonomyFileDetails.getFileName() + "]", e);
		}
		
		tempTaxonomyFile.delete();
		
		this.engine.storeConcepts(parser.parse());
		return Response.ok("taxonomies").build();
	}
}
