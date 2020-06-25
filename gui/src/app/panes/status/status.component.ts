import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-status',
  templateUrl: './status.component.html',
  styleUrls: ['./status.component.scss']
})
export class StatusComponent implements OnInit {

  public messages: string[] = [];

  ngOnInit(): void {
    for(let i = 0; i < 50; i++) {
      this.messages.push("Message " + i);
    }
  }

}
