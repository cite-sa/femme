package gr.cite.femme.metadata.xpath.mongodb.evaluation;

import com.mongodb.client.model.Filters;
import gr.cite.femme.metadata.xpath.grammar.XPathLexer;
import gr.cite.femme.metadata.xpath.grammar.XPathParser;
import gr.cite.femme.metadata.xpath.mongodb.MongoXPathDatastore;
import gr.cite.femme.metadata.xpath.parser.visitors.MongoXPathVisitor;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MongoXPathQuery {

    Logger logger = LoggerFactory.getLogger(MongoXPathQuery.class);

    private MongoXPathDatastore xPathDatastore;

    public MongoXPathQuery(MongoXPathDatastore xPathDatastore) {
        this.xPathDatastore = xPathDatastore;
    }

    public MongoXPathResult query(String xPath) {
        CharStream stream = new ANTLRInputStream(xPath);
        XPathLexer lexer = new XPathLexer(stream);
        XPathParser parser = new XPathParser(new CommonTokenStream(lexer));

        ParseTree tree = parser.xpath();

        /*MongoQuery mongoQuery = new MongoQuery();
        MongoXPathVisitor visitor = new MongoXPathVisitor(mongoQuery);
        visitor.visit(tree);
        mongoQuery.appendPathRegEx("$");
        Bson query = new Document().append("path", new Document().append("$regex", mongoQuery.getPathRegEx().toString()));*/

        List<Document> subQueries = new ArrayList<>();
        MongoXPathVisitor visitor = new MongoXPathVisitor(subQueries);
        visitor.visit(tree);
        Bson query = new Document().append("$and", subQueries);

        logger.info(query.toString());

        MongoXPathResult xPathResult = new MongoXPathResult();
        xPathResult.setResult(xPathDatastore.xPath(query));

        return xPathResult;
    }
}
