import { Injectable } from '@angular/core';
import {TableMetaDataDto} from "../dto/dto";
import {BehaviorSubject, Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root',
})
export class SchemaService
{
  private schema$: BehaviorSubject<TableMetaDataDto[] | undefined>;

  constructor(private http: HttpClient)
  {
    this.schema$ = new BehaviorSubject<TableMetaDataDto[] | undefined>(undefined);
  }

  public getSchema(): Observable<TableMetaDataDto[] | undefined>
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
