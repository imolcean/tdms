import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { NoopAnimationsModule } from "@angular/platform-browser/animations";
import { MenubarModule } from 'primeng/menubar';
import { TableModule } from 'primeng/table';
import { AccordionModule } from "primeng/accordion";
import { TreeModule } from 'primeng/tree';
import { ContextMenuModule } from "primeng/contextmenu";

import { AppComponent } from './app.component';
import { PropertiesComponent } from './panes/properties/properties.component';
import { SquareComponent } from './layouts/square/square.component';
import { ProjectComponent } from './panes/project/project.component';
import { StatusComponent } from './panes/status/status.component';
import { TableContentComponent } from './panes/table-content/table-content.component';
import { HttpClientModule } from "@angular/common/http";

@NgModule({
  declarations: [
    AppComponent,
    PropertiesComponent,
    SquareComponent,
    ProjectComponent,
    StatusComponent,
    TableContentComponent
  ],
  imports: [
    BrowserModule,
    NoopAnimationsModule,
    HttpClientModule,
    MenubarModule,
    TableModule,
    AccordionModule,
    TreeModule,
    ContextMenuModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
