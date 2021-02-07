import { Component, OnInit } from '@angular/core';
import { TreeNode } from "primeng/api";
import {ProjectDto, TableMetaDataDto, TableMetaDataDtoColumn} from "../../dto/dto";
import {Observable} from "rxjs";
import {SchemaService} from "../../services/schema.service";
import {map} from "rxjs/operators";
import {PropertiesService} from "../../services/properties.service";
import {DataService} from "../../services/data.service";
import {ProjectService} from "../../services/project.service";
import {StageSelectionService} from "../../services/stage-selection.service";

@Component({
  selector: 'app-project',
  templateUrl: './project.component.html',
  styleUrls: ['./project.component.scss']
})
export class ProjectComponent implements OnInit
{
  project$: Observable<ProjectDto | undefined>;

  internalTableNames$: Observable<string[] | undefined>;
  internalNodes$: Observable<TreeNode[]>;

  stage$: Observable<string | undefined>;
  stageTableNames$: Observable<string[] | undefined>;
  stageNodes$: Observable<TreeNode[]>;

  constructor(private projectService: ProjectService,
              private schemaService: SchemaService,
              private propertiesService: PropertiesService,
              private tableService: DataService,
              private stageSelectionService: StageSelectionService)
  {
    this.project$ = this.projectService.getProject();

    this.internalTableNames$ = schemaService.getInternalTableNames();
    this.internalNodes$ = this.internalTableNames$.pipe(
      map((tableNames: string[] | undefined) => this.tableNames2TreeNodes(tableNames))
    );

    this.stage$ = this.stageSelectionService.getCurrentStage();
    this.stageTableNames$ = this.schemaService.getCurrentStageTableNames();
    this.stageNodes$ = this.stageTableNames$.pipe(
      map((tableNames: string[] | undefined) => this.tableNames2TreeNodes(tableNames))
    );
  }

  ngOnInit(): void {}

  public update(): void
  {
    this.schemaService.loadTableNamesInternal();
  }

  public nodeExpand($event: any)
  {
    if($event.node.type === "table")
    {
      if(!$event.node.children)
      {
        this.schemaService.getInternalTable($event.node.label)
          .subscribe((value: TableMetaDataDto) =>
          {
            $event.node.data = value;
            $event.node.children = this.columns2treeNodes(value.columns);

            this.nodeSelect($event);
          });
      }
      else
      {
        this.nodeSelect($event);
      }
    }
  }

  public nodeSelect($event: any)
  {
    this.nodeUnselect();

    if($event.node.type === "column")
    {
      this.propertiesService.selectPropertiesFromColumn($event.node.data);
      return;
    }

    if($event.node.type === "table" && $event.node.data)
    {
      this.propertiesService.selectPropertiesFromTable($event.node.data);
    }
  }

  public nodeUnselect()
  {
    this.propertiesService.clearProperties();
  }

  public open(alias: string, node: TreeNode)
  {
    if(node.type === "table")
    {
      this.tableService.loadData(alias, node.label!);
    }
  }

  private tableNames2TreeNodes(tableNames: string[] | undefined): TreeNode[]
  {
    if(tableNames === undefined)
    {
      return [];
    }

    const tableNodes: TreeNode[] = [];

    for(let tableName of tableNames)
    {
      const node: TreeNode =
        {
          type: "table",
          data: undefined,
          label: tableName,
          icon: "pi pi-table",
          leaf: false
        };

      tableNodes.push(node);
    }

    return tableNodes;
  }

  private columns2treeNodes(columns: TableMetaDataDtoColumn[]): TreeNode[]
  {
    const columnNodes: TreeNode[] = [];

    for(let column of columns)
    {
      const node: TreeNode =
        {
          type: "column",
          data: column,
          label: column.name,
          icon: "pi pi-circle-off"
        };

      columnNodes.push(node);
    }

    return columnNodes;
  }
}
