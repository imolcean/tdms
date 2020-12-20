import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {MessageService} from "./message.service";

export interface PathsMap
{
  [key: string]: string;
}

@Injectable({
  providedIn: 'root'
})
export class PathsService
{
  private paths$: BehaviorSubject<PathsMap | undefined>;

  constructor(private http: HttpClient,
              private msg: MessageService)
  {
    this.paths$ = new BehaviorSubject<PathsMap | undefined>(undefined);
  }

  public getPaths(): Observable<object | undefined>
  {
    return this.paths$.asObservable();
  }

  public loadPaths(): void
  {
    this.msg.publish({kind: "INFO", content: "Loading system paths..."});

    this.http
      .get<PathsMap>('api/paths/')
      .subscribe((value: PathsMap) =>
      {
        this.paths$.next(value);
        this.msg.publish({kind: "SUCCESS", content: "System paths loaded"});
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }
}
