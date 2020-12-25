import {Injectable} from "@angular/core";
import {Observable, Subject} from "rxjs";
import {StatusMessageDto} from "../dto/dto";

@Injectable({
  providedIn: 'root',
})
export class MessageService
{
  private messages$: Subject<StatusMessageDto>;

  constructor()
  {
    this.messages$ = new Subject<StatusMessageDto>();
  }

  public getMessages(): Observable<StatusMessageDto>
  {
    return this.messages$.asObservable();
  }

  public publish(msg: StatusMessageDto)
  {
    this.messages$.next(msg);

    if(msg.kind === "ERROR")
    {
      console.error(msg.content);
    }
    else
    {
      console.info(msg.content);
    }
  }
}
