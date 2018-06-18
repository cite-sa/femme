import { FulltextDocument } from "@app/models/fulltext-document";
import { FulltextSearchResultSemantic } from "@app/models/fulltext-search-result-semantic";

export class FulltextSearchResult {
	fulltext: FulltextDocument;
	semantic: Array<Array<FulltextSearchResultSemantic>>;
}