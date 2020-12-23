import { Component, OnInit } from '@angular/core';
import {MenuItem} from "primeng/api";
import {DynamicDialogConfig, DynamicDialogRef} from "primeng/dynamicdialog";
import {DataSourceService} from "../../services/data-source.service";
import {ProjectService} from "../../services/project.service";
import {
  ProjectDto,
  SchemaUpdateDataMappingRequest,
  SchemaUpdateDataMappingRequestTableDataMigrationRequest,
  SchemaUpdateDto
} from "../../dto/dto";
import {UpdateService} from "../../services/update.service";

@Component({
  selector: 'app-update',
  templateUrl: './update.component.html',
  styleUrls: ['./update.component.scss']
})
export class UpdateComponent implements OnInit
{
  public updater: string | undefined;
  public steps: MenuItem[];
  public currentStep: number | undefined;
  public currentUpdateReport: SchemaUpdateDto | undefined;
  public loading: boolean;
  public mappingRequest: SchemaUpdateDataMappingRequest | undefined;

  constructor(private ref: DynamicDialogRef,
              private config: DynamicDialogConfig,
              private dsService: DataSourceService,
              private projectService: ProjectService,
              private updateService: UpdateService)
  {
    this.steps = [
      {label: 'Start'},
      {label: 'Update initialised'},
      {label: 'Data mapping'},
      {label: 'Confirmation'}
    ];

    this.loading = false;

    this.projectService.getProject()
      .subscribe((value: ProjectDto | undefined) => this.updater = value?.schemaUpdater);

    this.updateService.getUpdateStep()
      .subscribe((value: number | undefined) =>
      {
        this.currentStep = value;
        if(this.currentStep == 0)
        {
          this.loading = false;
        }
      });

    this.updateService.getCurrentUpdateReport()
      .subscribe((value: SchemaUpdateDto | undefined) =>
      {
        this.currentUpdateReport = value;
        this.loading = false;
        this.mappingRequest = this.createMappingRequest(value);
      });
  }

  private createMappingRequest(update: SchemaUpdateDto | undefined): SchemaUpdateDataMappingRequest | undefined
  {
    if(update === undefined)
    {
      return undefined;
    }

    const migrations: SchemaUpdateDataMappingRequestTableDataMigrationRequest[] = [];

    for(const table of update.addedTables)
    {
      migrations.push({tableName: table.name, sql: ""});
    }

    for(const comparison of update.changedTables)
    {
      migrations.push({tableName: comparison.before.name, sql: ""});
    }

    return {sqlMigrationTables: migrations};
  }

  ngOnInit(): void
  {
    this.updateService.loadUpdateStep();
  }

  public onUpdateBegin(): void
  {
    this.loading = true;
    this.updateService.initUpdate();
  }

  public onMapData(mappingRequest: SchemaUpdateDataMappingRequest): void
  {
    this.loading = true;
    this.updateService.mapData(mappingRequest);
  }

  public onRollbackDataMapping(): void
  {
    this.loading = true;
    this.updateService.rollbackDataMapping();
  }

  public onConfirm(): void
  {
    this.updateService.commitUpdate();
    this.ref.close(false);
  }

  public onCancel(): void
  {
    if(this.currentStep !== undefined && this.currentStep > 0)
    {
      this.updateService.cancelUpdate();
    }
    this.ref.close(false);
  }
}
