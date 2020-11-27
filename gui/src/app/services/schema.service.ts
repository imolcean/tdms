import { Injectable } from '@angular/core';
import {TableMetaDataDto} from "../dto/dto";
import {Observable, Subject} from "rxjs";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root',
})
export class SchemaService
{
  private schema$: Subject<TableMetaDataDto[]>;

  constructor(private http: HttpClient)
  {
    this.schema$ = new Subject<TableMetaDataDto[]>();
  }

  public getSchema(): Observable<TableMetaDataDto[]>
  {
    return this.schema$.asObservable();
  }

  public loadSchema(): void
  {
    this.http
      .get<TableMetaDataDto[]>('api/schema/internal')
      .subscribe((value: TableMetaDataDto[]) => this.schema$.next(value));
  }
}
