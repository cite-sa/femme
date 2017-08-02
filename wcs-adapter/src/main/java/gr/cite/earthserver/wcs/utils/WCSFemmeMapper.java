package gr.cite.earthserver.wcs.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import gr.cite.earthserver.wcs.geo.GeoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gr.cite.earthserver.wcs.core.Coverage;
import gr.cite.earthserver.wcs.core.Server;
import gr.cite.earthserver.wcs.core.WCSResponse;
import gr.cite.femme.core.model.BBox;
import gr.cite.femme.core.model.Collection;
import gr.cite.femme.core.model.DataElement;
import gr.cite.femme.core.model.Metadatum;
import gr.cite.femme.core.utils.Pair;

public final class WCSFemmeMapper {
	private static final Logger logger = LoggerFactory.getLogger(WCSFemmeMapper.class);
	private static final ObjectMapper mapper = new ObjectMapper();

	private static final String GET_CAPABILITIES = "GetCapabilities";
	private static final String DESCRIBE_COVERAGE = "DescribeCoverage";

	public static Collection fromServer(String endpoint, String name, WCSResponse response) throws ParseException {
		/*Collection.Builder collectionBuilder = Collection.builder();*/
		Collection collection = new Collection();

		/*collectionBuilder.endpoint(endpoint);*/
		collection.setEndpoint(endpoint);
		
		/*collectionBuilder.name(WCSParseUtils.getServerName(response.getResponse()));*/
		collection.setName(name);
		

		/*collectionBuilder.metadatum(fromWCSMetadata(response, GET_CAPABILITIES));*/
		collection.setMetadata(Collections.singletonList(WCSFemmeMapper.fromWCSMetadata(response, GET_CAPABILITIES)));

		/*return collectionBuilder.build();*/
		return collection;
	}

	public static DataElement fromCoverage(WCSResponse response) throws ParseException {
		DataElement dataElement = new DataElement();

		dataElement.setName(WCSParseUtils.getCoverageId(response.getResponse()));
		dataElement.setEndpoint(response.getEndpoint());

		dataElement.getMetadata().add(WCSFemmeMapper.fromWCSMetadata(response, DESCRIBE_COVERAGE));

		// TODO uncomment to transform geodata
		/*try {
			Pair<String, String> bboxGeoJsonWithCRS = GeoUtils.getGeoJsonBoundingBoxFromDescribeCoverage(response.getResponse());
			
			//Map<String, Object> other = new HashMap<>();
			Map<String, String> geo = new HashMap<>();
			String bboxJson;
			if (bboxGeoJsonWithCRS.getRight() != null) {
				BBox bbox = new BBox(bboxGeoJsonWithCRS.getLeft(), bboxGeoJsonWithCRS.getRight());
				
				try {
					bboxJson = mapper.writeValueAsString(bbox);
				} catch (JsonProcessingException e) {
					throw new ParseException(e.getMessage(), e);
				}
				geo.put("bbox", bboxJson);
			}
			//dataElement.getSystemicMetadata().setOther(other);
			dataElement.getSystemicMetadata().setGeo(geo);
			
		} catch(ParseException e) {
			logger.error(e.getMessage());
		}*/

		return dataElement;
	}

	public static Metadatum fromWCSMetadata(WCSResponse response, String name) {
		Metadatum metadatum = new Metadatum();
		metadatum.setEndpoint(response.getEndpoint());
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
			servers = dataElement.getCollections().stream().map(WCSFemmeMapper::collectionToServer)
					.collect(Collectors.toList());
		}
		coverage.setServers(servers);

		//String describeCoverage = "";
		if (dataElement.getMetadata() != null && dataElement.getMetadata().size() > 0) {
			dataElement.getMetadata().stream()
					.filter(metadatum -> metadatum != null && WCSFemmeMapper.DESCRIBE_COVERAGE.equals(metadatum.getName()))
					.findFirst().ifPresent(dataElementMetadatum -> coverage.setMetadata(dataElementMetadatum.getValue()));
		}
		//coverage.setMetadata(describeCoverage);

		return coverage;
	}

	public static Server collectionToServer(Collection collection) {
		if (collection != null) {
			Server server = new Server();
			server.setId(collection.getId());
			server.setName(collection.getName());
			server.setEndpoint(collection.getEndpoint());
	
			String describeCoverage = "";
			if (collection.getMetadata() != null && collection.getMetadata().size() > 0) {
				describeCoverage = collection.getMetadata().stream()
						.filter(metadatum -> metadatum != null && WCSFemmeMapper.GET_CAPABILITIES.equals(metadatum.getName()))
						.findFirst().orElse(null).getValue();
			}
			server.setMetadata(describeCoverage);

			return server;
		}
		return null;
	}

}
