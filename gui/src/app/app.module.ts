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
import { InputTextModule } from "primeng/inputtext";

import { AppComponent } from './app.component';
import { PropertiesComponent } from './panes/properties/properties.component';
import { SquareComponent } from './layouts/square/square.component';
import { ProjectComponent } from './panes/project/project.component';
import { StatusComponent } from './panes/status/status.component';
import { TableContentComponent } from './panes/table-content/table-content.component';
import { HttpClientModule } from "@angular/common/http";
import { FormsModule } from "@angular/forms";

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
    FormsModule,
    InputTextModule,
    DialogModule,
    NoopAnimationsModule,
    HttpClientModule,
    ToolbarModule,
    MenubarModule,
    ButtonModule,
    TableModule,
    AccordionModule,
    TreeModule,
    ContextMenuModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
