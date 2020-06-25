import { Component, OnInit } from '@angular/core';
import {StatusMessageDto, StatusMessageDtoKind} from "../../dto/dto";

@Component({
  selector: 'app-status',
  templateUrl: './status.component.html',
  styleUrls: ['./status.component.scss']
})
export class StatusComponent implements OnInit {

  public messages: StatusMessageDto[] = [];

  ngOnInit(): void {
    for(let i = 0; i < 50; i++) {
      const msg: StatusMessageDto = {
        kind: i%2 === 0 ? "WARNING" : "SUCCESS" as StatusMessageDtoKind,
        content: "Message " + i
      };

      this.messages.push(msg);
    }
  }

}
