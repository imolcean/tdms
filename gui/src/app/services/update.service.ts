import { Injectable } from '@angular/core';
import {BehaviorSubject, forkJoin, Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {MessageService} from "./message.service";
import {SchemaUpdateDataMappingRequest, SchemaUpdateDto} from "../dto/dto";

interface UpdateStep
{
  updateInProgress: boolean,
  dataMapped: boolean
}

@Injectable({
  providedIn: 'root'
})
export class UpdateService
{
  private updateStep$: BehaviorSubject<number | undefined>;
  private currentUpdateReport$: BehaviorSubject<SchemaUpdateDto | undefined>

  constructor(private http: HttpClient,
              private msg: MessageService)
  {
    this.updateStep$ = new BehaviorSubject<number | undefined>(undefined);
    this.currentUpdateReport$ = new BehaviorSubject<SchemaUpdateDto | undefined>(undefined);
  }

  public getUpdateStep(): Observable<number | undefined>
  {
    return this.updateStep$.asObservable();
  }

  public getCurrentUpdateReport(): Observable<SchemaUpdateDto | undefined>
  {
    return this.currentUpdateReport$.asObservable();
  }

  public loadUpdateStep(): void
  {
    this.msg.publish({kind: "INFO", content: "Checking current step of schema update..."});

    forkJoin({
      updateInProgress: this.http.get<boolean>('api/schema/internal/update'),
      dataMapped: this.http.get<boolean>('api/schema/internal/update/data')
    })
      .subscribe((value: UpdateStep) =>
      {
        let step: number;

        if(!value.updateInProgress)
        {
          step = 0;
        }
        else
        {
          if(!value.dataMapped)
          {
            step = 1;
          }
          else
          {
            step = 3;
          }
        }

        this.updateStep$.next(step);

        if(step > 0)
        {
          this.loadCurrentUpdateReport();
        }

        this.msg.publish({kind: "SUCCESS", content: "Current step of schema update: " + step});
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }

  public loadCurrentUpdateReport(): void
  {
    this.msg.publish({kind: "INFO", content: "Checking what changes have been made during active schema update..."});

    this.http
      .get<SchemaUpdateDto>('api/schema/internal/update/changes')
      .subscribe((value: SchemaUpdateDto) =>
      {
        this.currentUpdateReport$.next(value);
        this.msg.publish({kind: "SUCCESS", content: "Changes of the active schema update loaded"});
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }

  public initUpdate(): void
  {
    this.msg.publish({kind: "INFO", content: "Initialising schema update"});

    this.http
      .put<SchemaUpdateDto>('api/schema/internal/update/init', null)
      .subscribe((_value: SchemaUpdateDto) =>
      {
        // this.currentUpdateReport$.next(value);
        this.loadUpdateStep();
        this.msg.publish({kind: "SUCCESS", content: "Schema update initialised"});
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }

  public mapData(migrationScript: SchemaUpdateDataMappingRequest): void
  {
    this.msg.publish({kind: "INFO", content: "Applying migration script"});

    this.http
      .put('api/schema/internal/update/data/map', migrationScript)
      .subscribe(_value =>
      {
        this.updateStep$.next(3);
        this.msg.publish({kind: "SUCCESS", content: "Migration script applied"});
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }

  public rollbackDataMapping(): void
  {
    this.msg.publish({kind: "INFO", content: "Rolling back migration scripts..."});

    this.http
      .put('api/schema/internal/update/data/rollback', null)
      .subscribe(_value =>
      {
        this.updateStep$.next(1);
        this.msg.publish({kind: "SUCCESS", content: "Migration scripts rolled back"});
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }

  public cancelUpdate(): void
  {
    this.msg.publish({kind: "INFO", content: "Cancelling schema update..."});

    this.http
      .put('api/schema/internal/update/cancel', null)
      .subscribe(_value =>
      {
        this.updateStep$.next(0);
        this.msg.publish({kind: "SUCCESS", content: "Schema update cancelled successfully"});
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }

  public commitUpdate(): void
  {
    this.msg.publish({kind: "INFO", content: "Committing schema update..."});

    this.http
      .put('api/schema/internal/update/commit', null)
      .subscribe(_value =>
      {
        this.updateStep$.next(0);
        this.msg.publish({kind: "SUCCESS", content: "Schema update committed successfully"});
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }
}
