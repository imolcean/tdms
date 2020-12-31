import { Injectable } from '@angular/core';
import {ProjectDto, TableMetaDataDto} from "../dto/dto";
import {BehaviorSubject, Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {MessageService} from "./message.service";
import {tap} from "rxjs/operators";
import {ProjectService} from "./project.service";
import {StageSelectionService} from "./stage-selection.service";

@Injectable({
  providedIn: 'root',
})
export class SchemaService
{
  private internalSchema$: BehaviorSubject<TableMetaDataDto[] | undefined>;
  private currentStageSchema$: BehaviorSubject<TableMetaDataDto[] | undefined>;

  private project: ProjectDto | undefined;

  constructor(private http: HttpClient,
              private msg: MessageService,
              private projectService: ProjectService,
              private stageSelectionService: StageSelectionService)
  {
    this.internalSchema$ = new BehaviorSubject<TableMetaDataDto[] | undefined>(undefined);
    this.currentStageSchema$ = new BehaviorSubject<TableMetaDataDto[] | undefined>(undefined);

    this.projectService.getProject()
      .subscribe((value: ProjectDto | undefined) => {
        this.project = value;
        if(value !== undefined)
        {
          this.loadInternalSchema();
        }
        else
        {
          this.internalSchema$.next(undefined);
        }
      });

    this.stageSelectionService.getCurrentStage()
      .subscribe((value: string | undefined) =>
      {
        if(value === undefined)
        {
          this.currentStageSchema$.next(undefined);
          return;
        }

        this.loadCurrentStageSchema();
      });
  }

  public getInternalSchema(): Observable<TableMetaDataDto[] | undefined>
  {
    return this.internalSchema$.asObservable();
  }

  public getCurrentStageSchema(): Observable<TableMetaDataDto[] | undefined>
  {
    return this.currentStageSchema$.asObservable();
  }

  public loadInternalSchema(): void
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
        this.internalSchema$.next(value);
        this.msg.publish({kind: "SUCCESS", content: "Schema loaded"});
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }

  public loadCurrentStageSchema(): void
  {
    this.msg.publish({kind: "INFO", content: "Loading schema of current stage..."});

    this.http
      .get<TableMetaDataDto[]>('api/schema/current')
      .subscribe((value: TableMetaDataDto[]) =>
      {
        this.currentStageSchema$.next(value);
        this.msg.publish({kind: "SUCCESS", content: "Schema of current stage loaded"});
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }

  public getOccupiedTableNamesInternal(): Observable<string[]>
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

  public dropAllInternal(): void
  {
    this.msg.publish({kind: "INFO", content: "Clearing schema..."});

    this.http
      .delete('api/schema/internal')
      .subscribe(_value =>
      {
        this.internalSchema$.next([]);
        this.msg.publish({kind: "SUCCESS", content: "Schema cleared"});
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }
}
