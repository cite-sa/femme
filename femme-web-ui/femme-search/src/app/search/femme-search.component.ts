import { Component, Output, EventEmitter, HostListener } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Observable, BehaviorSubject } from 'rxjs';
import { debounceTime, map } from 'rxjs/operators';
import { trigger, state, style, transition, animate } from '@angular/animations';

import { FemmeSearchService } from '@app/services/femme-search.service';
import { FulltextSearchResult } from '@app/models/fulltext-search-result';
import { DataElement } from '@app/models/data-element';
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

	// queryType: string;
	searchTerm : FormControl = new FormControl();
	searchResults: Observable<Array<FulltextSearchResult>>;
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
				this.searchResults = this.searchService.search(this.searchField, term, this.expansionType ? this.expansionType.toLowerCase() : undefined);
			}
		});
	}

	elementToDisplayValue(element: FulltextSearchResult) {
		if (element != undefined) {
			let propertyNames: string[] = Object.getOwnPropertyNames(element.fulltext);
			let projectedFields =  propertyNames.filter(propertyName => (propertyName != "metadatumId") && (propertyName != "elementId"));
			return projectedFields.map(field => element.fulltext[field]).join(" - ");
		}
	}

	selectFromAutocomplete(event: MatAutocompleteSelectedEvent) {
		this.emitQueryEvent([event.option.value.fulltext.elementId]);
	}

	fulltextQuery() {
		let term: string = this.searchTerm.value;

		if (term == undefined && term == "") {
			this.emitQueryEvent();
		} else {
			this.searchByTerm(term);
		}
	}

	private searchByTerm(term: string) {
		this.searchService.search(this.searchField, term, this.expansionType ? this.expansionType.toLowerCase() : undefined)
				.pipe(
					map(results => results.map(result => result.fulltext.elementId))
				).subscribe(ids => this.emitQueryEvent(ids));
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
