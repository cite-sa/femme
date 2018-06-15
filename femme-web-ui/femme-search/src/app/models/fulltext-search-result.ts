import { FulltextDocument } from "@app/models/fulltext-document";

export class FulltextSearchResult {
	fulltext: FulltextDocument;
	semantic: FulltextDocument[][];
}