import { FemmeQuery } from './../models/femme-query';
import { DataElementList } from './../models/data-element-list';
import { environment } from '@env/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { DataElement } from '@app/models/data-element';
import { FemmeResponse } from '@app/models/femme-response';

@Injectable({
	providedIn: 'root'
})
export class FemmeService {
	private femmeBaseUrl: string = environment.femmeBaseUrl;
	private femmeDataElementsUrl: string = environment.femmeBaseUrl + environment.femmeDataElementsEndpoint;

	constructor(private http: HttpClient) { }

	countDataElements(xpath: string): Observable<number> {
		let xpathQueryParam = xpath ? `&xpath=${xpath}` : "";
		return this.http.get<FemmeResponse<number>>(`${this.femmeDataElementsUrl}?options={"limit":0}${xpathQueryParam}`).pipe(
			map(response => response.entity.body)
		);
	}

	getDataElements(offset?: number, limit?: number, query?: FemmeQuery): Observable<DataElement[]> {
		console.log("AAAAAAAAa");
		console.log(query);
		let xpathQueryParam = "";
		if (query) {
			if (query.xpath) {
				xpathQueryParam = `&xpath=${query.xpath}`;
			} else if (query.ids) {
				return this.submitDataElementRequest(`/${query.ids[0]}`);
			}
		}

		let limitQueryParam = `"limit":${limit}`;
		let offsetQueryParam = `"offset":${offset}`;
		let optionsQueryParam = `?options={${limitQueryParam},${offsetQueryParam}}`;

		let queryParams = `${optionsQueryParam}${xpathQueryParam}`;

		return this.submitDataElementsRequest(queryParams);
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

	getDataElement(id: string): Observable<DataElement> {
		return this.http.get<DataElement>(`${this.femmeDataElementsUrl}/id`);
	}
}
