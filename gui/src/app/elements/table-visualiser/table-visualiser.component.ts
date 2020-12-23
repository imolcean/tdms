import {Component, Input, OnInit} from '@angular/core';
import {TableMetaDataDto} from "../../dto/dto";

@Component({
  selector: 'app-table-visualiser',
  templateUrl: './table-visualiser.component.html',
  styleUrls: ['./table-visualiser.component.scss']
})
export class TableVisualiserComponent implements OnInit
{
  @Input()
  public table: TableMetaDataDto | undefined;

  constructor() {}

  ngOnInit(): void {}
}
