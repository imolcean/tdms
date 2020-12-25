import { Injectable } from '@angular/core';
import {TableContentDto} from "../dto/dto";
import {HttpClient} from "@angular/common/http";
import {BehaviorSubject, Observable} from "rxjs";
import {MessageService} from "./message.service";

@Injectable({
  providedIn: 'root',
})
export class TableService
{
  private content$: BehaviorSubject<TableContentDto | undefined>;

  constructor(private http: HttpClient,
              private msg: MessageService)
  {
    this.content$ = new BehaviorSubject<TableContentDto | undefined>(undefined);
  }

  public getContent(): Observable<TableContentDto | undefined>
  {
    return this.content$.asObservable();
  }

  public loadData(tableName: string): void
  {
    this.msg.publish({kind: "INFO", content: "Loading content of table " + tableName + "..."});

    this.http
      .get<TableContentDto>('api/data/internal/' + tableName)
      .subscribe((value: TableContentDto) =>
        {
          this.msg.publish({kind: "SUCCESS", content: "Content loaded"});
          this.content$.next(value);
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
        this.loadData(tableName);
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
        this.loadData(tableName);
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
        this.loadData(tableName);
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
}
