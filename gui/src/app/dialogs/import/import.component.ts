import { Component, OnInit } from '@angular/core';
import {DynamicDialogConfig, DynamicDialogRef} from "primeng/dynamicdialog";
import {TableService} from "../../services/table.service";
import {ProjectDto} from "../../dto/dto";
import {ProjectService} from "../../services/project.service";

@Component({
  selector: 'app-import',
  templateUrl: './import.component.html',
  styleUrls: ['./import.component.scss']
})
export class ImportComponent implements OnInit
{
  public importPath: string | undefined;

  constructor(private ref: DynamicDialogRef,
              private config: DynamicDialogConfig,
              private data: TableService,
              private projectService: ProjectService)
  {
    this.projectService.getProject()
      .subscribe((value: ProjectDto | undefined) => this.importPath = value ? value.dataDir : undefined);
  }

  ngOnInit(): void {}

  public onCancel()
  {
    this.ref.close(false);
  }

  public onConfirm()
  {
    this.ref.close(true);
    this.data.importData();
  }
}
