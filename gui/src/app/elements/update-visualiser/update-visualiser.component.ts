import {Component, Input, OnInit} from '@angular/core';
import {SchemaUpdateDto} from "../../dto/dto";

@Component({
  selector: 'app-update-visualiser',
  templateUrl: './update-visualiser.component.html',
  styleUrls: ['./update-visualiser.component.scss']
})
export class UpdateVisualiserComponent implements OnInit
{
  @Input()
  public update: SchemaUpdateDto | undefined;

  constructor() {}

  ngOnInit(): void {}
}
