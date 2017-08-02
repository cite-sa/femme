import { Subscription } from 'rxjs/Subscription';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { Observable } from 'rxjs/Observable';

import { DataElement } from '../../model/data-element';
import { Metadatum } from '../../model/metadatum';
import { FemmeQueryService } from '../../femme-services/femme-query.service';

@Component({
  selector: 'data-element-details',
  templateUrl: './data-element-details.component.html',
  styleUrls: ['./data-element-details.component.css']
})
export class DataElementDetailsComponent implements OnInit, OnDestroy {
	private id: string;
	dataElement = new DataElement();
	metadata: Array<Metadatum>;
	errorMessage: string;

	isCollapsed: boolean;

	private sub: Subscription;

	constructor(private route: ActivatedRoute, private femmeService: FemmeQueryService) {
		// this.createMetadataForm();
	}

	ngOnInit() {
		this.sub = this.route.params.subscribe(params => {
			this.id = params['id'];
		 	this.getDataElement();
		});
	}

	ngOnDestroy() {
		this.sub.unsubscribe();
	}

	private getDataElement() {
		this.femmeService.getDataElement(this.id)
			.subscribe(
				dataElement => {
					// console.log(dataElement);
					this.dataElement = dataElement
				},
				error => this.errorMessage = <any>error);

	}

	// onSelectChange($event) {
	// 	if ($event.index == 1) {
	// 		this.getMetadata();
	// 	}
	// }

	// onSubmit(value) {
	// 	console.log(value);
	// }


	// createMetadatum() {
	// 	let metadatum: Metadatum = new Metadatum();
	// 	this.dataElement.metadata.push(metadatum);
	// }

}
