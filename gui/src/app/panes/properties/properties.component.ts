import { Component, OnInit } from '@angular/core';
import {PropertiesService, Property} from "../../services/properties.service";

@Component({
  selector: 'app-properties',
  templateUrl: './properties.component.html',
  styleUrls: ['./properties.component.scss']
})
export class PropertiesComponent implements OnInit
{
  public properties: Property[];

  constructor(private propertiesService: PropertiesService)
  {
    this.properties = [];
    this.propertiesService.getProperties()
      .subscribe((value: Property[]) => this.properties = value);
  }

  ngOnInit(): void {}
}
