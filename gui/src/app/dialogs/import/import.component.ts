import { Component, OnInit } from '@angular/core';
import {DynamicDialogConfig, DynamicDialogRef} from "primeng/dynamicdialog";
import {TableService} from "../../services/table.service";

@Component({
  selector: 'app-import',
  templateUrl: './import.component.html',
  styleUrls: ['./import.component.scss']
})
export class ImportComponent implements OnInit
{
  public importPath: string = "PATH"; // TODO

  constructor(private ref: DynamicDialogRef,
              private config: DynamicDialogConfig,
              private data: TableService) {}

  ngOnInit(): void {}

  public onCancel()
  {
    this.ref.close(false);
  }

  public onImport()
  {
    this.ref.close(true);

    this.data.importData();
  }
}
