import { Component, Output, EventEmitter, HostListener } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Observable, BehaviorSubject } from 'rxjs';
import { debounceTime, map } from 'rxjs/operators';
import { trigger, state, style, transition, animate } from '@angular/animations';

import { FemmeSearchService } from '@app/services/femme-search.service';
import { FulltextSearchResult } from '@app/models/fulltext-search-result';
import { FemmeQuery } from '@app/models/femme-query';
import { MatAutocompleteSelectedEvent } from '@angular/material';

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
	expansionTypes = ["Broader", "Narrower", "Related"];

	loading = false;

	searchTerm : FormControl = new FormControl();
	searchResults: FulltextSearchResult[];
	expansionType: string;

	defaultFieldShown = "name";
	showFields: string = "hide";
	searchField: string = "all_fields";

	xpathTerm: FormControl = new FormControl();

	@Output()
	queryEvent = new EventEmitter<FemmeQuery>();


	@HostListener('document:click', ['$event']) clickedOutside($event) {
		if ($event.which !== 3) {
			this.showFields = 'hide';
		}
	  }

	constructor(private searchService: FemmeSearchService) {
		this.searchTerm.valueChanges.pipe(
			debounceTime(400) 
		).subscribe(term => {
			if (typeof term === "string") {
				this.loading = true;
				this.searchService.search(this.searchField, term, this.expansionType ? this.expansionType.toLowerCase() : undefined)
				.subscribe(
					(results) => this.searchResults = results,
					(error) => console.log(error),
					() => this.loading = false
				);
			}
		});
	}

	elementToDisplayValue(element: FulltextSearchResult) {
		console.log("elementToDisplayValue");
		console.log(element);
		if (element != undefined) {
			if (element.fulltext) {
				let propertyNames: string[] = Object.getOwnPropertyNames(element.fulltext);
				let projectedFields =  propertyNames.filter(propertyName => (propertyName != "metadatumId") && (propertyName != "elementId"));
				return projectedFields.map(field => element.fulltext[field]).join(" - ");
			} else {
				return element["name"];
			}
		}
	}

	selectFromAutocomplete(event: MatAutocompleteSelectedEvent) {
		console.log("selectFromAutocomplete");
		console.log(event);
		if (event.option.value.fulltext) {
			let query = {
				metadataField: {
					field: "name",
					value: event.option.value.fulltext.name
				}
			}
			this.searchService.searchExact(query).subscribe(
					elements => {
						console.log(elements);
						let elementIds = elements.map(element => element.fulltext.elementId);
						this.emitQueryEvent(elementIds);
					},
					error => console.log(error),
					() => this.loading = false
				);
		} else {
			let query = {
				metadataField: {
					field: "name",
					value: event.option.value.name
				}
			}
			this.searchService.searchExact(query).subscribe(
					elements => {
						console.log(elements);
						let elementIds = elements.map(element => element.fulltext.elementId);
						this.emitQueryEvent(elementIds);
					},
					error => console.log(error),
					() => this.loading = false
				);
		}
	}

	fulltextQuery() {
		// this.loading = true;
		let term: string = this.searchTerm.value;

		if (term == undefined && term == "") {
			this.emitQueryEvent();
			this.loading = false;
		} else {
			this.searchByTerm(term, () => this.loading = false);
		}
	}

	private searchByTerm(term: string, completedCallbak) {
		this.searchService.search(this.searchField, term, this.expansionType ? this.expansionType.toLowerCase() : undefined)
				.pipe(
					map(results => results.map(result => result.fulltext.elementId))
				).subscribe(
					ids => this.emitQueryEvent(ids),
					error => console.log(error),
					() => {
						if (completedCallbak) completedCallbak();
					}
			);
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

	stopProp(event: any) {
		event.stopPropagation();
	}

	filterButtonClick(event: any) {
		if (event.which !== 1) {
			event.stopPropagation();
			this.showFields = this.showFields === 'hide' ? 'show' : 'hide';
		}
	}
}
