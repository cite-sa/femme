package gr.cite.earthserver.wcs.utils;

import gr.cite.earthserver.wcs.core.WCSResponse;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.model.Metadatum;

public class WCSFemmeMapper {
	public static Collection fromServer(String endpoint, WCSResponse response) throws ParseException {
		Collection collection = new Collection();
		
		collection.setEndpoint(endpoint);
		collection.setName(WCSParseUtils.getServerName(response.getResponse()));
		
		collection.getMetadata().add(fromWCSMetadata(response));
		
		return collection;
	}
	
	public static DataElement fromCoverage(WCSResponse response) throws ParseException {
		DataElement dataElement = new DataElement();
		
		dataElement.setName(WCSParseUtils.getCoverageId(response.getResponse()));
		dataElement.setEndpoint(response.getEndpoint());
		
		dataElement.getMetadata().add(fromWCSMetadata(response));
		
		return dataElement;
	}
	
	public static Metadatum fromWCSMetadata(WCSResponse response) {
		Metadatum metadatum = new Metadatum();
		metadatum.setContentType(response.getContentType().toString());
		metadatum.setValue(response.getResponse());
		return metadatum;
	}
}
