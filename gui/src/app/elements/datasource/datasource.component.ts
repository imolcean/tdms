import {Component, Input, OnInit} from '@angular/core';
import {DataSourceDto} from "../../dto/dto";

@Component({
  selector: 'app-datasource',
  templateUrl: './datasource.component.html',
  styleUrls: ['./datasource.component.scss']
})
export class DatasourceComponent implements OnInit
{
  @Input()
  public ds: DataSourceDto | undefined;

  @Input()
  public editing: boolean = false;

  constructor() {}

  ngOnInit(): void {}
}
