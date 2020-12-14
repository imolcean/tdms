import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable} from "rxjs";
import {ProjectDto} from "../dto/dto";
import {HttpClient} from "@angular/common/http";
import {MessageService} from "./message.service";
import * as FileSaver from 'file-saver';

@Injectable({
  providedIn: 'root'
})
export class ProjectService
{
  private project$: BehaviorSubject<ProjectDto | undefined>;

  constructor(private http: HttpClient, private msg: MessageService)
  {
    this.project$ = new BehaviorSubject<ProjectDto | undefined>(undefined);
  }

  public getProject(): Observable<ProjectDto | undefined>
  {
    return this.project$.asObservable();
  }

  public loadProject(silent: boolean = false): void
  {
    if(!silent)
    {
      this.msg.publish({kind: "INFO", content: "Loading project profile..."});
    }

    this.http
      .get<ProjectDto>('api/project/')
      .subscribe((value: ProjectDto) =>
      {
        this.project$.next(value);

        if(!silent)
        {
          this.msg.publish({kind: "SUCCESS", content: "Project profile loaded"});
        }
      }, error =>
      {
        if(!silent)
        {
          this.msg.publish({kind: "ERROR", content: error.error});
        }
      });
  }

  public openProject(file: File): void
  {
    this.msg.publish({kind: "INFO", content: "Opening project..."});

    const formData: FormData = new FormData();
    formData.append('file', file, file.name);

    this.http
      .post('api/project/', formData)
      .subscribe(_value =>
      {
        this.msg.publish({kind: "SUCCESS", content: "Project opened"});
        this.loadProject();
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }

  public saveProject(): void
  {
    this.msg.publish({kind: "INFO", content: "Saving project..."});

    this.http
      .get<any>('api/project/save')
      .subscribe((value: any) =>
      {
        this.msg.publish({kind: "SUCCESS", content: "Project saved"});
        const blob: Blob = new Blob([JSON.stringify(value)], {type: 'text/json'});

        FileSaver.saveAs(blob, this.project$.getValue()?.projectName + ".tdm.json");
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }

  public closeProject(): void
  {
    this.msg.publish({kind: "INFO", content: "Closing project..."});

    this.http
      .delete('api/project/')
      .subscribe(_value =>
      {
        this.msg.publish({kind: "SUCCESS", content: "Project closed"});
        this.project$.next(undefined);
      }, error =>
      {
        this.msg.publish({kind: "ERROR", content: error.error});
      });
  }
}
