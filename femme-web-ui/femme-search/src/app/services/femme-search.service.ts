import { environment } from '@env/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map } from 'rxjs/operators';
import { Observable } from 'rxjs';

import { FulltextSearchResult } from '@app/models/fulltext-search-result';

@Injectable({
	providedIn: 'root'
})
export class FemmeSearchService {
	private femmeSearchBaseUrl: string = environment.femmeSearchBaseUrl;
	private femmeSearchUrl: string = environment.femmeSearchBaseUrl + environment.femmeSearchEndpoint;

	constructor(private http: HttpClient) { }

	search(searchField: string, searchTerm: string, expansionType: string): Observable<Array<FulltextSearchResult>> {
		console.log(searchField);
		let query = {
			expand: expansionType,
			autocompleteField: {
				"field": searchField,
				"value": searchTerm
			}
		}

		return this.http.post<Array<FulltextSearchResult>>(`${this.femmeSearchUrl}`, query);
	}
}
