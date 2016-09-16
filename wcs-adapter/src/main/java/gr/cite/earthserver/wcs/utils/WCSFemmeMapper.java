package gr.cite.earthserver.wcs.utils;

import java.util.List;
import java.util.stream.Collectors;

import gr.cite.earthserver.wcs.core.Coverage;
import gr.cite.earthserver.wcs.core.Server;
import gr.cite.earthserver.wcs.core.WCSResponse;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.model.Metadatum;

public final class WCSFemmeMapper {

	private static final String GET_CAPABILITIES = "GetCapabilities";

	private static final String DESCRIBE_COVERAGE = "DescribeCoverage";
	
	private WCSFemmeMapper() {
		
	}

	public static Collection fromServer(String endpoint, WCSResponse response) throws ParseException {
		Collection collection = new Collection();

		collection.setEndpoint(endpoint);
		collection.setName(WCSParseUtils.getServerName(response.getResponse()));

		collection.getMetadata().add(fromWCSMetadata(response, GET_CAPABILITIES));

		return collection;
	}

	public static DataElement fromCoverage(WCSResponse response) throws ParseException {
		DataElement dataElement = new DataElement();

		dataElement.setName(WCSParseUtils.getCoverageId(response.getResponse()));
		dataElement.setEndpoint(response.getEndpoint());

		dataElement.getMetadata().add(fromWCSMetadata(response, DESCRIBE_COVERAGE));

		return dataElement;
	}

	public static Metadatum fromWCSMetadata(WCSResponse response, String name) {
		Metadatum metadatum = new Metadatum();
		metadatum.setName(name);
		metadatum.setContentType(response.getContentType().toString());
		metadatum.setValue(response.getResponse());
		return metadatum;
	}

	public static Coverage dataElementToCoverage(DataElement dataElement) {
		Coverage coverage = new Coverage();
		coverage.setId(dataElement.getId());
		coverage.setCoverageId(dataElement.getName());

		List<Server> servers = null;
		if (dataElement.getCollections() != null) {
			servers = dataElement.getCollections().stream().map(collection -> collectionToServer(collection))
					.collect(Collectors.toList());
		}
		coverage.setServers(servers);

		String describeCoverage = "";
		if (dataElement.getMetadata() != null && dataElement.getMetadata().size() > 0) {
			describeCoverage = dataElement.getMetadata().stream()
					.filter(metadatum -> metadatum != null ? "DescribeCoverage".equals(metadatum.getName()) : false)
					.findFirst().get().getValue();
		}
		coverage.setMetadata(describeCoverage);

		return coverage;
	}

	public static Server collectionToServer(Collection collection) {
		Server server = new Server();
		server.setId(collection.getId());
		server.setEndpoint(collection.getEndpoint());

		String describeCoverage = "";
		if (collection.getMetadata() != null && collection.getMetadata().size() > 0) {
			describeCoverage = collection.getMetadata().stream()
					.filter(metadatum -> metadatum != null ? "DescribeCoverage".equals(metadatum.getName()) : false)
					.findFirst().get().getValue();
		}
		server.setMetadata(describeCoverage);

		return server;
	}

}
