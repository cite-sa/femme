package gr.cite.femme.fulltext.engine;

import gr.cite.femme.fulltext.core.FulltextDocument;
import gr.cite.femme.fulltext.engine.semantic.search.taxonomy.ElasticsearchClient;
import gr.cite.femme.fulltext.engine.semantic.search.taxonomy.QueryExpander;
import gr.cite.femme.fulltext.engine.semantic.search.taxonomy.TaxonomyRepository;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.UUID;

public class FullTextEngineTest {
	private FulltextIndexEngine engine;

	@Before
	public void init() throws IOException {
		ElasticsearchClient elasticsearchClient = new ElasticsearchClient("localhost", 9200, "semantic_search", "taxonomies");
		QueryExpander queryExpander = new QueryExpander(elasticsearchClient);
		TaxonomyRepository taxonomyRepository = new TaxonomyRepository(elasticsearchClient, queryExpander);
		this.engine = new FulltextIndexEngine("localhost", 9200, "fulltext_search", taxonomyRepository);
	}

	@Test
	public void testInsert() throws FemmeFulltextException, IOException {
		FulltextDocument doc = new FulltextDocument();
		doc.setElementId(UUID.randomUUID().toString());
		doc.setMetadatumId(UUID.randomUUID().toString());

		doc.setFulltextField("testField", "Evolutionary algorithm");
		this.engine.insert(doc);
	}
}
