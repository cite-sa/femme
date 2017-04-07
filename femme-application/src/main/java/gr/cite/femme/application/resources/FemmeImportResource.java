package gr.cite.femme.application.resources;

import gr.cite.femme.Femme;
import org.springframework.stereotype.Component;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("import")
@Produces(MediaType.APPLICATION_JSON)
public class FemmeImportResource {

	private Femme femme;



	/*@POST
	@Path("")
	public Response createImport(Impoer) {

	}*/
}
