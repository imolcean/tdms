import { Component, OnInit } from '@angular/core';
import {DynamicDialogConfig, DynamicDialogRef} from "primeng/dynamicdialog";
import {TableService} from "../../services/table.service";
import {PathsMap, PathsService} from "../../services/paths.service";
import {ProjectService} from "../../services/project.service";
import {ProjectDto} from "../../dto/dto";

@Component({
  selector: 'app-export',
  templateUrl: './export.component.html',
  styleUrls: ['./export.component.scss']
})
export class ExportComponent implements OnInit
{
  public exportPath: string| undefined;

  constructor(private ref: DynamicDialogRef,
              private config: DynamicDialogConfig,
              private data: TableService,
              private projectService: ProjectService)
  {
    this.projectService.getProject()
      .subscribe((value: ProjectDto | undefined) => this.exportPath = value ? value.dataDir : undefined);
  }

  ngOnInit(): void {}

  public onCancel()
  {
    this.ref.close(false);
  }

  public onConfirm()
  {
    this.ref.close(true);
    this.data.exportData();
  }
}
