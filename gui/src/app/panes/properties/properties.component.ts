import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-properties',
  templateUrl: './properties.component.html',
  styleUrls: ['./properties.component.scss']
})
export class PropertiesComponent implements OnInit {

  public properties: Object[];

  ngOnInit(): void {
    this.properties = [];

    for(let i = 0; i < 50; i++)
    {
      this.properties.push({key: "key", value: "value"});
    }
  }

}
