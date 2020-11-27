import { Component, OnInit } from '@angular/core';
import {StatusMessageDto} from "../../dto/dto";
import {MessageService} from "../../services/message.service";

@Component({
  selector: 'app-status',
  templateUrl: './status.component.html',
  styleUrls: ['./status.component.scss']
})
export class StatusComponent implements OnInit
{
  public messages: StatusMessageDto[] = [];

  constructor(private messageService: MessageService)
  {
    this.messageService.getMessages()
      .subscribe((value: StatusMessageDto) => this.messages.push(value));
  }

  ngOnInit(): void {}
}
