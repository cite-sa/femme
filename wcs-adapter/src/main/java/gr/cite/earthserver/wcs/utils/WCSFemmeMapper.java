package gr.cite.earthserver.wcs.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gr.cite.earthserver.wcs.core.Coverage;
import gr.cite.earthserver.wcs.core.Server;
import gr.cite.earthserver.wcs.core.WCSResponse;
import gr.cite.femme.model.BBox;
import gr.cite.femme.model.Collection;
import gr.cite.femme.model.DataElement;
import gr.cite.femme.model.Metadatum;
import gr.cite.femme.utils.Pair;

public final class WCSFemmeMapper {

	private static final String GET_CAPABILITIES = "GetCapabilities";

	private static final String DESCRIBE_COVERAGE = "DescribeCoverage";
	
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private WCSFemmeMapper() {
		
	}
	
	public static Collection fromServer(String endpoint, String name, WCSResponse response) throws ParseException {
		/*Collection.Builder collectionBuilder = Collection.builder();*/
		Collection collection = new Collection();

		/*collectionBuilder.endpoint(endpoint);*/
		collection.setEndpoint(endpoint);
		
		/*collectionBuilder.name(WCSParseUtils.getServerName(response.getResponse()));*/
		collection.setName(name);
		

		/*collectionBuilder.metadatum(fromWCSMetadata(response, GET_CAPABILITIES));*/
		collection.setMetadata(Arrays.asList(WCSFemmeMapper.fromWCSMetadata(response, GET_CAPABILITIES)));

		/*return collectionBuilder.build();*/
		return collection;
	}

	public static DataElement fromCoverage(WCSResponse response) throws ParseException {
		DataElement dataElement = new DataElement();

		dataElement.setName(WCSParseUtils.getCoverageId(response.getResponse()));
		dataElement.setEndpoint(response.getEndpoint());

		dataElement.getMetadata().add(WCSFemmeMapper.fromWCSMetadata(response, DESCRIBE_COVERAGE));
		
		Map<String, Object> other = new HashMap<>();
		
		Pair<String, String> bboxGeoJsonWithCRS = WCSParseUtils.getBoundingBoxJSON(response.getResponse());
		String bboxJson = null;
		if (bboxGeoJsonWithCRS.getRight() != null) {
			BBox bbox = new BBox(bboxGeoJsonWithCRS.getLeft(), bboxGeoJsonWithCRS.getRight());
			
			try {
				bboxJson = mapper.writeValueAsString(bbox);
			} catch (JsonProcessingException e) {
				throw new ParseException(e.getMessage(), e);
			}
			other.put("bbox", bboxJson);
		}
		dataElement.getSystemicMetadata().setOther(other);

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
			servers = dataElement.getCollections().stream().map(collection -> WCSFemmeMapper.collectionToServer(collection))
					.collect(Collectors.toList());
		}
		coverage.setServers(servers);

		String describeCoverage = "";
		if (dataElement.getMetadata() != null && dataElement.getMetadata().size() > 0) {
			describeCoverage = dataElement.getMetadata().stream()
					.filter(metadatum -> metadatum != null ? WCSFemmeMapper.DESCRIBE_COVERAGE.equals(metadatum.getName()) : false)
					.findFirst().get().getValue();
		}
		coverage.setMetadata(describeCoverage);

		return coverage;
	}

	public static Server collectionToServer(Collection collection) {
		if (collection != null) {
			Server server = new Server();
			server.setId(collection.getId());
			server.setEndpoint(collection.getEndpoint());
	
			String describeCoverage = "";
			if (collection.getMetadata() != null && collection.getMetadata().size() > 0) {
				describeCoverage = collection.getMetadata().stream()
						.filter(metadatum -> metadatum != null ? WCSFemmeMapper.GET_CAPABILITIES.equals(metadatum.getName()) : false)
						.findFirst().get().getValue();
			}
			server.setMetadata(describeCoverage);

			return server;
		}
		return null;
	}

}
