import { Component, Output, EventEmitter, HostListener } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Observable, BehaviorSubject } from 'rxjs';
import { debounceTime } from 'rxjs/operators';
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
			this.searchResults = this.searchService.search(this.searchField, term, this.expansionType ? this.expansionType.toLowerCase() : undefined);
		});
	}

	elementToDisplayValue(element: FulltextSearchResult) {
		if (element) {		
			return element.fulltext["name"];
		}
	}

	selectFromAutocomplete(event: MatAutocompleteSelectedEvent) {
		let searchResult: FulltextSearchResult = event.option.value;

		let query = new FemmeQuery();
		query.ids = [searchResult.fulltext.elementId];
		this.queryEvent.emit(query);
	}

	xpathQuery() {
		let query = new FemmeQuery();
		query.xpath = this.xpathTerm.value;
		this.queryEvent.emit(query);
	}

	stopProp(event: any) {
		event.stopPropagation();
	}

	filterButtonClick(event: any) {
		event.stopPropagation();
		this.showFields = this.showFields === 'hide' ? 'show' : 'hide';
	}
}
