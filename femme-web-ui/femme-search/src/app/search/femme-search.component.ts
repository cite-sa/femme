import { Component, Output, EventEmitter, HostListener, ViewChild } from '@angular/core';
import { FormControl, FormGroup, FormBuilder } from '@angular/forms';
import { debounceTime, map } from 'rxjs/operators';
import { trigger, state, style, transition, animate } from '@angular/animations';

import { FemmeSearchService } from '@app/services/femme-search.service';
import { FulltextSearchResult } from '@app/models/fulltext-search-result';
import { FemmeQuery } from '@app/models/femme-query';
import { MatAutocompleteSelectedEvent, MatAutocomplete, MatAutocompleteTrigger } from '@angular/material';

@Component({
	selector: 'femme-search',
	templateUrl: './femme-search.component.html',
	styleUrls: ['./femme-search.component.css'],
	animations: [
		trigger('collapse', [
			state('show', style({
				height: '60px'
			})),
			state('hide', style({
				height: '0px'
			})),
			transition('show => hide', animate('400ms ease-in-out')),
			transition('hide => show', animate('400ms ease-in-out'))
		])
	]
})
export class FemmeSearchComponent {
	expansionTypes = ["All", "Broader", "Narrower", "Related"];

	loading = false;
	@ViewChild(MatAutocompleteTrigger) autoTrigger: MatAutocompleteTrigger;

	fulltextQueryForm: FormGroup;
	// searchTerm : FormControl = new FormControl();
	searchResults: FulltextSearchResult[];
	expansionTypeValue: string;

	defaultFieldShown = "name";
	showFields: string = "hide";
	searchField: string = "all_fields";

	xpathTerm: FormControl = new FormControl();

	@Output()
	queryEvent = new EventEmitter<FemmeQuery>();
	@Output()
	loadingEvent = new EventEmitter<boolean>();

	constructor(private searchService: FemmeSearchService, private fb: FormBuilder) {
		this.fulltextQueryForm = this.fb.group({
			searchTerm: '',
			expansionType: ''
		  });

		this.fulltextQueryForm.valueChanges
			.pipe(debounceTime(600))
			.subscribe(query => {
				console.log("VALUE CHANGED");
				console.log(query);

				let term: any = query.searchTerm;

				if (typeof term !== "string") {
					if (term.fulltext) {
						term = term.fulltext.name;
					}
				}

				console.log(term);

				if (typeof term === "string") {
					this.executeQuery(term, query.expansionType);
				}
			});
	}

	private executeQuery(term: string, expansion: string) {
		let expansionType: string = expansion ? expansion.toLowerCase() : undefined;
		
		this.loading = true;
		this.loadingEvent.emit(true);

		this.searchService.search(this.searchField, term, expansionType)
		.subscribe(
			// (results) => this.searchResults = results,
			(results) => {
				this.loadingEvent.emit(false);
				if (results.length > 0) {
					if (expansionType != undefined) {
						this.handleExpansionQuery(results);
					} else {
						this.handlePlainQuery(results);
					}
				}
			},
			(error) => {
				this.loadingEvent.emit(false);
				console.log(error)
			},
			() => {
				this.loading = false;
				this.loadingEvent.emit(false);
				this.autoTrigger.openPanel();
			}
		);

		if (expansionType == undefined) {
			this.searchUniqueTermsInCaseOfPlainQuery(term);
		}
	}

	handleExpansionQuery(results: FulltextSearchResult[]) {
		this.searchResults = results;
		let ids = [];
		results[0].semantic.forEach(res => {
			res.forEach(r => {
				ids = ids.concat(r.docs.map(doc => doc.elementId));
			});
		});
		console.log(ids);
		this.emitQueryEvent(ids);
	}

	handlePlainQuery(results: FulltextSearchResult[]) {
		let ids = results.map(re => re.fulltext.elementId);
		this.emitQueryEvent(ids);
	}

	searchUniqueTermsInCaseOfPlainQuery(term: string) {
		this.searchService.searchUnique(this.searchField, term)
			.subscribe(results => {
				this.searchResults = results;
			}),
			(error) => console.log(error),
			() => {
				
			}
	}

	elementToDisplayValue(element: FulltextSearchResult) {
		if (element != undefined) {
			if (element.fulltext) {
				let propertyNames: string[] = Object.getOwnPropertyNames(element.fulltext);
				let projectedFields =  propertyNames.filter(propertyName => (propertyName != "metadatumId") && (propertyName != "elementId"));
				return projectedFields.map(field => element.fulltext[field]).join(" - ");
			} else {
				return element;
			}
		}
	}

	// selectFromAutocomplete(event: MatAutocompleteSelectedEvent) {
	// 	if (event.option.value.fulltext) {
	// 		this.searchExact(event.option.value.fulltext.name, () => this.loading = false);
	// 	} else {
	// 		this.searchExact(event.option.value, () => {
	// 			this.loading = false;
	// 		});
	// 	}
	// }

	fulltextQuery() {
		let term: string = this.fulltextQueryForm.value.searchTerm;
		let expansionType: string = this.fulltextQueryForm.value.expansionType;

		console.log("SUBMIT");
		console.log(term);

		if (term == undefined || term == "") {
			this.emitQueryEvent();
		} else {
			// let searchTerm: string;
			// searchTerm = typeof term === "string" ? term : term.fulltext["name"];
			// this.searchExact(searchTerm);
			this.executeQuery(term, expansionType);
		}
	}

	private searchExact(term: string, completedCallbak?: Function) {
		let query = {
			metadataField: {
				field: "name",
				value: term
			}
		}

		this.searchService.searchExact(query).subscribe(
			elements => {
				let elementIds = elements.map(element => element.fulltext.elementId);
				this.emitQueryEvent(elementIds);
			},
			error => console.log(error),
			() => {
				if (completedCallbak) completedCallbak();
			}
		)
	}

	xpathQuery() {
		this.emitQueryEvent(undefined, this.xpathTerm.value);
	}

	private emitQueryEvent(ids?: string[], xpath?: string): void {
		let query = new FemmeQuery();
		query.ids = ids;
		query.xpath = xpath;

		this.queryEvent.emit(query);
	}

}
