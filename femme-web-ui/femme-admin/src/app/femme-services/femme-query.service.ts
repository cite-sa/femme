import { Metadatum } from './../model/metadatum';
import { FulltextQuery } from './../model/fulltext-query';
import { FulltextResult } from './../model/fulltext-result';
import { FulltextField } from './../model/fulltext-field';
import { FemmeResponse } from './../model/femme-response';
import { Injectable } from '@angular/core';
import { Http, Response, RequestOptions, Headers, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';

import { DataElement } from './../model/data-element';

@Injectable()
export class FemmeQueryService {
	private femmeUrl = 'http://localhost:8080/femme-application-devel/';
	private femmeAdminUrl = this.femmeUrl + 'admin/';
	private femmeFulltextUrl = 'http://localhost:8081/fulltext-application-devel/elements';
	private headers = new Headers({ 'Content-Type': 'application/json' });

	dataElements: Array<DataElement>;

	constructor (private http: Http) { }

	getDataElement(id: string): Observable<DataElement> {
		let params = new URLSearchParams();
		params.set('pretty', 'true');
		params.set('loadInactiveMetadata', 'true');

    	let requestOptions = new RequestOptions({
			headers: this.headers,
			params: params
		});

		return this.http.get(this.femmeUrl + "dataElements/" + id, requestOptions)
			.map(response => response.json().entity.body);
	}

	getDataElementInfo(id: string): Observable<DataElement> {
		let queryOptions = {
			exclude: ['metadata']
		};
 		let params = new URLSearchParams();
 		params.set('options', JSON.stringify(queryOptions));

    	let requestOptions = new RequestOptions({
			headers: this.headers,
			params: params
		});

		return this.http.get(this.femmeUrl + "dataElements/" + id, requestOptions)
			.map(response => response.json().entity.body);
	}

	updateDataElementInfo(dataElement: DataElement): Observable<DataElement> {
 		let requestOptions = new RequestOptions({
			headers: this.headers
		});

		return this.http.post(this.femmeAdminUrl + 'dataElements/' + dataElement.id, JSON.stringify(dataElement), requestOptions)
				.map(response => response.json().entity);
	}

	getDataElementMetadata(dataElementId: string): Observable<Array<Metadatum>> {
    	let queryOptions = {
			include: ['metadata']
		};
 		let params = new URLSearchParams();
 		params.set('options', JSON.stringify(queryOptions));

    	let requestOptions = new RequestOptions({
			headers: this.headers,
			params: params
		});

		return this.http.get(this.femmeUrl + 'dataElements/' + dataElementId + '/metadata', requestOptions)
			.map(response => response.json().entity.body.metadata);
	}

	updateDataElementMetadatum(dataElementId: string, metadatum: Metadatum): Observable<Metadatum> {
    	let requestOptions = new RequestOptions({
			headers: this.headers
		});

		return this.http.post(this.femmeAdminUrl + 'dataElements/' + dataElementId + '/metadata' + (metadatum.id ? ('/' + metadatum.id) : ''), JSON.stringify(metadatum), requestOptions)
				.map(response => response.json().entity);
	}

	deleteDataElementMetadatum(dataElementId: string, metadatumId: string): Observable<string> {
		return this.http.delete(this.femmeAdminUrl + 'dataElements/' + dataElementId + '/metadata/' + metadatumId)
				.map(response => response.json().message);
	}

	listDataElements(): Observable<Array<DataElement>> {
		let queryOptions = {
			limit: 5,
			include: ['id','name','collections']
		};
 		let params = new URLSearchParams();
 		params.set('options', JSON.stringify(queryOptions));

    	let requestOptions = new RequestOptions({
			headers: this.headers,
			params: params
		});

		return this.http.get(this.femmeUrl + 'dataElements', requestOptions)
			.map(response => response.json().entity.body.dataElements);
	}

	queryDataElements(xpath: string): Observable<Array<DataElement>> {
		let queryOptions = {
			limit: 5,
			include: ['id','name','collections']
		};
 		let params = new URLSearchParams();
 		params.set('options', JSON.stringify(queryOptions));
		params.set('xpath', xpath);

    	let requestOptions = new RequestOptions({
			headers: this.headers,
			// search: new URLSearchParams('options=${queryOptions}&xpath=${xpath}'),
			params: params
			// params: params
		});
		console.log(requestOptions);

		return this.http.get(this.femmeUrl + 'dataElements', requestOptions)
			.map(response => response.json().entity.body.dataElements);
	}

	private extractData(response: Response) {
		let body = response.json();
		return body.data || { };
	}

	autocomplete(query: FulltextQuery): Observable<Array<FulltextResult>> {
		let headers = new Headers({ 'Content-Type': 'application/json' });
    	let options = new RequestOptions({ headers: headers });

		console.log(JSON.stringify(query));

		return this.http.post(this.femmeFulltextUrl, JSON.stringify(query), options)
			.map(response =>  response.json());
	}
}
