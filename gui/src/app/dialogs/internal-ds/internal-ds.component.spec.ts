import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { InternalDsComponent } from './internal-ds.component';

describe('InternalDsComponent', () => {
  let component: InternalDsComponent;
  let fixture: ComponentFixture<InternalDsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ InternalDsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(InternalDsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
