import { Component, OnInit } from '@angular/core';
import { TreeNode, MenuItem } from "primeng/api";
import {ProjectDto, TableMetaDataDto} from "../../dto/dto";
import {Observable} from "rxjs";
import {SchemaService} from "../../services/schema.service";
import {map} from "rxjs/operators";
import {PropertiesService} from "../../services/properties.service";
import {DataService} from "../../services/data.service";
import {ProjectService} from "../../services/project.service";

@Component({
  selector: 'app-project',
  templateUrl: './project.component.html',
  styleUrls: ['./project.component.scss']
})
export class ProjectComponent implements OnInit
{
  project$: Observable<ProjectDto | undefined>;
  schema$: Observable<TableMetaDataDto[] | undefined>;
  nodes$: Observable<TreeNode[]>;
  contextMenuItems: MenuItem[];

  constructor(private projectService: ProjectService,
              private schemaService: SchemaService,
              private propertiesService: PropertiesService,
              private tableService: DataService)
  {
    this.project$ = this.projectService.getProject();
    this.schema$ = schemaService.getSchema();
    this.nodes$ = this.schema$.pipe(
      map((schema: TableMetaDataDto[] | undefined) => this.schema2TreeNodes(schema))
    );

    this.contextMenuItems = [
      { label: 'Action 1', icon: 'fa fa-search', command: (event) => console.log("Action 1 on " + event.item.state[0].data.name) },
      { label: 'Action 2', icon: 'fa fa-close', command: (event) => console.log("Action 2 on " + event.item.state[0].data.name) }
    ];
  }

  ngOnInit(): void {}

  public update(): void
  {
    this.schemaService.loadSchema();
  }

  public nodeSelect($event: any)
  {
    switch($event.node.type)
    {
      case "table":
        this.propertiesService.selectPropertiesFromTable($event.node.data);
        break;
      case "column":
        this.propertiesService.selectPropertiesFromColumn($event.node.data);
        break;
    }
  }

  public nodeUnselect($event: any)
  {
    this.propertiesService.clearProperties();
  }

  public nodeCmSelect($event: any)
  {
    this.contextMenuItems.forEach((item: MenuItem) => item.state = [$event.node]);
  }

  public open(node: TreeNode)
  {
    if(node.type === "table")
    {
      this.tableService.loadData(node.data.name);
    }
  }

  private schema2TreeNodes(schema: TableMetaDataDto[] | undefined): TreeNode[]
  {
    if(schema === undefined)
    {
      return [];
    }

    const tableNodes: TreeNode[] = [];

    for(let table of schema)
    {
      const columnNodes: TreeNode[] = [];

      for(let column of table.columns)
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

      const node: TreeNode =
        {
          type: "table",
          data: table,
          label: table.name,
          icon: "pi pi-table",
          children: columnNodes
        };

      tableNodes.push(node);
    }

    return tableNodes;
  }
}
