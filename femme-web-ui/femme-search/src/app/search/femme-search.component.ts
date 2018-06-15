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
	expansionTypes = ["Broader", "Narrower", "Related"];

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

	constructor(private searchService: FemmeSearchService, private fb: FormBuilder) {
		this.fulltextQueryForm = this.fb.group({
			searchTerm: '',
			expansionType: ''
		  });

		this.fulltextQueryForm.valueChanges
			.pipe(debounceTime(400))
			.subscribe(query => {
				console.log("VALUE CHANGED");
				console.log(query);

				let term: any = query.searchTerm;
				let expansionType: string = query.expansionType ? query.expansionType.toLowerCase() : undefined;

				if (typeof term !== "string") {
					if (term.fulltext) {
						term = term.fulltext.name;
					}
				}

				console.log(term);

				if (typeof term === "string") {
					this.loading = true;
					this.searchService.search(this.searchField, term, expansionType)
					.subscribe(
						(results) => this.searchResults = results,
						(error) => console.log(error),
						() => {
							this.loading = false;
							this.autoTrigger.openPanel();
						}
					);
				}
			});
	}

	elementToDisplayValue(element: FulltextSearchResult) {
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
		if (event.option.value.fulltext) {
			this.searchExact(event.option.value.fulltext.name, () => this.loading = false);
		} else {
			this.searchExact(event.option.value.name, () => {
				this.loading = false;
			});
		}
	}

	fulltextQuery() {
		let term: any = this.fulltextQueryForm.value.searchTerm;
		console.log("SUBMIT");
		console.log(term);

		if (term == undefined) {
			this.emitQueryEvent();
		} else {
			let searchTerm: string;
			searchTerm = typeof term === "string" ? term : term.fulltext["name"];
			this.searchExact(searchTerm);
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
