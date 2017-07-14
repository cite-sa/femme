import { SystemicMetadata } from './../../model/systemic-metadata';
import { MetadataRetrievalService } from './../../femme-services/metadata-retrieval.service';
import { FormGroup, FormArray, FormBuilder } from '@angular/forms';
import { Metadatum } from './../../model/metadatum';
import { FemmeQueryService } from './../../femme-services/femme-query.service';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';
import { Component, OnInit, OnChanges, Input } from '@angular/core';

@Component({
	selector: 'data-element-metadata',
	templateUrl: './data-element-metadata.component.html',
	styleUrls: ['./data-element-metadata.component.css']
})
export class DataElementMetadataComponent {

	@Input()
	dataElementId: string;
	@Input()
	dataElementMetadata: Array<Metadatum>;
	metadataForm: FormGroup;

	dataElementStatus: boolean;

	constructor(private fb: FormBuilder, private femmeService: FemmeQueryService, private metadataRetrievalService: MetadataRetrievalService) {
		this.createMetadataForm();
	}

	// onSubmit(value) {
	// 	console.log(value);
	// 	console.log(this.dataElementMetadata);
	// }

	// createMetadatum() {
	// 	this.dataElementMetadata.push(new Metadatum);
	// }




	ngOnChanges() {
		// this.metadataForm.reset({
		// 	metadata: this.metadata
		// });
		// this.setMetadata(this.metadata);

		const control = <FormArray>this.metadataForm.controls['metadata'];
		this.dataElementMetadata.forEach(metadatum => control.push(this.createAndPopulateMetadatumForm(metadatum)));
	}

	// revert() {
	// 	this.ngOnChanges();
	// }

	private createMetadataForm() {
		this.metadataForm = this.fb.group({
			metadata: this.fb.array([])
		});
	}

	private createMetadatumForm(): FormGroup {
		return this.fb.group({
			id: [''],
			name: [''],
            endpoint: [''],
			value: [''],
			contentType: [''],
			status: ['']
        });
	}

	private createAndPopulateMetadatumForm(metadatum: Metadatum): FormGroup {
		if (metadatum.systemicMetadata.status == "ACTIVE") {
			this.dataElementStatus = true;
		} else if (metadatum.systemicMetadata.status == "INACTIVE") {
			this.dataElementStatus = false;
		}

		let metadatumForm: FormGroup = this.createMetadatumForm();
		metadatumForm.setValue({
			id: metadatum.id,
			name: metadatum.name,
			endpoint: metadatum.endpoint,
			value: metadatum.value,
			contentType: metadatum.contentType,
			status: this.dataElementStatus
		});
		return metadatumForm;
	}

	setMetadata(metadata: Array<Metadatum>) {
    	const metadataFormGroups = metadata.map(metadatum => this.createMetadatumForm());
    	const metadataFormArray = this.fb.array(metadataFormGroups);
    	this.metadataForm.setControl('metadata', metadataFormArray);
  }

	addMetadatumForm() {
    	this.metadataArray.push(this.createMetadatumForm());
	}

	removeMetadatumForm(event, index) {
		this.metadataArray.removeAt(index);
		event.preventDefault();
	}

	get metadataArray(): FormArray {
		return this.metadataForm.get('metadata') as FormArray;
	}

	onSubmit() {
		let metadataToUpdate: Array<Metadatum> = this.prepareSaveMetadata();
		console.log(metadataToUpdate);
		metadataToUpdate.forEach(metadatum =>
				this.femmeService.updateDataElementMetadatum(this.dataElementId, metadatum)
					.subscribe(
						entity => console.log(entity),
						error => console.log(error)
					)
			);

		// let metadatumIds: Array<string> = metadataToUpdate.map(metadatum => metadatum.id);
		// this.dataElementMetadata.filter(metadatum => !metadatumIds.includes(metadatum.id))
		// 	.forEach(metadatum => this.femmeService.deleteDataElementMetadatum(this.dataElementId, metadatum.id)
		// 			.subscribe(
		// 				message => console.log(message),
		// 				errorMessage => console.log(errorMessage)
		// 			)
		// 	);
		// this.ngOnChanges();
	}

	prepareSaveMetadata(): Array<Metadatum> {
		console.log(this.metadataArray);
		return this.metadataArray.controls.filter(metadatum => !metadatum.pristine).map(metadatumForm => {
			let metadatum: Metadatum;
			if (metadatumForm.value.endpoint && !metadatumForm.value.value) {
				this.metadataRetrievalService.retrieve(metadatumForm.value.endpoint)
				.subscribe(
					response => {
						console.log(response);
						metadatumForm.patchValue({
							value: response.text(),
							contentType: response.headers.get("content-type")
						});
						metadatum = this.tranformMetadatumFormToMetadatum(metadatumForm.value);
					},
					error => console.log(error)
				);
			} else {
				metadatum = this.tranformMetadatumFormToMetadatum(metadatumForm.value);
			}

			return metadatum;
		});
	}

	tranformMetadatumFormToMetadatum(metadatumFormValue): Metadatum {
		let metadatum: Metadatum = new Metadatum();

		metadatum.id = metadatumFormValue.id;
		metadatum.name = metadatumFormValue.name;
		metadatum.endpoint = metadatumFormValue.endpoint;
		metadatum.value = metadatumFormValue.value;
		metadatum.contentType = metadatumFormValue.contentType;

		metadatum.systemicMetadata = new SystemicMetadata();
		metadatumFormValue.status ? metadatum.systemicMetadata.status = "ACTIVE" : "INACTIVE"

		return metadatum;
	}


	changeDataElementStatus(metadatumForm: FormGroup, $event) {
		this.dataElementStatus = !this.dataElementStatus;
		metadatumForm.patchValue({
			status: this.dataElementStatus
		});

		metadatumForm.markAsDirty();


		$event.preventDefault();
		$event.stopPropagation();
	}

}
