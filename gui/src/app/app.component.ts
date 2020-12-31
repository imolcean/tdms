import {Component, OnInit} from '@angular/core';
import {ConfirmationService, MenuItem} from "primeng/api";
import {ImportComponent} from "./dialogs/import/import.component";
import {DialogService} from "primeng/dynamicdialog";
import {ExportComponent} from "./dialogs/export/export.component";
import {StagesComponent} from "./dialogs/stages/stages.component";
import {ProjectProfileComponent} from "./dialogs/project/profile/project-profile.component";
import {ProjectService} from "./services/project.service";
import {ProjectOpenComponent} from "./dialogs/project/open/project-open.component";
import {ProjectDto} from "./dto/dto";
import {ExtensionsService} from "./services/extensions.service";
import {PathsService} from "./services/paths.service";
import {UpdateComponent} from "./dialogs/update/update.component";
import {SchemaService} from "./services/schema.service";
import {DataService} from "./services/data.service";
import {GenerationComponent} from "./dialogs/generation/generation.component";
import {StageSelectionService} from "./services/stage-selection.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit
{
  public menuItems: MenuItem[];
  public project: ProjectDto | undefined;

  constructor(private dialogService: DialogService,
              private confirmationService: ConfirmationService,
              private projectService: ProjectService,
              private extensionsService: ExtensionsService,
              private pathsService: PathsService,
              private schemaService: SchemaService,
              private dataService: DataService,
              private stageSelectionService: StageSelectionService)
  {
    this.menuItems = this.getMenuContent();

    this.projectService.getProject()
      .subscribe((value: ProjectDto | undefined) => {
        this.project = value;
        this.menuItems = this.getMenuContent();
      });
  }

  ngOnInit(): void
  {
    this.pathsService.loadPaths();
    this.extensionsService.loadAvailable();
    this.dataService.loadValueLists();
    this.stageSelectionService.loadCurrentStage();
    this.projectService.loadProject();
  }

  private getMenuContent(): MenuItem[]
  {
    return [
      {
        label: "Project",
        items: [
          {label: 'New', icon: 'pi pi-plus', command: _e => this.onNewProject(), disabled: this.project !== undefined},
          {label: 'Open', icon: 'pi pi-upload', command: _e => this.onOpenProject(), disabled: this.project !== undefined},
          {label: 'Save', icon: 'pi pi-download', command: _e => this.onSaveProject(), disabled: this.project === undefined},
          {label: 'Close', icon: 'pi pi-times', command: _e => this.onCloseProject(), disabled: this.project === undefined},
          {label: 'Properties', icon: 'pi pi-cog', command: _e => this.onShowProject(), disabled: this.project === undefined}
        ]
      },
      {
        label: "Schema",
        items: [
          {label: 'Drop all', icon: 'pi pi-times', command: _e => this.onShowDropSchema(), disabled: this.project === undefined},
          {label: 'Schema update', icon: 'pi pi-briefcase', command: _e => this.onShowUpdate(), disabled: this.project === undefined}
        ]
      },
      {
        label: "Data",
        items: [
          {label: 'Clear all', icon: 'pi pi-times', command: _e => this.onShowClearData(), disabled: this.project === undefined},
          {label: 'Import', icon: 'pi pi-arrow-left', command: _e => this.onShowImport(), disabled: this.project === undefined},
          {label: 'Export', icon: 'pi pi-arrow-right', command: _e => this.onShowExport(), disabled: this.project === undefined},
          {label: 'Generation', icon: 'pi pi-briefcase', command: _e => this.onShowGeneration(), disabled: this.project === undefined}
        ]
      },
      {
        label: "Connections",
        items: [
          {label: 'Internal', icon: 'pi pi-desktop', command: _e => this.onShowInternalDs(), disabled: this.project === undefined},
          {label: 'Stages', icon: 'pi pi-globe', command: _e => this.onShowStages()},
        ]
      },
      {
        label: "Help",
        items: [
          {label: 'About TDMS', icon: 'pi pi-info'},
          {label: 'Plugins', icon: 'pi pi-th-large'}
        ]
      }
    ];
  }

  private onNewProject(): void
  {
    this.dialogService.open(ProjectProfileComponent, {
      header: 'Create project',
      width: '30%',
      dismissableMask: false,
      closable: false,
      data: {
        creating: true
      }
    });
  }

  private onOpenProject(): void
  {
    this.dialogService.open(ProjectOpenComponent, {
      header: 'Open project',
      width: '30%',
      dismissableMask: false,
      closable: false
    });
  }

  private onSaveProject(): void
  {
    this.projectService.saveProject();
  }

  private onCloseProject(): void
  {
    this.projectService.closeProject();
  }

  private onShowProject(): void
  {
    this.dialogService.open(ProjectProfileComponent, {
      header: 'Project profile',
      width: '50%',
      dismissableMask: false,
      closable: false
    });
  }

  private onShowImport(): void
  {
    this.dialogService.open(ImportComponent, {
      header: 'Data import',
      width: '30%',
      dismissableMask: false,
      closable: false
    });
  }

  private onShowExport(): void
  {
    this.dialogService.open(ExportComponent, {
      header: 'Data export',
      width: '30%',
      dismissableMask: false,
      closable: false
    });
  }

  private onShowInternalDs(): void
  {
    this.dialogService.open(ProjectProfileComponent, {
      header: 'Internal database',
      width: '30%',
      dismissableMask: false,
      closable: false,
      data: {
        tabIndex: 1
      }
    });
  }

  private onShowStages(): void
  {
    this.dialogService.open(StagesComponent, {
      header: 'Stages',
      dismissableMask: false,
      closable: false
    });
  }

  private onShowUpdate(): void
  {
    this.dialogService.open(UpdateComponent, {
      header: 'Schema update',
      width: '70%',
      dismissableMask: false,
      closable: false
    });
  }

  private onShowGeneration(): void
  {
    this.dialogService.open(GenerationComponent, {
      header: 'Data generation',
      width: '50%',
      dismissableMask: false,
      closable: false
    });
  }

  private onShowDropSchema(): void
  {
    this.confirmationService.confirm({
      message: 'Are you sure that you want to drop all tables?',
      accept: () => this.schemaService.dropAllInternal()
    });
  }

  private onShowClearData(): void
  {
    this.confirmationService.confirm({
      message: 'Are you sure that you want to remove data from all tables?',
      accept: () => this.dataService.clearAll()
    });
  }
}
