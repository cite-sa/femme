import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchElementsComponent } from './search-elements.component';

describe('SearchElementsComponent', () => {
	let component: SearchElementsComponent;
	let fixture: ComponentFixture<SearchElementsComponent>;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [SearchElementsComponent]
		})
			.compileComponents();
	}));

	beforeEach(() => {
		fixture = TestBed.createComponent(SearchElementsComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should be created', () => {
		expect(component).toBeTruthy();
	});
});
