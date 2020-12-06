import { Component, OnInit } from '@angular/core';
import {DynamicDialogConfig, DynamicDialogRef} from "primeng/dynamicdialog";
import {DataSourceService} from "../../services/data-source.service";
import {StageDto} from "../../dto/dto";

@Component({
  selector: 'app-stages',
  templateUrl: './stages.component.html',
  styleUrls: ['./stages.component.scss']
})
export class StagesComponent implements OnInit
{
  public stages: StageDto[] | undefined;
  public selectedStage: StageDto;
  public editing: boolean;
  public creating: boolean;

  private tmpStage: StageDto | undefined;

  constructor(private ref: DynamicDialogRef,
              private config: DynamicDialogConfig,
              private dsService: DataSourceService)
  {
    this.dsService.getStages()
      .subscribe((value: StageDto[]) => this.stages = value);

    this.selectedStage = this.createEmptyStage();
    this.editing = false;
    this.creating = false;
  }

  ngOnInit(): void
  {
    this.dsService.loadStages();
  }

  public onOk()
  {
    this.ref.close(true);
  }

  public onStageSelect($event: any): void
  {
    this.selectedStage = this.cloneStage($event.value as StageDto);
  }

  public onCreateStage(): void
  {
    this.selectedStage = this.createEmptyStage();
    this.editing = true;
    this.creating = true;
  }

  public onCopyStage(): void
  {
    this.selectedStage = this.cloneStage(this.selectedStage);
    this.selectedStage.name = "";
    this.editing = true;
    this.creating = true;
  }

  public onEditStage(): void
  {
    this.tmpStage = this.cloneStage(this.selectedStage);
    this.editing = true;
  }

  public onSaveStageEditing(): void
  {
    if(this.creating)
    {
      this.dsService.createStage(this.selectedStage);
    }
    else
    {
      this.dsService.updateStage(this.selectedStage);
    }

    this.editing = false;
    this.creating = false;
    this.selectedStage = this.createEmptyStage();
    delete this.tmpStage;
  }

  public onCancelStageEditing(): void
  {
    this.editing = false;
    this.selectedStage = this.tmpStage!;
    delete this.tmpStage;
  }

  public onDeleteStage(): void
  {
    this.dsService.deleteStage(this.selectedStage.name);
    this.selectedStage = this.createEmptyStage();
  }

  private createEmptyStage(): StageDto
  {
    return {name: "", datasource: {driverClassName: "", url: "", database: "", username: "", password: ""}};
  }

  private cloneStage(original: StageDto): StageDto
  {
    return {
      name: original.name,
      datasource: {
        driverClassName: original.datasource.driverClassName,
        url: original.datasource.url,
        database: original.datasource.database,
        username: original.datasource.username,
        password: original.datasource.password
      }
    } as StageDto;
  }
}
