import { Component, OnInit } from '@angular/core';
import { TreeNode, MenuItem } from "primeng/api";
import {TableMetaDataDto} from "../../dto/dto";
import {EMPTY, Observable} from "rxjs";
import {SchemaService} from "../../services/schema.service";
import {map, tap} from "rxjs/operators";

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

  constructor(private schemaService: SchemaService)
  {
    this.nodes = EMPTY;
    this.schemaLoading = false;
    this.contextMenuItems = [
      { label: 'Action 1', icon: 'fa fa-search', command: (event) => console.log("Called Action 1 on " + event.node) },
      { label: 'Action 2', icon: 'fa fa-close', command: (event) => console.log("Called Action 2 on " + event.node) }
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
    console.log("Selected " + $event.node);

    // TODO Show properties of selection
  }

  public nodeUnselect($event: any)
  {
    console.log("Selection cleared");

    // TODO Clear properties pane
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
            label: column.name,
            icon: "pi pi-circle-off"
          };

        columnNodes.push(node);
      }

      const node: TreeNode =
        {
          label: table.name,
          icon: "pi pi-table",
          children: columnNodes
        };

      tableNodes.push(node);
    }

    return tableNodes;
  }
}
