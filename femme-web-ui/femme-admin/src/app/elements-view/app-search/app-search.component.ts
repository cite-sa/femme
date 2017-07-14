import { Component, OnInit, Output, EventEmitter, OnDestroy } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Operator } from 'rxjs/Operator';
import { Subject } from 'rxjs/Subject';
import { Subscription } from 'rxjs/Subscription';
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/distinctUntilChanged';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/switchMap';

import { DataElement } from './../../model/data-element';
import { FemmeQueryService } from './../../femme-services/femme-query.service';
import { FulltextResult } from '../../model/fulltext-result';
import { FulltextField } from '../../model/fulltext-field';
import { FulltextQuery } from '../../model/fulltext-query';

// TODO: Remove this when a stable release of RxJS without the bug is available.
// declare module 'rxjs/Subject' {
//   interface Subject<T> {
//     lift<R>(operator: Operator<T, R>): Observable<R>;
//   }
// }


@Component({
	selector: 'femme-app-search',
	templateUrl: './app-search.component.html',
	styleUrls: ['./app-search.component.css']
})
export class AppSearchComponent implements OnInit, OnDestroy {

	isXpath: boolean = false;
	xpathValue: string;

	errorMessage: string;
	searching: boolean;
	searchFailed: boolean;

	@Output() onResult = new EventEmitter<Array<DataElement>>();

	private searchTermStream = new Subject<string>();
	private dataElementsSubscription: Subscription;

	constructor(private femmeService: FemmeQueryService) { }

	ngOnInit() {
		this.listDataElements();

		this.dataElementsSubscription = this.searchTermStream
			.debounceTime(500)
			.distinctUntilChanged()
			.switchMap((term: string) => {
				let fulltextQuery = new FulltextQuery;
				fulltextQuery.metadataField = new FulltextField("name", term);

				let dataElements: Array<DataElement> = new Array<DataElement>();

				return this.femmeService.autocomplete(fulltextQuery);
			}).subscribe(
				fulltextResults => {
					let dataElements: Array<DataElement> = new Array<DataElement>();
					fulltextResults.forEach(result => {
						let dataElement: DataElement = new DataElement();
						dataElement.id = result.elementId;
						dataElement.name = result.name;
						dataElements.push(dataElement);
					});
					this.onResult.emit(dataElements);
				},
				error => this.errorMessage = <any>error);
	}

	ngOnDestroy() {
		this.dataElementsSubscription.unsubscribe();
	}

	search(term: string): void {
		if (this.isXpath) {
			return;
		}

		if (term === '') {
			this.listDataElements();
			return;
		}

		this.searchTermStream.next(term);
	}

	xpath(term: string): void {
		if (!this.isXpath) {
			return;
		}

		if (term === '') {
			this.listDataElements();
			return;
		}

		this.femmeService.queryDataElements(term)
			.subscribe(dataElements => {
				this.onResult.emit(dataElements);
			}, error => this.errorMessage = <any>error);
	}

	private listDataElements(): void {
		this.femmeService.listDataElements()
			.subscribe(dataElements => {
				this.onResult.emit(dataElements);
			}, error => this.errorMessage = <any>error);
	}
}

// autocomplete(query: FulltextQuery): void {
// 		let dataElements: Array<DataElement> = new Array<DataElement>();
// 		this.femmeService.autocomplete(query).subscribe(
// 			fulltextResults => {
// 				fulltextResults.forEach(result => {
// 					let dataElement: DataElement = new DataElement();
// 					dataElement.id = result.elementId;
// 					dataElement.name = result.name;
// 					dataElements.push(dataElement);
// 				});
// 				this.dataElements = dataElements;
// 				this.subject.next(dataElements);
// 			},
// 			error => this.errorMessage = <any>error);
// 	}
