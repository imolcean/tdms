import { Component, OnInit } from '@angular/core';
import {MessageService} from "../../services/message.service";
import {DynamicDialogConfig, DynamicDialogRef} from "primeng/dynamicdialog";

@Component({
  selector: 'app-import',
  templateUrl: './import.component.html',
  styleUrls: ['./import.component.scss']
})
export class ImportComponent implements OnInit
{
  // TODO: "Are you sure you want to import data from {{importPath}}? \\ Note: Internal database has to be empty"
  // TODO: "Internal database is not empty. Do you want to clear it before performing import?"

  constructor(private ref: DynamicDialogRef,
              private config: DynamicDialogConfig,
              private msg: MessageService) {}

  ngOnInit(): void {}

  public onCancel()
  {
    this.msg.publish({kind: "INFO", content: "Import canceled"});
    this.ref.close(false);
  }

  public onImport()
  {
    this.msg.publish({kind: "INFO", content: "Import confirmed"});
    this.ref.close(true);
  }
}
