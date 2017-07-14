import { MetadataRetrievalService } from './femme-services/metadata-retrieval.service';

import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule, ReactiveFormsModule }   from '@angular/forms';
import { HttpModule } from '@angular/http';
import { RouterModule, Routes } from '@angular/router';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { UiSwitchModule } from 'ng2-ui-switch';

import { AppComponent } from './app.component';
// import { SearchElementsComponent } from './search-elements/search-elements.component';
// import { TypeaheadHttpComponent } from './search-elements/typeahead-http/typeahead-http.component';
import { AppNavbarComponent } from './app-navbar/app-navbar.component';
// import { HighlightJsModule, HighlightJsService } from 'angular2-highlight-js';
// import { AppSidePanelComponent } from './app-side-panel/app-side-panel.component';
import { AppSearchComponent } from './elements-view/app-search/app-search.component';
// import { AppMenuComponent } from './app-menu/app-menu.component';
import { ElementsViewComponent } from './elements-view/elements-view.component';
import { CollectionListComponent } from './collection-list/collection-list.component';
import { CollectionDetailsComponent } from './collection-details/collection-details.component';
import { DataElementListComponent } from './elements-view/data-element-list/data-element-list.component';
import { DataElementDetailsComponent } from './data-element-details/data-element-details.component';
import { MetadataListComponent } from './metadata-list/metadata-list.component';

import { ElementsService } from './elements-service.service';
import { FemmeQueryService } from './femme-services/femme-query.service';
import { FemmeSearchService } from './femme-services/femme-search.service';
import { DataElementInfoComponent } from './data-element-details/data-element-info/data-element-info.component';
import { DataElementMetadataComponent } from './data-element-details/data-element-metadata/data-element-metadata.component';


const appRoutes: Routes = [
	{ path: 'collections', component: CollectionListComponent },
	{ path: 'dataElements', component: ElementsViewComponent },
	{ path: 'dataElements/:id', component: DataElementDetailsComponent },
	{ path: '', redirectTo: '/dataElements', pathMatch: 'full' }
];

@NgModule({
	declarations: [
		AppComponent,
		AppNavbarComponent,
		// AppSidePanelComponent,
		AppSearchComponent,
		// AppElementComponent,
		// AppMenuComponent,
		CollectionListComponent, DataElementListComponent,
		CollectionDetailsComponent, DataElementDetailsComponent, ElementsViewComponent, MetadataListComponent, DataElementInfoComponent, DataElementMetadataComponent
		// TypeaheadHttpComponent
	],
	imports: [
		BrowserModule, BrowserAnimationsModule,
		FormsModule, ReactiveFormsModule,
		HttpModule,
		RouterModule.forRoot(appRoutes),
		NgbModule.forRoot(),
		UiSwitchModule
	],
	providers: [FemmeQueryService, ElementsService, MetadataRetrievalService],
	bootstrap: [AppComponent]
})

export class AppModule { }
