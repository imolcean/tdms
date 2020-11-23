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
  content: TableContentDto | undefined;

  constructor(private tableService: TableService)
  {
    this.tableService.getContent()
      .subscribe((value: TableContentDto | undefined) => this.content = value);
  }

  ngOnInit(): void {}
}
