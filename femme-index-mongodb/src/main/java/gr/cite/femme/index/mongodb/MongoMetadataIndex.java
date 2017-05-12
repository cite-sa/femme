package gr.cite.femme.index.mongodb;

import com.mongodb.client.MongoCollection;

import gr.cite.commons.converter.XmlJsonConverter;
import gr.cite.femme.index.api.client.MetadataIndexClient;
import gr.cite.femme.core.model.Metadatum;
import gr.cite.femme.index.api.client.MetadatumIndex;

import javax.xml.stream.XMLStreamException;

public class MongoMetadataIndex implements MetadataIndexClient {
	
	MetadataIndexMongoClient mongoClient;
	
	MongoCollection<MetadatumIndex> indexCollection;
	
	public MongoMetadataIndex() {
		mongoClient = new MetadataIndexMongoClient();
		indexCollection = mongoClient.getMetadataIndexCollection();
	}
	
	public MongoMetadataIndex(String host, String dbName) {
		mongoClient = new MetadataIndexMongoClient(host, dbName);
		indexCollection = mongoClient.getMetadataIndexCollection();
	}

	@Override
	public void index(Metadatum metadatum) throws XMLStreamException {
		
		MetadatumIndex metadatumIndex = new MetadatumIndex();
		
		metadatumIndex.setId(metadatum.getId());
		metadatumIndex.setElementId(metadatum.getElementId());
		metadatumIndex.setContentType(metadatum.getContentType());
		
		if (metadatum.getContentType().toLowerCase().contains("xml")) {
			metadatumIndex.setValue(XmlJsonConverter.xmlToJson(metadatum.getValue()));
		} else if (metadatum.getContentType().toLowerCase().contains("json")) {
			metadatumIndex.setValue(metadatum.getValue());
		}
		
		indexCollection.insertOne(metadatumIndex);
		
	}

	@Override
	public void reIndex(Metadatum metadatum) {
		// TODO Auto-generated method stub
	}

	@Override
	public void reIndex() {
		// TODO Auto-generated method stub
	}

	@Override
	public String search(String xPath) {
		// TODO Auto-generated method stub
		return null;
	}

}
