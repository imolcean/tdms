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
import {forkJoin} from "rxjs";

@Component({
  selector: 'app-update',
  templateUrl: './update.component.html',
  styleUrls: ['./update.component.scss']
})
export class UpdateComponent implements OnInit
{
  public loading: boolean;
  public currentStep: number | undefined;
  public steps: MenuItem[];
  public updater: string | undefined;
  public mappingRequired: boolean | undefined;
  public currentUpdateReport: SchemaUpdateDto | undefined;
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

    this.loading = true;
  }

  ngOnInit(): void
  {
    this.projectService.getProject()
      .subscribe((value: ProjectDto | undefined) =>
      {
        this.updater = value?.schemaUpdater;
        this.init();
      });
  }

  private init(): void
  {
    forkJoin({
      // project: this.projectService.getProject(),
      updateInProgress: this.updateService.isUpdateInProgress(),
      updaterType: this.updateService.getUpdaterType(),
      dataMapped: this.updateService.isDataMapped()
    })
      .subscribe((value: { /*project: ProjectDto | undefined, */updateInProgress: boolean, updaterType: string, dataMapped: boolean }) =>
      {
        // this.updater = value.project?.schemaUpdater;
        this.mappingRequired = value.updaterType === 'diff';

        if(!value.updateInProgress)
        {
          this.currentStep = 0;
        }
        else
        {
          this.updateService.getCurrentUpdateReport()
            .subscribe((value1: SchemaUpdateDto) =>
            {
              this.currentUpdateReport = value1;
              this.mappingRequest = this.createMappingRequest(value1);
            });

          if(this.mappingRequired)
          {
            if(!value.dataMapped)
            {
              this.currentStep = 1;
            }
            else
            {
              this.currentStep = 3;
            }
          }
          else
          {
            this.currentStep = 1;
          }
        }

        this.loading = false;
      });
  }

  public onUpdateInit(): void
  {
    this.loading = true;
    this.updateService.initUpdate()
      .subscribe((value: SchemaUpdateDto) =>
      {
        this.currentUpdateReport = value;
        this.mappingRequest = this.createMappingRequest(value);
        this.currentStep = 1;
        this.loading = false
      }, _error => this.loading = false);
  }

  public onMapData(mappingRequest: SchemaUpdateDataMappingRequest): void
  {
    this.loading = true;
    this.updateService.mapData(mappingRequest)
      .subscribe(_value =>
      {
        this.currentStep = 3;
        this.loading = false;
      }, _error =>
      {
        this.currentStep = 2;
        this.loading = false;
      });
  }

  public onRollbackDataMapping(): void
  {
    this.loading = true;
    this.updateService.rollbackDataMapping()
      .subscribe(_value =>
      {
        this.currentStep = 1;
        this.loading = false;
      }, _error =>
      {
        this.currentStep = 3;
        this.loading = false;
      });
  }

  public onConfirm(): void
  {
    this.loading = true;
    this.updateService.commitUpdate()
      .subscribe(_value => this.ref.close(true), _error =>
      {
        this.currentStep = 3;
        this.loading = false;
      });
  }

  public onCancel(): void
  {
    if(this.currentStep !== undefined && this.currentStep > 0)
    {
      this.loading = true;
      this.updateService.cancelUpdate()
        .subscribe(_value => {}, _error => {}, () => this.ref.close(false));
    }
    else
    {
      this.ref.close(false);
    }
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
}
