import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DataElementMetadataComponent } from './data-element-metadata.component';

describe('DataElementMetadataComponent', () => {
  let component: DataElementMetadataComponent;
  let fixture: ComponentFixture<DataElementMetadataComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DataElementMetadataComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DataElementMetadataComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
