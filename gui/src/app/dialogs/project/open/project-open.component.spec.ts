import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectOpenComponent } from './project-open.component';

describe('OpenComponent', () => {
  let component: ProjectOpenComponent;
  let fixture: ComponentFixture<ProjectOpenComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ProjectOpenComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectOpenComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
