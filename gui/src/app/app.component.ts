import {Component, OnInit} from '@angular/core';
import {MenuItem} from "primeng/api";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  public items: MenuItem[];

  constructor()
  {
    this.items = [
      {
        label: "Project",
        items: [
          {label: 'New', icon: 'pi pi-plus'},
          {label: 'Open', icon: 'pi pi-upload'},
          {label: 'Save', icon: 'pi pi-download'},
          {label: 'Close', icon: 'pi pi-times'},
          {label: 'Properties', icon: 'pi pi-cog'}
        ]
      },
      {
        label: "Data",
        items: [
          {label: 'Import', icon: 'pi pi-arrow-right'},
          {label: 'Export', icon: 'pi pi-arrow-left'},
          {label: 'Generation', icon: 'pi pi-briefcase'}
        ]
      },
      {
        label: "Connections",
        items: [
          {label: 'Internal', icon: 'pi pi-desktop'},
          {label: 'Stages', icon: 'pi pi-globe'},
        ]
      },
      {
        label: "Help",
        items: [
          {label: 'About TDMS', icon: 'pi pi-info'},
          {label: 'Plugins', icon: 'pi pi-th-large'}
        ]
      }
    ];
  }

  ngOnInit(): void {}

}
