import { Component, OnInit } from '@angular/core';
import { TreeNode, MenuItem } from "primeng/api";
import {TableMetaDataDto} from "../../dto/dto";
import {EMPTY, Observable} from "rxjs";
import {SchemaService} from "../../services/schema.service";
import {map, tap} from "rxjs/operators";
import {PropertiesService} from "../../services/properties.service";
import {TableService} from "../../services/table.service";

@Component({
  selector: 'app-project',
  templateUrl: './project.component.html',
  styleUrls: ['./project.component.scss']
})
export class ProjectComponent implements OnInit
{
  nodes: Observable<TreeNode[]>;
  schemaLoading: boolean;
  contextMenuItems: MenuItem[];

  constructor(private schemaService: SchemaService,
              private propertiesService: PropertiesService,
              private tableService: TableService)
  {
    this.nodes = EMPTY;
    this.schemaLoading = false;
    this.contextMenuItems = [
      { label: 'Action 1', icon: 'fa fa-search', command: (event) => console.log("Action 1 on " + event.item.state[0].data.name) },
      { label: 'Action 2', icon: 'fa fa-close', command: (event) => console.log("Action 2 on " + event.item.state[0].data.name) }
    ];
  }

  ngOnInit(): void
  {
    this.update();
  }

  public update(): void
  {
    this.schemaLoading = true;

    this.nodes = this.schemaService.getSchema().pipe(
      map((schema: TableMetaDataDto[]) => this.schema2TreeNodes(schema)),
      tap(() => this.schemaLoading = false)
    );
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

  private schema2TreeNodes(schema: TableMetaDataDto[]): TreeNode[]
  {
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
