import {Component, OnInit} from '@angular/core';
import {MenuItem} from "primeng/api";
import {ImportComponent} from "./dialogs/import/import.component";
import {DialogService} from "primeng/dynamicdialog";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit
{
  public items: MenuItem[];

  constructor(private dialogService: DialogService)
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
          {label: 'Import', icon: 'pi pi-arrow-right', command: e => this.onShowImport()},
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

  public onShowImport() {
    this.dialogService.open(ImportComponent, {
      header: 'Generic dialog',
      width: '70%',
      dismissableMask: false,
      closable: false
    });
  }
}
