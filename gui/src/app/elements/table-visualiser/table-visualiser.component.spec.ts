import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TableVisualiserComponent } from './table-visualiser.component';

describe('TableVisualiserComponent', () => {
  let component: TableVisualiserComponent;
  let fixture: ComponentFixture<TableVisualiserComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TableVisualiserComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TableVisualiserComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
