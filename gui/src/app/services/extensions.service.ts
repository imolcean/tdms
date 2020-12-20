import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {MessageService} from "./message.service";
import {BehaviorSubject, forkJoin, Observable} from "rxjs";

export interface ExtensionsMap
{
  exporters: string[],
  importers: string[],
  generators: string[],
  deployers: string[],
  updaters: string[]
}

@Injectable({
  providedIn: 'root'
})
export class ExtensionsService
{
  private available$: BehaviorSubject<ExtensionsMap | undefined>

  constructor(private http: HttpClient,
              private msg: MessageService)
  {
    this.available$ = new BehaviorSubject<ExtensionsMap | undefined>(undefined);
  }

  public getAvailable(): Observable<ExtensionsMap | undefined>
  {
    return this.available$.asObservable();
  }

  public loadAvailable(): void
  {
    this.msg.publish({kind: "INFO", content: "Loading available extensions..."});

    forkJoin({
      exporters: this.http.get<string[]>('api/data-exporters/'),
      importers: this.http.get<string[]>('api/data-importers/'),
      generators: this.http.get<string[]>('api/data-generators/'),
      deployers: this.http.get<string[]>('api/deployers/'),
      updaters: this.http.get<string[]>('api/schema-updaters/')
    })
      .subscribe((value: ExtensionsMap) =>
      {
        this.available$.next(value);
        this.msg.publish({kind: "SUCCESS", content: "Available extensions loaded"});
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }
}
