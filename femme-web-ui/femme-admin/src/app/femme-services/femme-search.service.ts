import { FulltextResult } from './../model/fulltext-result';
import { FulltextQuery } from './../model/fulltext-query';
import { Component, Injectable } from '@angular/core';
import { Http, Response, RequestOptions, Headers } from '@angular/http';

import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';

import { Element } from '../model/element';

@Injectable()
export class FemmeSearchService {
	private femmeUrl = 'http://localhost:8081/fulltext-application-devel/elements';

	constructor (private http: Http) {}

	autocomplete(query: FulltextQuery): Observable<Array<FulltextResult>> {
		let headers = new Headers({ 'Content-Type': 'application/json' });
    	let options = new RequestOptions({ headers: headers });

		console.log(JSON.stringify(query));

		return this.http.post(this.femmeUrl, JSON.stringify(query), options)
			.map(response => {
				console.log(response.json());
				return response.json()
			});
	}

	// private handleError (error: Response | any) {
	// 	let errMsg: string;
	// 	if (error instanceof Response) {
	// 		const body = error.json() || '';
	// 		const err = body.error || JSON.stringify(body);
	// 		errMsg = `${error.status} - ${error.statusText || ''} ${err}`;
	// 	} else {
	// 		errMsg = error.message ? error.message : error.toString();
	// 	}
	// 	console.error(errMsg);
	// 	return Observable.throw(errMsg);
	// }
}
