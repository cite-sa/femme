import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ElementsViewComponent } from './elements-view.component';

describe('ElementsViewComponent', () => {
  let component: ElementsViewComponent;
  let fixture: ComponentFixture<ElementsViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ElementsViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ElementsViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
