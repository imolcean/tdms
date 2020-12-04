import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {MessageService} from "./message.service";
import {Observable} from "rxjs";
import {DataSourceDto} from "../dto/dto";
import {tap} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class DataSourceService
{
  constructor(private http: HttpClient,
              private msg: MessageService) {}

  public loadInternalDatasource(): Observable<DataSourceDto>
  {
    this.msg.publish({kind: "INFO", content: "Loading parameters of internal database..."});

    return this.http
      .get<DataSourceDto>('api/datasource/internal')
      .pipe(
        tap(_value => this.msg.publish({kind: "SUCCESS", content: "Parameters loaded"}),
            e => this.msg.publish({kind: "ERROR", content: e.error}))
      );
  }
}
