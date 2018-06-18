import { FulltextDocument } from "@app/models/fulltext-document";

export class FulltextSearchResultSemantic {
	term: string;
	docs: Array<FulltextDocument>;
}