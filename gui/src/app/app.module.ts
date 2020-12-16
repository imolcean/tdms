import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { NoopAnimationsModule } from "@angular/platform-browser/animations";
import { MenubarModule } from 'primeng/menubar';
import { TableModule } from 'primeng/table';
import { AccordionModule } from "primeng/accordion";
import { TreeModule } from 'primeng/tree';
import { ContextMenuModule } from "primeng/contextmenu";
import { ButtonModule } from "primeng/button";
import { ToolbarModule } from "primeng/toolbar";
import { DialogModule } from "primeng/dialog";
import { DialogService, DynamicDialogModule } from "primeng/dynamicdialog";
import { ConfirmPopupModule } from "primeng/confirmpopup";
import { InputTextModule } from "primeng/inputtext";
import { ProgressSpinnerModule } from "primeng/progressspinner";
import { ListboxModule } from "primeng/listbox";
import { TabViewModule } from 'primeng/tabview';

import { AppComponent } from './app.component';
import { PropertiesComponent } from './panes/properties/properties.component';
import { SquareComponent } from './layouts/square/square.component';
import { ProjectComponent } from './panes/project/project.component';
import { StatusComponent } from './panes/status/status.component';
import { TableContentComponent } from './panes/table-content/table-content.component';
import { HttpClientModule } from "@angular/common/http";
import { FormsModule } from "@angular/forms";
import { ImportComponent } from './dialogs/import/import.component';
import { ExportComponent } from './dialogs/export/export.component';
import { StagesComponent } from './dialogs/stages/stages.component';
import { ProjectProfileComponent } from "./dialogs/project/profile/project-profile.component";
import { ProjectOpenComponent } from './dialogs/project/open/project-open.component';
import { DatasourceComponent } from './elements/datasource/datasource.component';

@NgModule({
  declarations: [
    AppComponent,
    PropertiesComponent,
    SquareComponent,
    ProjectComponent,
    StatusComponent,
    TableContentComponent,
    ImportComponent,
    ExportComponent,
    StagesComponent,
    ProjectProfileComponent,
    ProjectOpenComponent,
    DatasourceComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    InputTextModule,
    DialogModule,
    DynamicDialogModule,
    NoopAnimationsModule,
    HttpClientModule,
    ToolbarModule,
    MenubarModule,
    ButtonModule,
    TableModule,
    AccordionModule,
    TreeModule,
    ContextMenuModule,
    ConfirmPopupModule,
    ProgressSpinnerModule,
    ListboxModule,
    TabViewModule
  ],
  providers: [DialogService],
  bootstrap: [AppComponent]
})
export class AppModule { }
