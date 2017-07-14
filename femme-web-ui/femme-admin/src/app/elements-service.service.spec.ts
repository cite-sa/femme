import { TestBed, inject } from '@angular/core/testing';

import { ElementsServiceService } from './elements-service.service';

describe('ElementsServiceService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ElementsServiceService]
    });
  });

  it('should be created', inject([ElementsServiceService], (service: ElementsServiceService) => {
    expect(service).toBeTruthy();
  }));
});
