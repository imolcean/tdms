import { Injectable } from '@angular/core';
import {ProjectDto, TableMetaDataDto} from "../dto/dto";
import {BehaviorSubject, Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {MessageService} from "./message.service";
import {tap} from "rxjs/operators";
import {ProjectService} from "./project.service";

@Injectable({
  providedIn: 'root',
})
export class SchemaService
{
  private schema$: BehaviorSubject<TableMetaDataDto[] | undefined>;

  private project: ProjectDto | undefined;

  constructor(private http: HttpClient,
              private msg: MessageService,
              private projectService: ProjectService)
  {
    this.schema$ = new BehaviorSubject<TableMetaDataDto[] | undefined>(undefined);
    this.projectService.getProject()
      .subscribe((value: ProjectDto | undefined) => {
        this.project = value;
        if(value !== undefined)
        {
          this.loadSchema();
        }
        else
        {
          this.schema$.next(undefined);
        }
      });
  }

  public getSchema(): Observable<TableMetaDataDto[] | undefined>
  {
    return this.schema$.asObservable();
  }

  public loadSchema(): void
  {
    if(this.project === undefined)
    {
      this.msg.publish({kind: "WARNING", content: "There is no open project"});
      return;
    }

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

  public dropAll(): void
  {
    this.msg.publish({kind: "INFO", content: "Clearing schema..."});

    this.http
      .delete('api/schema/internal')
      .subscribe(_value =>
      {
        this.schema$.next([]);
        this.msg.publish({kind: "SUCCESS", content: "Schema cleared"});
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }
}
