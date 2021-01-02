import {Component, Input, OnInit} from '@angular/core';
import {SchemaUpdateDto} from "../../dto/dto";

@Component({
  selector: 'app-migration-form',
  templateUrl: './migration-form.component.html',
  styleUrls: ['./migration-form.component.scss']
})
export class MigrationFormComponent implements OnInit
{
  @Input()
  public update: SchemaUpdateDto | undefined;

  @Input()
  public model: {[tableName: string]: string;} | undefined;

  constructor() {}

  ngOnInit(): void {}
}
