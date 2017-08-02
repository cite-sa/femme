import { FulltextResult } from '../model/fulltext-result';
import { FulltextQuery } from '../model/fulltext-query';
import { FemmeQueryService } from './femme-query.service';
import { DataElement } from '../model/data-element';
import { Injectable, OnInit } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';

@Injectable()
export class ElementsService {

	private dataElements: Array<DataElement>;
	private subject: Subject<Array<DataElement>> = new Subject<Array<DataElement>>();

	errorMessage: string;

	constructor(private femmeService: FemmeQueryService) { }

	autocomplete(query: FulltextQuery): void {
		let dataElements: Array<DataElement> = new Array<DataElement>();
		this.femmeService.autocomplete(query).subscribe(
			fulltextResults => {
				fulltextResults.forEach(result => {
					let dataElement: DataElement = new DataElement();
					dataElement.id = result.elementId;
					dataElement.name = result.name;
					dataElements.push(dataElement);
				});
				this.dataElements = dataElements;
				this.subject.next(dataElements);
			},
			error => this.errorMessage = <any>error);
	}

	listDataElements(): void {
		this.femmeService.listDataElements().subscribe(
			dataElements => {
				this.dataElements = dataElements
				this.subject.next(dataElements);
			},
			error => this.errorMessage = <any>error);
	}

	getDataElements(): Observable<Array<DataElement>> {
		console.log("Get data elements");
		return this.subject.asObservable();
	}

}
