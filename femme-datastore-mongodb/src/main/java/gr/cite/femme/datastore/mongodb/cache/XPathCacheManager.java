package gr.cite.femme.datastore.mongodb.cache;

import java.util.List;

import gr.cite.femme.model.Element;
import gr.cite.femme.model.Metadatum;

public interface XPathCacheManager {
	
	public void createIndex();
	
	public void removeIndex();
	
	/*public void createIndexOnXPath(Metadatum metadatum, String xpath, List<String> xPathResultm T element);*/
	
	public <T extends Element> void checkAndCreateIndexOnXPath(Metadatum metadatum, String xpath, List<String> xPathResult, T element);
	
	public void removeIndexOnXPath();
}
