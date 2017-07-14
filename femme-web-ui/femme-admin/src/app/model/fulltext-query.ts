import { FulltextField } from './fulltext-field';

export class FulltextQuery {
	elementId: string;
	metadatumId: string;
	collectionId: string;
	elementName: string;
	collectionName: string;
	metadataField: FulltextField;
	autocompleteField: FulltextField;
	allField: string;

	constructor() {

	}
}
