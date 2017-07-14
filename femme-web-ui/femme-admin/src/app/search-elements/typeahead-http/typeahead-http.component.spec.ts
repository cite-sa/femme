import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TypeaheadHttpComponent } from './typeahead-http.component';

describe('TypeaheadHttpComponent', () => {
  let component: TypeaheadHttpComponent;
  let fixture: ComponentFixture<TypeaheadHttpComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TypeaheadHttpComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TypeaheadHttpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
