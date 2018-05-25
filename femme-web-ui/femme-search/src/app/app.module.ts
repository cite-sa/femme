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
import { MatDialogContainer } from "@angular/material";
import { ExtractFulltextFieldsPipe } from './search/extract-fulltext-fields.pipe';
import { FormatXmlPipe } from "@app/elements-view/format-xml.pipe";

@NgModule({
	declarations: [
		AppComponent,
		FemmeNavbarComponent,
		FemmeSearchComponent,
		FemmeElementsTableComponent,
		FemmeElementDialogComponent,
		ExtractFulltextFieldsPipe,
		FormatXmlPipe
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
		FemmeService,
		FemmeSearchService
	],
	bootstrap: [ AppComponent ]
})
export class AppModule { }
