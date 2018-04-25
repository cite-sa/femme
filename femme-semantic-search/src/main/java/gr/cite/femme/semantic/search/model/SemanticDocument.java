package gr.cite.femme.semantic.search.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import gr.cite.femme.fulltext.core.FulltextDocument;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SemanticDocument {

    private String score;
    private FulltextDocument fulltextDocument;

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public FulltextDocument getFulltextDocument() {
        return fulltextDocument;
    }

    public void setFulltextDocument(FulltextDocument fulltextDocument) {
        this.fulltextDocument = fulltextDocument;
    }
}
