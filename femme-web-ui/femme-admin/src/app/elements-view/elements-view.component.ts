import { DataElement } from './../model/data-element';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-elements-view',
  templateUrl: './elements-view.component.html',
  styleUrls: ['./elements-view.component.css']
})
export class ElementsViewComponent implements OnInit {

	dataElements: Array<DataElement>;

	constructor() { }

	ngOnInit() { }

	getDataElements(dataElements: Array<DataElement>) {
		this.dataElements = dataElements;
	}

}
