import { Component, OnInit } from '@angular/core';
import {ProjectDto} from "../../../dto/dto";
import {DynamicDialogConfig, DynamicDialogRef} from "primeng/dynamicdialog";
import {ProjectService} from "../../../services/project.service";

@Component({
  selector: 'app-project-profile',
  templateUrl: './project-profile.component.html',
  styleUrls: ['./project-profile.component.scss']
})
export class ProjectProfileComponent implements OnInit
{
  public project: ProjectDto | undefined;

  constructor(private ref: DynamicDialogRef,
              private config: DynamicDialogConfig,
              private projectService: ProjectService)
  {
    this.projectService.getProject()
      .subscribe((value: ProjectDto | undefined) => this.project = value);
  }

  ngOnInit(): void {}

  public onOk()
  {
    this.ref.close(true);
  }
}
