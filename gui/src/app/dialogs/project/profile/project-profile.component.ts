import { Component, OnInit } from '@angular/core';
import {DataSourceDto, GitRepositoryDto, ProjectDto} from "../../../dto/dto";
import {DynamicDialogConfig, DynamicDialogRef} from "primeng/dynamicdialog";
import {ProjectService} from "../../../services/project.service";

@Component({
  selector: 'app-project-profile',
  templateUrl: './project-profile.component.html',
  styleUrls: ['./project-profile.component.scss']
})
export class ProjectProfileComponent implements OnInit
{
  public tabIndex: number;
  public project: ProjectDto | undefined;
  public editing: boolean;

  private backup: ProjectDto | undefined;

  constructor(private ref: DynamicDialogRef,
              private config: DynamicDialogConfig,
              private projectService: ProjectService)
  {
    this.editing = false;

    this.tabIndex = 0;
    if(this.config.data && this.config.data['tabIndex'])
    {
      this.tabIndex = this.config.data['tabIndex'];
    }

    this.projectService.getProject()
      .subscribe((value: ProjectDto | undefined) => this.project = value);
  }

  ngOnInit(): void {}

  public onOk()
  {
    this.ref.close(true);
  }

  public onEdit(): void
  {
    this.backup = this.cloneProject(this.project!);
    this.editing = true;
  }

  public onSaveEditing(): void
  {
    this.editing = false;
    delete this.backup;
    this.projectService.updateProject(this.project!);
  }

  public onCancelEditing(): void
  {
    this.editing = false;
    this.project = this.backup;
    delete this.backup;
  }

  private cloneProject(original: ProjectDto): ProjectDto
  {
    return {
      projectName: original.projectName,
      internal: {
        driverClassName: original.internal.driverClassName,
        url: original.internal.url,
        database: original.internal.database,
        username: original.internal.username,
        password: original.internal.password
      } as DataSourceDto,
      tmp: {
        driverClassName: original.tmp.driverClassName,
        url: original.tmp.url,
        database: original.tmp.database,
        username: original.tmp.username,
        password: original.tmp.password
      } as DataSourceDto,
      gitRepository: {
        url: original.gitRepository.url,
        dir: original.gitRepository.dir,
        token: original.gitRepository.token
      } as GitRepositoryDto,
      schemaUpdater: original.schemaUpdater,
      dataImporter: original.dataImporter,
      dataExporter: original.dataExporter,
      deployer: original.deployer,
      dataGenerator: original.dataGenerator,
      dataDir: original.dataDir
    } as ProjectDto
  }
}
