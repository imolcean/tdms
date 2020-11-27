import { Component, OnInit } from '@angular/core';
import {TableContentDto} from "../../dto/dto";
import {TableService} from "../../services/table.service";

@Component({
  selector: 'app-table-content',
  templateUrl: './table-content.component.html',
  styleUrls: ['./table-content.component.scss']
})
export class TableContentComponent implements OnInit
{
  public content: TableContentDto | undefined;

  public showDialog: boolean;

  public tmpRow: any[];
  public tmpRowIndex: number | undefined;

  constructor(private tableService: TableService)
  {
    this.tableService.getContent()
      .subscribe((value: TableContentDto | undefined) => this.content = value);

    this.showDialog = false;
    this.tmpRow = [];
  }

  ngOnInit(): void {}

  public onAddRow()
  {
    this.tmpRow = [];
    this.showDialog = true;
  }

  public onRowEdit(row: any, ri: number)
  {
    this.tmpRow = {...row};
    this.tmpRowIndex = ri;
    this.showDialog = true;
  }

  public onRowSave()
  {
    const map: {[key: string]: any;} = {};

    console.log(this.tmpRow);

    for(let i in this.tmpRow)
    {
      console.log(i);

      map[this.content!.columnNames[i]] = this.tmpRow[i];
    }

    if(this.tmpRowIndex === undefined)
    {
      this.tableService.addRows(this.content!.tableName, [map]);
    }
    else
    {
      this.tableService.updateRow(this.content!.tableName, this.tmpRowIndex, map);
    }

    this.tmpRow = [];
    delete this.tmpRowIndex;
    this.showDialog = false;
  }

  public onRowCancel()
  {
    this.tmpRow = [];
    delete this.tmpRowIndex;
    this.showDialog = false;
  }

  public onDeleteRow(ri: number)
  {
    this.tableService.deleteRow(this.content!.tableName, ri);

    // TODO Ask for confirmation
  }
}
