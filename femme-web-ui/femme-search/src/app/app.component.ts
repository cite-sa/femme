import { trigger } from '@angular/animations';
import { FemmeQuery } from './models/femme-query';
import { BehaviorSubject } from 'rxjs';
import { Component, EventEmitter } from '@angular/core';

@Component({
	selector: 'app-root',
	templateUrl: './app.component.html',
	styleUrls: []
})
export class AppComponent {
	title = 'app';
	private querySubject = new BehaviorSubject<FemmeQuery>(new FemmeQuery);
	query$ = this.querySubject.asObservable();

	private loadingSubject = new BehaviorSubject<boolean>(false);
	loading$ = this.loadingSubject.asObservable();

	submitQuery(query: FemmeQuery) {
		console.log(query);
		this.querySubject.next(query);
	}

	triggerTableLoader(loading: boolean) {
		console.log("out " + loading);
		this.loadingSubject.next(loading);
	}
}
