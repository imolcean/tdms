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
import { ConfirmDialogModule } from "primeng/confirmdialog";
import { InputTextModule } from "primeng/inputtext";
import { ProgressSpinnerModule } from "primeng/progressspinner";
import { ListboxModule } from "primeng/listbox";
import { TabViewModule } from 'primeng/tabview';
import { DropdownModule } from "primeng/dropdown";
import { StepsModule } from "primeng/steps";
import { InputTextareaModule } from "primeng/inputtextarea";
import { InputNumberModule } from "primeng/inputnumber";
import { CheckboxModule } from "primeng/checkbox";

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
import { UpdateComponent } from './dialogs/update/update.component';
import {RouterModule, Routes} from "@angular/router";
import { UpdateVisualiserComponent } from './elements/update-visualiser/update-visualiser.component';
import { TableVisualiserComponent } from './elements/table-visualiser/table-visualiser.component';
import { MigrationFormComponent } from './elements/migration-form/migration-form.component';
import {ConfirmationService} from "primeng/api";
import { GenerationComponent } from './dialogs/generation/generation.component';

const appRoutes: Routes = [
  // { path: 'crisis-center', component: CrisisListComponent },
  // { path: 'hero/:id', component: HeroDetailComponent },
  // { path: '**', component: PageNotFoundComponent }
];

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
    DatasourceComponent,
    UpdateComponent,
    UpdateVisualiserComponent,
    TableVisualiserComponent,
    MigrationFormComponent,
    GenerationComponent
  ],
  imports: [
    RouterModule.forRoot([]),
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
    ConfirmDialogModule,
    ProgressSpinnerModule,
    ListboxModule,
    TabViewModule,
    DropdownModule,
    StepsModule,
    InputTextareaModule,
    InputNumberModule,
    CheckboxModule
  ],
  providers: [DialogService, ConfirmationService],
  bootstrap: [AppComponent]
})
export class AppModule { }
