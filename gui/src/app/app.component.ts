import {Component, OnInit} from '@angular/core';
import {MenuItem} from "primeng/api";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  public items: MenuItem[];

  ngOnInit(): void {
    this.items = [
      {
        label: "File",
        items: [
          {label: 'New', icon: 'pi pi-plus'},
          {label: 'Open', icon: 'pi pi-download'},
          {label: 'Settings', icon: 'pi pi-cog'}
        ]
      },
      {
        label: "Edit",
        items: [
          {label: 'Undo', icon: 'pi pi-replay'},
          {label: 'Redo', icon: 'pi pi-refresh'}
        ]
      },
      {
        label: "Tools",
        items: [
          {label: 'Connections', icon: 'pi pi-sitemap'},
          {label: 'Plugins', icon: 'pi pi-th-large'}
        ]
      }
    ];
  }

}
