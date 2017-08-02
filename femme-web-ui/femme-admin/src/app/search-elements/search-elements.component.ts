import { FulltextResult } from './../model/fulltext-result';
import { FulltextField } from './../model/fulltext-field';
import { Component, OnInit } from '@angular/core';

import { FulltextQuery } from './../model/fulltext-query';
import { Element } from '../model/element'

@Component({
	selector: 'app-search-elements',
	templateUrl: './search-elements.component.html',
	styleUrls: ['./search-elements.component.css']
})

export class SearchElementsComponent {
	// elements: Array<Element>;
	// fulltextResults: Array<FulltextResult>;
	// errorMessage: string;
	// mode = 'Observable';

	constructor() {}

	// search() {
	// 	let query: FulltextQuery;
	// 	query = new FulltextQuery;
	// 	query.metadataField = new FulltextField("name", "pl_rehum");
	// 	this.femmeSearchService.autocomplete(query)
	// 		.subscribe(
	// 			fulltextResults => this.fulltextResults = fulltextResults,
	// 			error => this.errorMessage = <any>error);
	// }
}
