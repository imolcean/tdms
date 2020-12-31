import { Injectable } from '@angular/core';
import {ProjectDto, TableContentDto, TableRuleDto, ValueListDto} from "../dto/dto";
import {HttpClient} from "@angular/common/http";
import {BehaviorSubject, Observable} from "rxjs";
import {MessageService} from "./message.service";
import {tap} from "rxjs/operators";
import {ProjectService} from "./project.service";

@Injectable({
  providedIn: 'root',
})
export class DataService
{
  private contentLocation$: BehaviorSubject<string| undefined>;
  private content$: BehaviorSubject<TableContentDto | undefined>;
  private valueLists$: BehaviorSubject<ValueListDto[] | undefined>;

  constructor(private http: HttpClient,
              private msg: MessageService,
              private projectService: ProjectService)
  {
    this.contentLocation$ = new BehaviorSubject<string | undefined>(undefined);
    this.content$ = new BehaviorSubject<TableContentDto | undefined>(undefined);
    this.valueLists$ = new BehaviorSubject<ValueListDto[] | undefined>(undefined);

    this.projectService.getProject()
      .subscribe((value: ProjectDto | undefined) => {
        if(value === undefined)
        {
          this.content$.next(undefined);
        }
      });
  }

  public getContentLocation(): Observable<string | undefined>
  {
    return this.contentLocation$.asObservable();
  }

  public getContent(): Observable<TableContentDto | undefined>
  {
    return this.content$.asObservable();
  }

  public getValueLists(): Observable<ValueListDto[] | undefined>
  {
    return this.valueLists$.asObservable();
  }

  public loadData(alias: string, tableName: string): void
  {
    this.msg.publish({kind: "INFO", content: "Loading content of table " + tableName + "..."});

    this.http
      .get<TableContentDto>('api/data/' + alias + '/' + tableName)
      .subscribe((value: TableContentDto) =>
        {
          this.msg.publish({kind: "SUCCESS", content: "Content loaded"});
          this.contentLocation$.next(alias);
          this.content$.next(value);
        }, error =>
        {
          this.msg.publish({kind: "ERROR", content: error.error});
        });
  }

  public unloadData(): void
  {
    this.content$.next(undefined);
  }

  public loadValueLists(): void
  {
    this.msg.publish({kind: "INFO", content: "Loading value lists"});

    this.http
      .get<ValueListDto[]>('api/data/internal/generate/lists')
      .subscribe((value: ValueListDto[]) =>
      {
        this.msg.publish({kind: "SUCCESS", content: "Value lists loaded"});
        this.valueLists$.next(value);
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }

  public addRows(tableName: string, rows: {[key: string]: any;}[]): void
  {
    this.msg.publish({kind: "INFO", content: "Adding " + rows.length + " new row(s) into table " + tableName + "..."});

    this.http
      .post('api/data/internal/' + tableName, rows)
      .subscribe(_value =>
      {
        this.msg.publish({kind: "SUCCESS", content: "Row(s) added"});
        this.loadData('internal', tableName);
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }

  public updateRow(tableName: string, rowIndex: number, row: {[key: string]: any;}): void
  {
    this.msg.publish({kind: "INFO", content: "Updating row nr. " + rowIndex + " of table " + tableName + "..."});

    this.http
      .put('api/data/internal/' + tableName + '/' + rowIndex, row)
      .subscribe(_value =>
      {
        this.msg.publish({kind: "SUCCESS", content: "Row updated"});
        this.loadData('internal', tableName);
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }

  public deleteRow(tableName: string, rowIndex: number): void
  {
    this.msg.publish({kind: "INFO", content: "Deleting row nr. " + rowIndex + " from table " + tableName + "..."});

    this.http
      .delete('api/data/internal/' + tableName + '/' + rowIndex)
      .subscribe(_value =>
      {
        this.msg.publish({kind: "SUCCESS", content: "Row deleted"});
        this.loadData('internal', tableName);
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }

  public importData(): void
  {
    this.msg.publish({kind: "INFO", content: "Importing data into internal database..."});

    this.http
      .put('api/data/internal/import', null)
      .subscribe(_value =>
      {
        this.msg.publish({kind: "SUCCESS", content: "Import finished successfully"});
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }

  public exportData(): void
  {
    this.msg.publish({kind: "INFO", content: "Exporting data from internal database..."});

    this.http
      .put('api/data/internal/export', null)
      .subscribe(_value =>
      {
        this.msg.publish({kind: "SUCCESS", content: "Export finished successfully"});
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }

  public clearAll(): void
  {
    this.msg.publish({kind: "INFO", content: "Clearing all data..."});

    this.http
      .delete('api/data/internal')
      .subscribe(_value =>
      {
        this.msg.publish({kind: "SUCCESS", content: "All data cleared"});
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }

  public generate(rules: TableRuleDto[]): Observable<void>
  {
    this.msg.publish({kind: "INFO", content: "Generating data..."});

    return this.http
      .put<void>('api/data/internal/generate', rules)
      .pipe(
        tap(
          _x => this.msg.publish({kind: "SUCCESS", content: "Data generated successfully"}),
          e => this.msg.publish({kind: "ERROR", content: e.error}))
      );
  }

  public deployToCurrentStage(): void
  {
    this.msg.publish({kind: "INFO", content: "Deploying data..."});

    this.http
      .put<void>('api/deployment/current', null)
      .subscribe(_value =>
      {
        this.msg.publish({kind: "SUCCESS", content: "Data deployed successfully"});
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }
}
