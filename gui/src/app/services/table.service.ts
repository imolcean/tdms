import { Injectable } from '@angular/core';
import {TableContentDto} from "../dto/dto";
import {HttpClient} from "@angular/common/http";
import {BehaviorSubject, Observable} from "rxjs";

@Injectable({
  providedIn: 'root',
})
export class TableService
{
  private content$: BehaviorSubject<TableContentDto | undefined>;

  constructor(private http: HttpClient)
  {
    this.content$ = new BehaviorSubject<TableContentDto | undefined>(undefined);
  }

  public getContent(): Observable<TableContentDto | undefined>
  {
    return this.content$.asObservable();
  }

  public loadData(tableName: string): void
  {
    this.http
      .get<TableContentDto>('api/data/internal/' + tableName)
      .subscribe((value: TableContentDto) => this.content$.next(value));
  }

  public addRows(tableName: string, rows: {[key: string]: any;}[]): void
  {
    this.http
      .post('api/data/internal/' + tableName, rows)
      .subscribe(_value => this.loadData(tableName));
  }

  public updateRow(tableName: string, rowIndex: number, row: {[key: string]: any;}): void
  {
    this.http
      .put('api/data/internal/' + tableName + '/' + rowIndex, row)
      .subscribe(_value => this.loadData(tableName));
  }

  public deleteRow(tableName: string, rowIndex: number): void
  {
    this.http
      .delete('api/data/internal/' + tableName + '/' + rowIndex)
      .subscribe(_value => this.loadData(tableName));
  }
}
