import { Observable } from 'rxjs/Observable';
import { Http, Response } from '@angular/http';
import { Injectable } from '@angular/core';

@Injectable()
export class MetadataRetrievalService {

	constructor (private http: Http) { }

	retrieve(url: string): Observable<Response> {
		return this.http.get(url);
				// .catch(this.handleError);
	}

	private extractData(response: Response) {
		console.log(response);
		return response.text();
	}

	private handleError (error: Response | any) {
	// In a real world app, you might use a remote logging infrastructure
	let errMsg: string;
	if (error instanceof Response) {
		const body = error.json() || '';
	} else {
		errMsg = error.message ? error.message : error.toString();
	}

	console.error(errMsg);
	return Observable.throw(errMsg);
	}

}
