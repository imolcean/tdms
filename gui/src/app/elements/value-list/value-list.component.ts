import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ValueListDto} from "../../dto/dto";
import {DataService} from "../../services/data.service";

@Component({
  selector: 'app-value-list',
  templateUrl: './value-list.component.html',
  styleUrls: ['./value-list.component.scss']
})
export class ValueListComponent implements OnInit
{
  public lists: ValueListDto[] | undefined;
  public currentList: ValueListDto | undefined;
  public empty: boolean = true;

  public addedOption: string = "";

  private _options: (string | number)[] = [];

  @Input()
  public required: boolean = false;

  @Input()
  public set options(value: (string | number)[])
  {
    this._options = value;
    this.empty = this._options.length === 0;
  }

  public get options(): (string | number)[]
  {
    return this._options;
  }

  @Output()
  public optionsChange: EventEmitter<(string | number)[]> = new EventEmitter<(string | number)[]>();

  constructor(private dataService: DataService)
  {
    this.dataService.getValueLists()
      .subscribe((value: ValueListDto[] | undefined) => this.lists = value);
  }

  ngOnInit(): void {}

  public onListChange($event: any): void
  {
    if($event.value === null)
    {
      delete this.currentList;
      this.options = [];
      this.optionsChange.emit(this.options);

      return;
    }

    this.currentList = $event.value;
    this.options = $event.value.options;

    this.optionsChange.emit(this.options);
  }

  public onAddOption($event: any): void
  {
    this.options.unshift(this.addedOption);
    this.addedOption = "";
  }

  public onRemoveOption(i: number): void
  {
    this.options.splice(i, 1);
  }
}
