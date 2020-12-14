import { Component, OnInit } from '@angular/core';
import {DynamicDialogConfig, DynamicDialogRef} from "primeng/dynamicdialog";
import {ProjectService} from "../../../services/project.service";

@Component({
  selector: 'app-open-project',
  templateUrl: './project-open.component.html',
  styleUrls: ['./project-open.component.scss']
})
export class ProjectOpenComponent implements OnInit
{
  public ready: boolean;
  public file: File | null;

  constructor(private ref: DynamicDialogRef,
              private config: DynamicDialogConfig,
              private projectService: ProjectService)
  {
    this.ready = false;
    this.file = null;
  }

  ngOnInit(): void {}

  public onOk(): void
  {
    this.projectService.openProject(this.file as File);
    this.ref.close(true);
  }

  public onCancel(): void
  {
    this.ref.close(true);
  }

  public onUpload(files: FileList): void
  {
    this.file = files.item(0);
    this.ready = true;
  }
}
