import { Component, OnInit } from '@angular/core';
import {DynamicDialogConfig, DynamicDialogRef} from "primeng/dynamicdialog";
import {ProjectService} from "../../services/project.service";
import {SchemaService} from "../../services/schema.service";
import {
  ProjectDto,
  TableMetaDataDto,
  TableMetaDataDtoColumn,
  TableRuleDto,
  TableRuleDtoColumnRuleDto
} from "../../dto/dto";
import {SelectItem, SelectItemGroup} from "primeng/api";
import {DataService} from "../../services/data.service";

@Component({
  selector: 'app-generation',
  templateUrl: './generation.component.html',
  styleUrls: ['./generation.component.scss']
})
export class GenerationComponent implements OnInit
{
  public loading: boolean = false;

  public project: ProjectDto | undefined;
  public tables: TableMetaDataDto[] | undefined;

  public fillModeOptions: string[] = ["APPEND", "UPDATE"];
  public capitalizationOptions: string[] = ["MIXED", "LOWER", "UPPER", "FIRST_UPPER"];
  public generationMethodOptions: SelectItemGroup[] = [
    {
      label: 'Ignore', value: 'ignore',
      items: [
        {label: 'Ignore', value: 'ignore'},
      ]
    },
    {
      label: 'Number', value: 'number',
      items: [
        {label: 'Byte', value: 'ByteGenerationMethod'},
        {label: 'Short', value: 'ShortGenerationMethod'},
        {label: 'Tiny Int', value: 'TinyIntGenerationMethod'},
        {label: 'Integer', value: 'IntegerGenerationMethod'},
        {label: 'Long', value: 'LongGenerationMethod'},
        {label: 'Big Decimal', value: 'BigDecimalGenerationMethod'},
        {label: 'Float', value: 'FloatGenerationMethod'},
        {label: 'Double', value: 'DoubleGenerationMethod'}
      ]
    },
    {
      label: 'Timeline', value: 'timeline',
      items: [
        {label: 'Date', value: 'DateGenerationMethod'},
        {label: 'Time', value: 'TimeGenerationMethod'},
        {label: 'Timestamp', value: 'TimestampGenerationMethod'}
      ]
    },
    {
      label: 'String', value: 'string',
      items: [
        {label: 'String', value: 'StringGenerationMethod'},
        {label: 'Regex', value: 'RegexGenerationMethod'}
      ]
    },
    {
      label: 'Other', value: 'other',
      items: [
        {label: 'Boolean', value: 'BooleanGenerationMethod'},
        {label: 'Foreign key', value: 'FkGenerationMethod'},
        {label: 'Value List', value: 'ValueListGenerationMethod'},
        {label: 'Formula', value: 'FormulaGenerationMethod'}
      ]
    },
  ];

  public rules: TableRuleDto[];
  public currentTableRule: TableRuleDto | undefined;
  public currentColumnRule: TableRuleDtoColumnRuleDto | undefined;
  public currentGenerationMethodType: string | undefined;

  public currentTable: TableMetaDataDto | undefined;
  public currentColumn: TableMetaDataDtoColumn | undefined;

  constructor(private ref: DynamicDialogRef,
              private config: DynamicDialogConfig,
              private projectService: ProjectService,
              private schemaService: SchemaService,
              private dataService: DataService)
  {
    this.projectService.getProject()
      .subscribe((value: ProjectDto | undefined) => this.project = value)

    this.schemaService.getSchema()
      .subscribe((value: TableMetaDataDto[] | undefined) => this.tables = value);

    // TODO: Get rules from local storage
    this.rules = [];
  }

  ngOnInit(): void {}

  public onTableSelect($event: any): void
  {
    delete this.currentColumn;
    delete this.currentColumnRule;
    delete this.currentGenerationMethodType;

    this.currentTable = ($event.value as TableMetaDataDto);

    const existingRule: TableRuleDto | undefined =
      this.rules.find((value: TableRuleDto) => value.tableName === this.currentTable!.name);

    if(existingRule)
    {
      this.currentTableRule = existingRule;
    }
    else
    {
      this.currentTableRule = {
        tableName: this.currentTable.name,
        fillMode: "APPEND",
        rowCountTotalOrMin: 1,
        rowCountMax: undefined,
        columnRules: []
      };

      this.rules.push(this.currentTableRule);
    }
  }

  public onColumnSelect($event: any): void
  {
    if($event.value === null)
    {
      delete this.currentColumn;
      delete this.currentColumnRule;
      return;
    }

    this.currentColumn = ($event.value as TableMetaDataDtoColumn);

    const existingRule: TableRuleDtoColumnRuleDto | undefined =
      this.currentTableRule!.columnRules.find((value: TableRuleDtoColumnRuleDto) => value.columnName === this.currentColumn!.name);

    if(existingRule)
    {
      this.currentColumnRule = existingRule;
      this.currentGenerationMethodType = this.getGenerationMethodTypeByName(this.currentColumnRule.generationMethodName);
    }
    else
    {
      this.currentColumnRule = {
        columnName: this.currentColumn.name,
        generationMethodName: "ignore",
        uniqueValues: false,
        nullPart: 0.0,
        params: {}
      };

      delete this.currentGenerationMethodType;

      this.currentTableRule!.columnRules.push(this.currentColumnRule);
    }
  }

  public onGenerationMethodSelect($event: any): void
  {
    const method: string = ($event.value as string);

    this.currentGenerationMethodType = this.getGenerationMethodTypeByName(method);
    this.currentColumnRule!.params = this.createParamsByGenerationMethodType(this.currentGenerationMethodType);
    this.currentColumnRule!.generationMethodName = method;
  }

  public onConfirm(): void
  {
    if(this.rules.length === 0)
    {
      this.ref.close(false);
    }

    this.loading = true;
    this.dataService.generate(this.rules)
      .subscribe(_value => this.ref.close(true), _error => this.loading = false);
  }

  public onCancel(): void
  {
    this.ref.close(false);
  }

  private getGenerationMethodTypeByName(methodName: string): string | undefined
  {
    const isNumberMethod: boolean = this.generationMethodOptions
      .filter((value:SelectItemGroup) => value.value === 'number')
      [0]
      .items
      .find((value: SelectItem) => value.value === methodName) !== undefined;

    const isTimelineMethod: boolean = this.generationMethodOptions
      .filter((value:SelectItemGroup) => value.value === 'timeline')
      [0]
      .items
      .find((value: SelectItem) => value.value === methodName) !== undefined;

    if(isNumberMethod)
    {
      return 'number';
    }
    else if(isTimelineMethod && methodName !== 'TimeGenerationMethod')
    {
      return 'date';
    }
    else if(methodName === 'TimeGenerationMethod')
    {
      return 'time';
    }
    else if(methodName === 'StringGenerationMethod')
    {
      return 'string';
    }
    else if(methodName === 'RegexGenerationMethod')
    {
      return 'regex';
    }
    else if(methodName === 'ValueListGenerationMethod')
    {
      return 'list';
    }
    else if(methodName === 'FormulaGenerationMethod')
    {
      return 'formula';
    }

    return undefined;
  }

  private createParamsByGenerationMethodType(type: string | undefined): object
  {
    switch(type)
    {
      case 'number':
        return {
          min: null,
          max: null
        };
      case 'timeline':
        return {
          min: null,
          max: null
        };
      case 'string':
        return {
          minLength: null,
          maxLength: null,
          capitalization: "MIXED"
        };
      case 'regex':
        return {
          pattern: null
        };
      case 'list':
        return {
          options: []
        };
      case 'formula':
        return {
          formula: null
        };
      default:
        return {};
    }
  }
}
