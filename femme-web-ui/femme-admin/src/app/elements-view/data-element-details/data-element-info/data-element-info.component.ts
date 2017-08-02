import { SystemicMetadata } from './../../../model/systemic-metadata';
import { Component, OnInit, Input, OnDestroy, OnChanges } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';

import { DataElement } from '../../../model/data-element';
import { FemmeQueryService } from '../../../femme-services/femme-query.service';

@Component({
	selector: 'data-element-info',
	templateUrl: './data-element-info.component.html',
	styleUrls: ['./data-element-info.component.css']
})
export class DataElementInfoComponent implements OnInit, OnChanges {

	@Input()
	dataElement: DataElement;
	errorMessage: string;

	private id: string;
	private sub: Subscription;

	status: boolean;

	constructor(private femmeService: FemmeQueryService) { }

	ngOnInit() {
		// if (this.dataElement.systemicMetadata.status == "ACTIVE") {
		// 	this.status = true;
		// } else if (this.dataElement.systemicMetadata.status == "INACTIVE") {
		// 	this.status = false;
		// }
	}

	ngOnChanges() {
		console.log(this.dataElement);
		console.log(this.dataElement.systemicMetadata);

		if (this.dataElement.systemicMetadata != undefined) {
			if (this.dataElement.systemicMetadata.status == "ACTIVE") {
				this.status = true;
			} else if (this.dataElement.systemicMetadata.status == "INACTIVE") {
				this.status = false;
			}
		}
	}

	// private getDataElementInfo() {
	// 	this.femmeService.getDataElementInfo(this.id)
	// 		.subscribe(
	// 			dataElement => {
	// 				console.log(dataElement);
	// 				this.dataElement = dataElement
	// 			},
	// 			error => this.errorMessage = <any>error);

	// }

	onSubmit() {
		this.femmeService.updateDataElementInfo(this.prepareSaveDataElement())
			.subscribe(
				response => console.log(response),
				error => console.log(error),
		);
	}

	prepareSaveDataElement(): DataElement {
		let dataElementUpdate = new DataElement();
		dataElementUpdate.id = this.dataElement.id;
		dataElementUpdate.name = this.dataElement.name;

		dataElementUpdate.systemicMetadata = new SystemicMetadata();
		dataElementUpdate.systemicMetadata.status = this.dataElement.systemicMetadata.status;

		console.log(dataElementUpdate);

		// console.log(this.metadataArray);
		// return this.metadataArray.controls.filter(metadatum => !metadatum.pristine).map(metadatumForm => {
		// 	let metadatum: Metadatum;
		// 	if (metadatumForm.value.endpoint && !metadatumForm.value.value) {
		// 		this.metadataRetrievalService.retrieve(metadatumForm.value.endpoint)
		// 		.subscribe(
		// 			response => {
		// 				console.log(response);
		// 				metadatumForm.patchValue({
		// 					value: response.text(),
		// 					contentType: response.headers.get("content-type")
		// 				});
		// 				metadatum = this.tranformMetadatumFormToMetadatum(metadatumForm.value);
		// 			},
		// 			error => console.log(error)
		// 		);
		// 	} else {
		// 		metadatum = this.tranformMetadatumFormToMetadatum(metadatumForm.value);
		// 	}

		// 	return metadatum;
		// });
		return dataElementUpdate;
	}
}
