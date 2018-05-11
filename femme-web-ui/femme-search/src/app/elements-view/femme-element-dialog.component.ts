import { Component, Input, Inject, OnInit, AfterViewInit, ElementRef, ViewChild } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';

import { DataElement } from '@app/models/data-element';

@Component({
	selector: 'femme-element',
	templateUrl: './femme-element-dialog.component.html',
	styleUrls: ['./femme-element-dialog.component.css']
})
export class FemmeElementDialogComponent {
	constructor(@Inject(MAT_DIALOG_DATA) public data: any) { }
} 