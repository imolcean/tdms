import { Component, OnInit } from '@angular/core';
import {DynamicDialogConfig, DynamicDialogRef} from "primeng/dynamicdialog";
import {TableService} from "../../services/table.service";

@Component({
  selector: 'app-export',
  templateUrl: './export.component.html',
  styleUrls: ['./export.component.scss']
})
export class ExportComponent implements OnInit
{
  public exportPath: string = "PATH"; // TODO

  constructor(private ref: DynamicDialogRef,
              private config: DynamicDialogConfig,
              private data: TableService) {}

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
