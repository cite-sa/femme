import { TestBed, inject } from '@angular/core/testing';

import { FemmeQueryService } from './femme-query.service';

describe('FemmeQueryService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [FemmeQueryService]
    });
  });

  it('should be created', inject([FemmeQueryService], (service: FemmeQueryService) => {
    expect(service).toBeTruthy();
  }));
});
