package gr.cite.femme.semantic.search.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/*@Configuration
@ImportResource("classpath:/config/application-context.xml")*/
public class AppConfig {

    public static String searchField = "name";

    public AppConfig(String searchField){
        AppConfig.searchField = searchField;
    }

}
