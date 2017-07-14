import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DataElementInfoComponent } from './data-element-info.component';

describe('DataElementInfoComponent', () => {
  let component: DataElementInfoComponent;
  let fixture: ComponentFixture<DataElementInfoComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DataElementInfoComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DataElementInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
