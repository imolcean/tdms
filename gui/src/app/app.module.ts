import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import {NoopAnimationsModule} from "@angular/platform-browser/animations";
import {MenubarModule} from 'primeng/menubar';
import {TableModule} from 'primeng/table';

import { PropertiesComponent } from './panes/properties/properties.component';

@NgModule({
  declarations: [
    AppComponent,
    PropertiesComponent
  ],
  imports: [
    BrowserModule,
    NoopAnimationsModule,
    MenubarModule,
    TableModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
