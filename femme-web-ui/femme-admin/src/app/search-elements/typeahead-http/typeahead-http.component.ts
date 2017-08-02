import { FemmeQueryService } from './../../femme-services/femme-query.service';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/distinctUntilChanged';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/switchMap';

import { Component, OnInit } from '@angular/core';
import { FulltextResult } from '../../model/fulltext-result';
import { FulltextField } from '../../model/fulltext-field';
import { FulltextQuery } from '../../model/fulltext-query';

@Component({
  selector: 'typeahead-http',
  templateUrl: './typeahead-http.component.html',
  styleUrls: ['./typeahead-http.component.css']
})

export class TypeaheadHttpComponent {
	query: string;
	// fulltextResults: Array<FulltextResult>;
	errorMessage: string;
	searching: boolean;
	searchFailed: boolean;
	searchResults: Array<FulltextResult>;

	constructor(private femmeService: FemmeQueryService) { }

	// autocomplete(text$: Observable<string>) {

	search($event) {
		console.log($event);
		let fulltextQuery = new FulltextQuery;
		fulltextQuery.metadataField = new FulltextField("name", this.query);
		this.femmeService.autocomplete(fulltextQuery)
			.subscribe(
				searchResults => this.searchResults = searchResults,
				error => this.errorMessage = <any>error);
	}

	formatter = (result: FulltextResult) => result.name;

	autocomplete = (text$: Observable<string>) =>

		// let searching: boolean
		// searching = false;
		// let searchFailed: boolean
  		// searchFailed = false;

		text$
		.debounceTime(300)
		.distinctUntilChanged()
		.do(() => this.searching = true)
		.switchMap(term => {
			console.log("Term: " + term);
			let fulltextQuery: FulltextQuery;
			fulltextQuery = new FulltextQuery();
			fulltextQuery.metadataField = new FulltextField("name", term);
			return this.femmeService.autocomplete(fulltextQuery)
				// .subscribe(
				// 	fulltextResults => this.fulltextResults = fulltextResults,
				// 	error => this.errorMessage = <any>error);
				// .do((result) => console.log(result))
				.do(() => this.searchFailed = false)
            	.catch(() => {
              		this.searchFailed = true;
              		return Observable.of([]);
            	})
		})
		.do(() => this.searching = false);

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
