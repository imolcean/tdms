import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateVisualiserComponent } from './update-visualiser.component';

describe('UpdateVisualiserComponent', () => {
  let component: UpdateVisualiserComponent;
  let fixture: ComponentFixture<UpdateVisualiserComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UpdateVisualiserComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UpdateVisualiserComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
