import { FemmeQuery } from './../models/femme-query';
import { Component, OnInit, ViewChild, AfterViewInit, Input } from '@angular/core';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { DataSource } from '@angular/cdk/collections';

import { DataElement } from '@app/models/data-element';
import { FemmeService } from '@app/services/femme.service';
import { catchError, finalize, tap } from 'rxjs/operators';
import { MatPaginator, MatDialog } from '@angular/material';
import { FemmeElementDialogComponent } from '@app/elements-view/femme-element-dialog.component';

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
	query: Observable<FemmeQuery>;

	constructor(private femmeService: FemmeService, private dialog: MatDialog) { }

	ngOnInit() {
		this.dataSource.loadDataElements(0, 10);
		this.query.subscribe(q => this.loadDataElementsPage(q));
	}

	ngAfterViewInit() {
		this.paginator.page.pipe(
			tap(() => this.loadDataElementsPage())
		).subscribe();
	}

	loadDataElementsPage(query?: FemmeQuery) {
		if (query) {
			if (query.xpath) {
				let xpath = query ? query.xpath : undefined;
				this.getTotalDataElements(xpath);
			} else if (query.ids) {
				this.totalSubject.next(query.ids.length)
			} else {
				this.getTotalDataElements();
			}
		}

        this.dataSource.loadDataElements(this.paginator.pageIndex * this.paginator.pageSize, this.paginator.pageSize, query);
	}

	getTotalDataElements(xpath?: string) {
		this.femmeService.countDataElements(xpath).pipe(
			catchError(() => of(0)),
		).subscribe(total => this.totalSubject.next(total));
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
		).subscribe(dataElements => this.dataElementsSubject.next(dataElements));
	}
}