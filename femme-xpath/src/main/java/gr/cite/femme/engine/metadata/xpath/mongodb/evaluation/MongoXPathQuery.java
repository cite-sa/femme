package gr.cite.femme.engine.metadata.xpath.mongodb.evaluation;

import gr.cite.femme.engine.metadata.xpath.datastores.api.MetadataIndexDatastore;
import gr.cite.femme.engine.metadata.xpath.grammar.XPathLexer;
import gr.cite.femme.engine.metadata.xpath.grammar.XPathParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MongoXPathQuery {

    Logger logger = LoggerFactory.getLogger(MongoXPathQuery.class);

    private MetadataIndexDatastore xPathDatastore;

    public MongoXPathQuery(MetadataIndexDatastore xPathDatastore) {
        this.xPathDatastore = xPathDatastore;
    }

    public Bson query(String xPath) {
        CharStream stream = CharStreams.fromString(xPath);
        XPathLexer lexer = new XPathLexer(stream);
        XPathParser parser = new XPathParser(new CommonTokenStream(lexer));

        ParseTree tree = parser.xpath();

        /*MongoQuery mongoQuery = new MongoQuery();
        MongoXPathVisitor visitor = new MongoXPathVisitor(mongoQuery);
        visitor.visit(tree);
        mongoQuery.appendPathRegEx("$");
        Bson query = new Document().append("path", new Document().append("$regex", mongoQuery.getPathRegEx().toString()));*/

        List<Document> subQueries = new ArrayList<>();
        /*MongoXPathVisitor visitor = new MongoXPathVisitor(subQueries);*/
        /*visitor.visit(tree);*/
        Bson query = new Document().append("$and", subQueries);

        /*MongoXPathResult xPathResult = new MongoXPathResult();
        xPathResult.setResult(xPathDatastore.xPath(query));*/

        return query;
    }
}
