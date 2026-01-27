import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ParseReviewComponent } from './parse-review.component';

describe('ParseReviewComponent', () => {
  let component: ParseReviewComponent;
  let fixture: ComponentFixture<ParseReviewComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ParseReviewComponent]
    });
    fixture = TestBed.createComponent(ParseReviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
