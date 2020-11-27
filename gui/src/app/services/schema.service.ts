import { Injectable } from '@angular/core';
import {TableMetaDataDto} from "../dto/dto";
import {Observable, Subject} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {MessageService} from "./message.service";

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
}
