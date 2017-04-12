package gr.cite.femme.application;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import gr.cite.femme.api.Datastore;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/*@WebListener*/
public class FemmeContextListener implements ServletContextListener {

	/*@Autowired*/
	private Datastore mongoDatastore;
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("Destroyed");
		
		/*SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        try {
        	datastore.hashCode();
        } catch (Exception e) {
            // rethrow as a runtime exception
            throw new IllegalStateException(e);
        }*/
		
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
		System.out.println("Initalized");
		
	}
}
