import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {MessageService} from "./message.service";
import {Observable, Subject} from "rxjs";
import {DataSourceDto, StageDto} from "../dto/dto";

@Injectable({
  providedIn: 'root'
})
export class DataSourceService
{
  private internalDs$: Subject<DataSourceDto>;
  private tmpDs$: Subject<DataSourceDto>;
  private stages$: Subject<StageDto[]>;

  constructor(private http: HttpClient,
              private msg: MessageService)
  {
    this.internalDs$ = new Subject<DataSourceDto>();
    this.tmpDs$ = new Subject<DataSourceDto>();
    this.stages$ = new Subject<StageDto[]>();
  }

  public getInternalDs(): Observable<DataSourceDto>
  {
    return this.internalDs$.asObservable();
  }

  public getTmpDs(): Observable<DataSourceDto>
  {
    return this.tmpDs$.asObservable();
  }

  public getStages(): Observable<StageDto[]>
  {
    return this.stages$.asObservable();
  }

  public loadInternalDatasource(): void
  {
    this.msg.publish({kind: "INFO", content: "Loading parameters of internal database..."});

    this.http
      .get<DataSourceDto>('api/datasource/internal')
      .subscribe((value: DataSourceDto) =>
      {
        this.internalDs$.next(value);
        this.msg.publish({kind: "SUCCESS", content: "Parameters loaded"});
      }, error =>
        this.msg.publish({kind: "ERROR", content: error.error}));
  }

  public loadTmpDatasource(): void
  {
    this.msg.publish({kind: "INFO", content: "Loading parameters of temp database..."});

    this.http
      .get<DataSourceDto>('api/datasource/tmp')
      .subscribe((value: DataSourceDto) =>
      {
        this.tmpDs$.next(value);
        this.msg.publish({kind: "SUCCESS", content: "Parameters loaded"});
      }, error =>
        this.msg.publish({kind: "ERROR", content: error.error}));
  }

  public loadStages(): void
  {
    this.msg.publish({kind: "INFO", content: "Loading list of stages..."});

    this.http
      .get<StageDto[]>('api/datasource/stages')
      .subscribe((value: StageDto[]) =>
      {
        this.stages$.next(value);
        this.msg.publish({kind: "SUCCESS", content: "Stages loaded"});
      }, error =>
        this.msg.publish({kind: "ERROR", content: error.error}));
  }

  public createStage(stage: StageDto): void
  {
    this.msg.publish({kind: "INFO", content: "Creating new stage..."});

    this.http
      .post<StageDto>('api/datasource/stages/' + stage.name, stage.datasource)
      .subscribe(_value =>
      {
        this.msg.publish({kind: "SUCCESS", content: "Stage created"});
        this.loadStages();
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }

  public updateStage(stage: StageDto): void
  {
    this.msg.publish({kind: "INFO", content: "Updating stage..."});

    this.http
      .put<StageDto>('api/datasource/stage/' + stage.name, stage.datasource)
      .subscribe(_value =>
      {
        this.msg.publish({kind: "SUCCESS", content: "Stage updated"});
        this.loadStages();
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }

  public deleteStage(stageName:string): void
  {
    this.msg.publish({kind: "INFO", content: "Deleting stage..."});

    this.http
      .delete('api/datasource/stage/' + stageName)
      .subscribe(_value =>
      {
        this.msg.publish({kind: "SUCCESS", content: "Stage deleted"});
        this.loadStages();
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }
}
