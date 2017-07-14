import { TestBed, inject } from '@angular/core/testing';

import { FemmeSearchService } from './femme-search.service';

describe('FemmeSearchService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [FemmeSearchService]
    });
  });

  it('should be created', inject([FemmeSearchService], (service: FemmeSearchService) => {
    expect(service).toBeTruthy();
  }));
});
