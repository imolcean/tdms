import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {MessageService} from "./message.service";

@Injectable({
  providedIn: 'root'
})
export class StageSelectionService
{
  private currentStage$: BehaviorSubject<string | undefined>;

  constructor(private http: HttpClient,
              private msg: MessageService)
  {
    this.currentStage$ = new BehaviorSubject<string | undefined>(undefined);
  }

  public getCurrentStage(): Observable<string | undefined>
  {
    return this.currentStage$.asObservable();
  }

  public loadCurrentStage(): void
  {
    this.msg.publish({kind: "INFO", content: "Loading current stage..."});

    this.http
      .get('api/stage/current', {responseType: "text"})
      .subscribe((value: string) =>
      {
        this.currentStage$.next(value);
        this.msg.publish({kind: "SUCCESS", content: "Current stage loaded"});
      }, error =>
        this.msg.publish({kind: "ERROR", content: error.error}));
  }

  public selectCurrentStage(stage: string): void
  {
    this.msg.publish({kind: "INFO", content: "Changing current stage..."});

    this.http
      .put('api/stage/current/' + stage, null, {responseType: "text"})
      .subscribe((value: string) =>
      {
        this.currentStage$.next(value);
        this.msg.publish({kind: "SUCCESS", content: "Current stage changed"});
      }, error =>
        this.msg.publish({kind: "ERROR", content: error.error}));
  }

  public clearCurrentStage(): void
  {
    this.msg.publish({kind: "INFO", content: "Clearing current stage selection..."});

    this.http
      .delete('api/stage/current')
      .subscribe(_value =>
      {
        this.currentStage$.next(undefined);
        this.msg.publish({kind: "SUCCESS", content: "Current stage selection cleared"});
      }, error =>
        this.msg.publish({kind: "ERROR", content: error.error}));
  }
}
