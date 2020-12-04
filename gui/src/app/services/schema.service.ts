import { Injectable } from '@angular/core';
import {TableMetaDataDto} from "../dto/dto";
import {Observable, Subject} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {MessageService} from "./message.service";
import {tap} from "rxjs/operators";

@Injectable({
  providedIn: 'root',
})
export class SchemaService
{
  private schema$: Subject<TableMetaDataDto[]>;

  constructor(private http: HttpClient, private msg: MessageService)
  {
    this.schema$ = new Subject<TableMetaDataDto[]>();
  }

  public getSchema(): Observable<TableMetaDataDto[]>
  {
    return this.schema$.asObservable();
  }

  public loadSchema(): void
  {
    this.msg.publish({kind: "INFO", content: "Loading schema..."});

    this.http
      .get<TableMetaDataDto[]>('api/schema/internal')
      .subscribe((value: TableMetaDataDto[]) =>
      {
        this.schema$.next(value);
        this.msg.publish({kind: "SUCCESS", content: "Schema loaded"});
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }

  public getOccupiedTableNames(): Observable<string[]>
  {
    this.msg.publish({kind: "INFO", content: "Loading list of non-empty tables..."});

    return this.http
      .get<string[]>('api/schema/tables/internal/occupied')
      .pipe(
        tap(
          (_value: string[]) => this.msg.publish({kind: "SUCCESS", content: "List of non-empty tables loaded"}),
          error => this.msg.publish({kind: "ERROR", content: error.error})
        )
      );
  }
}