import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MigrationFormComponent } from './migration-form.component';

describe('MigrationFormComponent', () => {
  let component: MigrationFormComponent;
  let fixture: ComponentFixture<MigrationFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MigrationFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MigrationFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
