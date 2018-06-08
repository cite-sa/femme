import { Component, OnInit, ViewChild, AfterViewInit, Input } from '@angular/core';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { DataSource } from '@angular/cdk/collections';
import { MatPaginator, MatDialog } from '@angular/material';

import { catchError, finalize, tap } from 'rxjs/operators';

import { FemmeElementDialogComponent } from '@app/elements-view/femme-element-dialog.component';
import { FemmeService } from '@app/services/femme.service';
import { Collection } from '@app/models/collection';
import { DataElement } from '@app/models/data-element';
import { FemmeQuery } from '@app/models/femme-query';

@Component({
	selector: 'femme-elements-table',
	templateUrl: './femme-elements-table.component.html',
	styleUrls: ['./femme-elements-table.component.css']
})
export class FemmeElementsTableComponent implements OnInit, AfterViewInit {
	dataSource = new FemmeDataSource(this.femmeService);
	displayedColumns = ['name', 'created', 'modified'];

	private totalSubject = new BehaviorSubject<number>(100);
	total$ = this.totalSubject.asObservable();

	@ViewChild(MatPaginator) paginator: MatPaginator;

	@Input()
	query$: Observable<FemmeQuery>;
	query: FemmeQuery;

	constructor(private femmeService: FemmeService, private dialog: MatDialog) { }

	ngOnInit() {
		// this.dataSource.loadDataElements(0, 10);
		this.query$.subscribe(q => {
			this.query = q;
			this.paginator.pageIndex = 0;
			this.loadDataElementsPage();
		});
	}

	ngAfterViewInit() {
		this.paginator.page.pipe(
			tap(() => this.loadDataElementsPage())
		).subscribe();
	}

	loadDataElementsPage() {
		this.getDataElementCount(this.query, () => this.dataSource.loadDataElements(this.paginator.pageIndex * this.paginator.pageSize, this.paginator.pageSize, this.query));
		console.log("pageIndex: " + this.paginator.pageIndex + ", pageSize: " + this.paginator.pageSize);
        // this.dataSource.loadDataElements(this.paginator.pageIndex * this.paginator.pageSize, this.paginator.pageSize, query);
	}

	private getDataElementCount(query?: FemmeQuery, dataElementsQueryCallback?: Function) {
		if (query != undefined) {
			if (query.xpath != undefined && query.xpath != "") {
				this.getTotalDataElements(query.xpath, dataElementsQueryCallback);
			} else if (query.ids != undefined && query.ids.length > 0) {
				this.totalSubject.next(query.ids.length)
				dataElementsQueryCallback();
			} else {
				this.getTotalDataElements(undefined, dataElementsQueryCallback);
			}
		} else {
			this.getTotalDataElements(undefined, dataElementsQueryCallback);
		}
	}

	getTotalDataElements(xpath?: string, dataElementsQueryCallback?: Function) {
		this.femmeService.countDataElements(xpath).pipe(
			catchError(() => of(0)),
		).subscribe(total => {
			this.totalSubject.next(total);
			dataElementsQueryCallback();
		});
	}

	openElementDialog(element: DataElement) {
		console.log(element);
		this.dialog.open(FemmeElementDialogComponent, {
			height: '80%',
			data: {
				element: element
			}
		});
	}

	stopProp(event: any) {
		event.stopPropagation();
	}
}

export class FemmeDataSource extends DataSource<DataElement> {
	private dataElementsSubject = new BehaviorSubject<DataElement[]>([]);
	private loadingSubject = new BehaviorSubject<boolean>(false);
	public loading$ = this.loadingSubject.asObservable();

	constructor(private femmeService: FemmeService) {
		super();
	}

	connect(): Observable<DataElement[]> {
		return this.dataElementsSubject.asObservable();
	}

	disconnect() {
		this.dataElementsSubject.complete();
		this.loadingSubject.complete();
	}

	loadDataElements(offset?: number, limit?: number, query?: FemmeQuery) {
		this.loadingSubject.next(true);

		this.femmeService.getDataElements(offset, limit, query).pipe(
			catchError(() => of([])),
			finalize(() => this.loadingSubject.next(false))
		).subscribe((dataElements: DataElement[]) => {
			dataElements.forEach((dataElement: DataElement) => {
				dataElement.collections.forEach(dataElementCollection => {
					this.femmeService.getCollection(dataElementCollection.id).subscribe(collection => {
						dataElementCollection.name = collection.name;
					})
				})
			})
			this.dataElementsSubject.next(dataElements);
		});
	}
}