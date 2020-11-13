import { Injectable } from '@angular/core';
import {TableMetaDataDto} from "../dto/dto";
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root',
})
export class SchemaService
{
  constructor(private http: HttpClient) {}

  getSchema(): Observable<TableMetaDataDto[]>
  {
    return this.http.get<TableMetaDataDto[]>('api/schema/internal');
  }
}
