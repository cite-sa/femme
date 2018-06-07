import { SpinnerComponent } from '@app/shared/spinner/spinner.component';
import { BrowserModule } from "@angular/platform-browser";
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from "@angular/common/http";
import { NgModule } from "@angular/core";
import { ReactiveFormsModule, FormsModule } from "@angular/forms";

import { AppRoutingModule } from "@app/app-routing.module";
import { AppComponent } from "@app/app.component";
import { MaterialModule } from "@app/material.module";

import { FemmeNavbarComponent } from '@app/navbar/femme-navbar.component';
import { FemmeSearchComponent } from '@app/search/femme-search.component';
import { FemmeSearchService } from '@app/services/femme-search.service';
import { FemmeService } from "@app/services/femme.service";
import { FemmeElementDialogComponent } from '@app/elements-view/femme-element-dialog.component';
import { FemmeElementsTableComponent } from "@app/elements-view/femme-elements-table.component";
import { ExtractFulltextFieldsPipe } from './search/extract-fulltext-fields.pipe';
import { FormatMetadatumPipe } from "@app/elements-view/format-metadatum.pipe";
import { SpinnerService } from "@app/shared/spinner/spinner.service";

@NgModule({
	declarations: [
		AppComponent,
		SpinnerComponent,
		FemmeNavbarComponent,
		FemmeSearchComponent,
		FemmeElementsTableComponent,
		FemmeElementDialogComponent,
		ExtractFulltextFieldsPipe,
		FormatMetadatumPipe
	],
	imports: [
		BrowserModule,
		BrowserAnimationsModule,
		AppRoutingModule,
		FormsModule,
		ReactiveFormsModule,
		HttpClientModule,
		MaterialModule
	],
	entryComponents: [
		FemmeElementDialogComponent
	],
	providers: [
		SpinnerService,
		FemmeService,
		FemmeSearchService
	],
	bootstrap: [ AppComponent ]
})
export class AppModule { }
