import { Component, OnInit, Input, OnDestroy } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';

import { DataElement } from './../../model/data-element';
import { FemmeQueryService } from './../../femme-services/femme-query.service';

@Component({
	selector: 'data-element-info',
	templateUrl: './data-element-info.component.html',
	styleUrls: ['./data-element-info.component.css']
})
export class DataElementInfoComponent implements OnInit {

	@Input()
	dataElement: DataElement;
	errorMessage: string;

	private id: string;
	private sub: Subscription;

	constructor(private femmeService: FemmeQueryService) { }

	ngOnInit() {

	}

	private getDataElementInfo() {
		this.femmeService.getDataElementInfo(this.id)
			.subscribe(
				dataElement => this.dataElement = dataElement,
				error => this.errorMessage = <any>error);

	}
}
