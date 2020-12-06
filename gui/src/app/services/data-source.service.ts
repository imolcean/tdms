import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {MessageService} from "./message.service";
import {Observable, Subject} from "rxjs";
import {DataSourceDto} from "../dto/dto";

@Injectable({
  providedIn: 'root'
})
export class DataSourceService
{
  private internalDs$: Subject<DataSourceDto>;
  private tmpDs$: Subject<DataSourceDto>;
  private stages$: Subject<{ [name: string]: DataSourceDto }>;

  constructor(private http: HttpClient,
              private msg: MessageService)
  {
    this.internalDs$ = new Subject<DataSourceDto>();
    this.tmpDs$ = new Subject<DataSourceDto>();
    this.stages$ = new Subject<{[name: string]: DataSourceDto}>();
  }

  public getInternalDs(): Observable<DataSourceDto>
  {
    return this.internalDs$.asObservable();
  }

  public getTmpDs(): Observable<DataSourceDto>
  {
    return this.tmpDs$.asObservable();
  }

  public getStages(): Observable<{ [name: string]: DataSourceDto }>
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
      .get<{ [name: string]: DataSourceDto }>('api/datasource/stages')
      .subscribe((value: { [name: string]: DataSourceDto }) =>
      {
        this.stages$.next(value);
        this.msg.publish({kind: "SUCCESS", content: "Stages loaded"});
      }, error =>
        this.msg.publish({kind: "ERROR", content: error.error}));
  }
}
