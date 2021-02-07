import { Injectable } from '@angular/core';
import {ProjectDto, TableMetaDataDto} from "../dto/dto";
import {BehaviorSubject, Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {MessageService} from "./message.service";
import {ProjectService} from "./project.service";
import {StageSelectionService} from "./stage-selection.service";
import {tap} from "rxjs/operators";

@Injectable({
  providedIn: 'root',
})
export class SchemaService
{
  private internalTableNames$: BehaviorSubject<string[] | undefined>;
  private currentStageTableNames$: BehaviorSubject<string[] | undefined>;

  private project: ProjectDto | undefined;

  constructor(private http: HttpClient,
              private msg: MessageService,
              private projectService: ProjectService,
              private stageSelectionService: StageSelectionService)
  {
    this.internalTableNames$ = new BehaviorSubject<string[] | undefined>(undefined);
    this.currentStageTableNames$ = new BehaviorSubject<string[] | undefined>(undefined);

    this.projectService.getProject()
      .subscribe((value: ProjectDto | undefined) => {
        this.project = value;
        if(value !== undefined)
        {
          this.loadTableNamesInternal();
        }
        else
        {
          this.internalTableNames$.next(undefined);
        }
      });

    this.stageSelectionService.getCurrentStage()
      .subscribe((value: string | undefined) =>
      {
        if(value === undefined)
        {
          this.currentStageTableNames$.next(undefined);
          return;
        }

        this.loadTableNamesCurrentStage();
      });
  }

  public getInternalTableNames(): Observable<string[] | undefined>
  {
    return this.internalTableNames$.asObservable();
  }

  public getCurrentStageTableNames(): Observable<string[] | undefined>
  {
    return this.currentStageTableNames$.asObservable();
  }

  public getInternalTable(tableName: string): Observable<TableMetaDataDto>
  {
    this.msg.publish({kind: "INFO", content: "Loading table '" + tableName + "'..."});

    return this.http
      .get<TableMetaDataDto>('api/schema/table/internal/' + tableName)
      .pipe(
        tap(
          (_value: TableMetaDataDto) => this.msg.publish({kind: "SUCCESS", content: "Table loaded"}),
          error => this.msg.publish({kind: "ERROR", content: error.error})
        )
      );
  }

  public loadTableNamesInternal(): void
  {
    this.msg.publish({kind: "INFO", content: "Loading list of tables of internal DB..."});

    this.http
      .get<string[]>('api/schema/tables/internal')
      .subscribe((value: string[]) =>
      {
        this.internalTableNames$.next(value);
        this.msg.publish({kind: "SUCCESS", content: "List of tables of internal DB loaded"});
      },
      error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }

  public loadTableNamesCurrentStage(): void
  {
    this.msg.publish({kind: "INFO", content: "Loading list of tables of current stage..."});

    this.http
      .get<string[]>('api/schema/tables/current')
      .subscribe((value: string[]) =>
        {
          this.currentStageTableNames$.next(value);
          this.msg.publish({kind: "SUCCESS", content: "List of tables of current stage loaded"});
        },
        error =>
        {
          this.msg.publish({kind: "ERROR", content: error.error});
        });
  }

  public dropAllInternal(): void
  {
    this.msg.publish({kind: "INFO", content: "Clearing internal schema..."});

    this.http
      .delete('api/schema/internal')
      .subscribe(_value =>
      {
        this.internalTableNames$.next([]);
        this.msg.publish({kind: "SUCCESS", content: "Internal schema cleared"});
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }

  public copySchemaFromInternalToCurrentStage(): void
  {
    this.msg.publish({kind: "INFO", content: "Clearing schema of current stage..."});

    this.http
      .delete('api/schema/current')
      .subscribe(_value =>
      {
        this.currentStageTableNames$.next([]);

        this.msg.publish({kind: "SUCCESS", content: "Schema of current stage cleared"});
        this.msg.publish({kind: "INFO", content: "Applying schema to current stage..."});

        this.http
          .put('api/schema/copy/internal/current', null)
          .subscribe(_value =>
          {
            this.msg.publish({kind: "SUCCESS", content: "Schema applied"});
            this.loadTableNamesCurrentStage();
          }, error =>
          {
            this.msg.publish({kind: "ERROR", content: error.error});
          });
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }
}
