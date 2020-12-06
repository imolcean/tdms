import { Component, OnInit } from '@angular/core';
import {DynamicDialogConfig, DynamicDialogRef} from "primeng/dynamicdialog";
import {DataSourceService} from "../../services/data-source.service";
import {DataSourceDto} from "../../dto/dto";

@Component({
  selector: 'app-internal-ds',
  templateUrl: './internal-ds.component.html',
  styleUrls: ['./internal-ds.component.scss']
})
export class InternalDsComponent implements OnInit
{
  public ds: DataSourceDto | undefined;

  constructor(private ref: DynamicDialogRef,
              private config: DynamicDialogConfig,
              private dsService: DataSourceService)
  {
    dsService.getInternalDs()
      .subscribe((value: DataSourceDto) => this.ds = value);
  }

  ngOnInit(): void
  {
    this.dsService.loadInternalDatasource();
  }

  public onOk()
  {
    this.ref.close(true);
  }
}
