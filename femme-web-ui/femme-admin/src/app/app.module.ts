import { MetadataRetrievalService } from './femme-services/metadata-retrieval.service';

import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule, ReactiveFormsModule }   from '@angular/forms';
import { HttpModule } from '@angular/http';
import { RouterModule, Routes } from '@angular/router';
import { UiSwitchModule } from 'ng2-ui-switch';

import { AppComponent } from './app.component';
// import { SearchElementsComponent } from './search-elements/search-elements.component';
// import { TypeaheadHttpComponent } from './search-elements/typeahead-http/typeahead-http.component';

import { AppNavbarComponent } from './app-navbar/app-navbar.component';
import { AppSearchComponent } from './elements-view/app-search/app-search.component';
import { ElementsViewComponent } from './elements-view/elements-view.component';
import { CollectionListComponent } from './elements-view/collection-list/collection-list.component';
import { CollectionDetailsComponent } from './elements-view/collection-details/collection-details.component';
import { DataElementListComponent } from './elements-view/data-element-list/data-element-list.component';
import { DataElementDetailsComponent } from './elements-view/data-element-details/data-element-details.component';
import { DataElementInfoComponent } from './elements-view/data-element-details/data-element-info/data-element-info.component';
import { DataElementMetadataComponent } from './elements-view/data-element-details/data-element-metadata/data-element-metadata.component';

import { ElementsService } from './femme-services/elements-service.service';
import { FemmeQueryService } from './femme-services/femme-query.service';

const appRoutes: Routes = [
	{ path: 'collections', component: CollectionListComponent },
	{ path: 'dataElements', component: ElementsViewComponent },
	{ path: 'dataElements/:id', component: DataElementDetailsComponent },
	{ path: '', redirectTo: '/dataElements', pathMatch: 'full' }
];

@NgModule({
	declarations: [
		AppComponent,
		AppNavbarComponent, ElementsViewComponent, AppSearchComponent,
		CollectionListComponent, DataElementListComponent,
		CollectionDetailsComponent, DataElementDetailsComponent, DataElementInfoComponent, DataElementMetadataComponent
		// TypeaheadHttpComponent
	],
	imports: [
		BrowserModule, BrowserAnimationsModule,
		FormsModule, ReactiveFormsModule,
		HttpModule,
		RouterModule.forRoot(appRoutes)
	],
	providers: [FemmeQueryService, ElementsService, MetadataRetrievalService],
	bootstrap: [AppComponent]
})

export class AppModule { }
