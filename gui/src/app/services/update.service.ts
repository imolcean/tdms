import { Injectable } from '@angular/core';
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {MessageService} from "./message.service";
import {SchemaUpdateDataMappingRequest, SchemaUpdateDto} from "../dto/dto";
import {tap} from "rxjs/operators";
import {SchemaService} from "./schema.service";

@Injectable({
  providedIn: 'root'
})
export class UpdateService
{
  constructor(private http: HttpClient, private msg: MessageService, private schemaService: SchemaService) {}

  public getUpdaterType(): Observable<string>
  {
    this.msg.publish({kind: "INFO", content: "Checking schema updater type..."});

    return this.http
      .get('api/schema-updaters/selected/type', {responseType: "text"})
      .pipe(
        tap(
          (value: string) => this.msg.publish({kind: "SUCCESS", content: "Schema updater is of type " + value}),
          error => this.msg.publish({kind: "ERROR", content: error.error}))
      );
  }

  public isUpdateInProgress(): Observable<boolean>
  {
    this.msg.publish({kind: "INFO", content: "Checking if an update is already running..."});

    return this.http
      .get<boolean>('api/schema/internal/update')
      .pipe(
        tap(
          (value: boolean) => this.msg.publish({kind: "SUCCESS", content: "Update is running: " + value}),
          error => this.msg.publish({kind: "ERROR", content: error.error}))
      );
  }

  public isDataMapped(): Observable<boolean>
  {
    this.msg.publish({kind: "INFO", content: "Checking if data has been mapped..."});

    return this.http
      .get<boolean>('api/schema/internal/update/data')
      .pipe(
        tap(
          (value: boolean) => this.msg.publish({kind: "SUCCESS", content: "Data has been mapped: " + value}),
          error => this.msg.publish({kind: "ERROR", content: error.error}))
      );
  }

  public getCurrentUpdateReport(): Observable<SchemaUpdateDto>
  {
    this.msg.publish({kind: "INFO", content: "Retrieving last created update report..."});

    return this.http
      .get<SchemaUpdateDto>('api/schema/internal/update/changes')
      .pipe(
        tap(
          (_value: SchemaUpdateDto) => this.msg.publish({kind: "SUCCESS", content: "Update report retrieved"}),
          error => this.msg.publish({kind: "ERROR", content: error.error}))
      );
  }

  public initUpdate(): Observable<SchemaUpdateDto>
  {
    this.msg.publish({kind: "INFO", content: "Initialising schema update..."});

    return this.http
      .put<SchemaUpdateDto>('api/schema/internal/update/init', null)
      .pipe(
        tap(
          (_value: SchemaUpdateDto ) => this.msg.publish({kind: "SUCCESS", content: "Schema update initialised"}),
          error => this.msg.publish({kind: "ERROR", content: error.error}))
      );
  }

  public mapData(mappingRequest: SchemaUpdateDataMappingRequest): Observable<void>
  {
    this.msg.publish({kind: "INFO", content: "Mapping data..."});

    return this.http
      .put<void>('api/schema/internal/update/data/map', mappingRequest)
      .pipe(
        tap(
          (_value: void) => this.msg.publish({kind: "SUCCESS", content: "Data mapped successfully"}),
          error => this.msg.publish({kind: "ERROR", content: error.error}))
      );
  }

  public rollbackDataMapping(): Observable<void>
  {
    this.msg.publish({kind: "INFO", content: "Rolling back data mapping..."});

    return this.http
      .put<void>('api/schema/internal/update/data/rollback', null)
      .pipe(
        tap(
          (_value: void) => this.msg.publish({kind: "SUCCESS", content: "Data mapping rolled back"}),
          error => this.msg.publish({kind: "ERROR", content: error.error}))
      );
  }

  public cancelUpdate(): Observable<void>
  {
    this.msg.publish({kind: "INFO", content: "Cancelling schema update..."});

    return this.http
      .put<void>('api/schema/internal/update/cancel', null)
      .pipe(
        tap(
          (_value: void) => this.msg.publish({kind: "SUCCESS", content: "Schema update cancelled"}),
          error => this.msg.publish({kind: "ERROR", content: error.error}))
      );
  }

  public commitUpdate(): Observable<void>
  {
    this.msg.publish({kind: "INFO", content: "Committing schema update..."});

    return this.http
      .put<void>('api/schema/internal/update/commit', null)
      .pipe(
        tap(
          (_value: void) =>
          {
            this.msg.publish({kind: "SUCCESS", content: "Schema update committed successfully"});
            this.schemaService.loadSchema();
          },
          error => this.msg.publish({kind: "ERROR", content: error.error}))
      );
  }
}
