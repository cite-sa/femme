import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@env/environment';
import { map } from 'rxjs/operators';
import { Observable } from 'rxjs';

import { FemmeQuery } from '@app/models/femme-query';
import { Collection } from '@app/models/collection';
import { DataElement } from '@app/models/data-element';
import { DataElementList } from './../models/data-element-list';
import { FemmeResponse } from '@app/models/femme-response';

@Injectable({
	providedIn: 'root'
})
export class FemmeService {
	private femmeBaseUrl: string = environment.femmeBaseUrl;

	private femmeCollectionsUrl: string = environment.femmeBaseUrl + environment.femmeCollectionsEndpoint;
	private femmeDataElementsUrl: string = environment.femmeBaseUrl + environment.femmeDataElementsEndpoint;

	constructor(private http: HttpClient) { }

	countDataElements(xpath?: string): Observable<number> {
		let xpathQueryParam = xpath ? `&xpath=${xpath}` : "";
		return this.http.get<FemmeResponse<number>>(`${this.femmeDataElementsUrl}?options={"limit":0}${xpathQueryParam}`).pipe(map(response => response.entity.body));
	}

	getDataElements(offset?: number, limit?: number, query?: FemmeQuery): Observable<DataElement[]> {
		let limitQueryParam = `"limit":${limit}`;
		let offsetQueryParam = `"offset":${offset}`;
		let optionsQueryParam = `?options={${limitQueryParam},${offsetQueryParam}}`;

		let xpathQueryParam = "";
		if (query != undefined) {

			if (query.xpath!= undefined && query.xpath != "") {
				console.log("xpath");
				console.log(query.xpath);
				xpathQueryParam = `&xpath=${query.xpath}`;
				return this.submitDataElementsRequest(`${optionsQueryParam}${xpathQueryParam}`);

			} else if (query.ids != undefined && query.ids.length > 0) {
				console.log("ids");
				console.log(query.ids);
				// let ids = JSON.stringify(query.ids.join('&id=')).replace('"', '').replace('"', '');
				return this.submitDataElementListRequest(`/list${optionsQueryParam}`, query.ids);
			} else {
				return this.submitDataElementsRequest(`${optionsQueryParam}`);
			}
		} else {
			return this.submitDataElementsRequest(`${optionsQueryParam}`);
		}
	}

	getCollection(id: string): Observable<Collection> {
		return this.http.get<FemmeResponse<Collection>>(`${this.femmeCollectionsUrl}`).pipe(
			map(response => response.entity.body)
		);
	}

	private submitDataElementRequest(request: string) {
		return this.http.get<FemmeResponse<DataElement>>(`${this.femmeDataElementsUrl}${request}`).pipe(
			map(response => new Array<DataElement>(response.entity.body))
		);
	}

	private submitDataElementsRequest(request: string) {
		return this.http.get<FemmeResponse<DataElementList>>(`${this.femmeDataElementsUrl}${request}`).pipe(
			map(response => response.entity.body.elements)
		);
	}

	private submitDataElementListRequest(request: string, ids: string[]) {
		return this.http.post<FemmeResponse<DataElementList>>(`${this.femmeDataElementsUrl}${request}`, ids).pipe(
			map(response => response.entity.body.elements)
		);
	}

	getDataElement(id: string): Observable<DataElement> {
		return this.http.get<DataElement>(`${this.femmeDataElementsUrl}/${id}`);
	}
}
