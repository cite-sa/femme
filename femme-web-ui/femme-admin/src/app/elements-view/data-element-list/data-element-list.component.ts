import { Component, OnInit, Input } from '@angular/core';
import { DataElement } from '../../model/data-element';
import { FemmeQueryService } from '../../femme-services/femme-query.service';


@Component({
	selector: 'femme-data-element-list',
	templateUrl: './data-element-list.component.html',
	styleUrls: ['./data-element-list.component.css']
})

export class DataElementListComponent {
	@Input() dataElements: Array<DataElement>;
	@Input() errorMessage: string;

	constructor() { }

}
