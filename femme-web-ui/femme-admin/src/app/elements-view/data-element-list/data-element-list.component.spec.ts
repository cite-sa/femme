import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DataElementListComponent } from './data-element-list.component';

describe('DataElementListComponent', () => {
  let component: DataElementListComponent;
  let fixture: ComponentFixture<DataElementListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DataElementListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DataElementListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
