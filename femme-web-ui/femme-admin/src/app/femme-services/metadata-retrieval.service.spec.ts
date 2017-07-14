import { TestBed, inject } from '@angular/core/testing';

import { MetadataRetrievalService } from './metadata-retrieval.service';

describe('MetadataRetrievalService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MetadataRetrievalService]
    });
  });

  it('should be created', inject([MetadataRetrievalService], (service: MetadataRetrievalService) => {
    expect(service).toBeTruthy();
  }));
});
