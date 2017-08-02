import gr.cite.opensearch.model.OpenSearchDescription;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;

public class JAXBMarshaller {

	@Test
	public void marshal() throws Exception {
		String simple = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<OpenSearchDescription xmlns=\"http://a9.com/-/spec/opensearch/1.1/\">" +
					"<ShortName>Web Search</ShortName>" +
					"<Description>Use Example.com to search the Web.</Description>" +
					"<Tags>example web</Tags>" +
					"<Contact>admin@example.com</Contact>" +
					"<Url type=\"application/rss+xml\" template=\"http://example.com/?q={searchTerms}&amp;pw={startPage?}&amp;format=rss\"/>" +
				"</OpenSearchDescription>";
		
		String detailed = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<OpenSearchDescription xmlns=\"http://a9.com/-/spec/opensearch/1.1/\">" +
				"<ShortName>Web Search</ShortName>" +
				"<Description>Use Example.com to search the Web.</Description>" +
				"<Tags>example web</Tags>" +
				"<Contact>admin@example.com</Contact>" +
				"<Url type=\"application/atom+xml\" template=\"http://example.com/?q={searchTerms}&amp;pw={startPage?}&amp;format=atom\"/>" +
				"<Url type=\"application/rss+xml\" template=\"http://example.com/?q={searchTerms}&amp;pw={startPage?}&amp;format=rss\"/>" +
				"<Url type=\"text/html\" template=\"http://example.com/?q={searchTerms}&amp;pw={startPage?}\"/>" +
				"<LongName>Example.com Web Search</LongName>" +
				"<Image height=\"64\" width=\"64\" type=\"image/png\">http://example.com/websearch.png</Image>" +
				"<Image height=\"16\" width=\"16\" type=\"image/vnd.microsoft.icon\">http://example.com/websearch.ico</Image>" +
				"<Query role=\"example\" searchTerms=\"cat\" />" +
				"<Developer>Example.com Development Team</Developer>" +
				"<Attribution>Search data Copyright 2005, Example.com, Inc., All Rights Reserved</Attribution>" +
				"<SyndicationRight>open</SyndicationRight>" +
				"<AdultContent>false</AdultContent>" +
				"<Language>en-us</Language>" +
				"<OutputEncoding>UTF-8</OutputEncoding>" +
				"<InputEncoding>UTF-8</InputEncoding>" +
				"</OpenSearchDescription>";

		JAXBContext jc = JAXBContext.newInstance(OpenSearchDescription.class);

		Unmarshaller unmarshaller = jc.createUnmarshaller();
		OpenSearchDescription osd = (OpenSearchDescription) unmarshaller.unmarshal(new ByteArrayInputStream(detailed.getBytes()));

		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(osd, System.out);
	}
}
