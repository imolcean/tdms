/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 2.26.723 on 2020-12-27 10:46:53.

export interface DataSourceDto {
    driverClassName: string;
    url: string;
    database: string;
    username: string;
    password: string;
}

export interface GitRepositoryDto {
    url: string;
    dir: string;
    token: string;
}

export interface ProjectDto {
    projectName: string;
    internal: DataSourceDto;
    tmp: DataSourceDto;
    gitRepository: GitRepositoryDto;
    schemaUpdater: string;
    dataImporter: string;
    dataExporter: string;
    deployer: string;
    dataGenerator: string;
    dataDir: string;
    schemaUpdateDescriptor: string;
}

export interface SchemaUpdateDataMappingRequest {
    sqlMigrationTables: SchemaUpdateDataMappingRequestTableDataMigrationRequest[];
}

export interface SchemaUpdateDto {
    untouchedTables: string[];
    addedTables: TableMetaDataDto[];
    deletedTables: TableMetaDataDto[];
    changedTables: SchemaUpdateDtoComparison[];
}

export interface StageDto {
    name: string;
    datasource: DataSourceDto;
}

export interface StatusMessageDto {
    kind: StatusMessageDtoKind;
    content: string;
    addition?: string;
}

export interface TableContentDto {
    tableName: string;
    columnNames: string[];
    data: any[][];
}

export interface TableMetaDataDto {
    name: string;
    columns: TableMetaDataDtoColumn[];
    pk: TableMetaDataDtoPrimaryKey;
    fks: TableMetaDataDtoForeignKey[];
}

export interface TableRuleDto {
    tableName: string;
    fillMode: TableRuleDtoFillMode;
    rowCountTotalOrMin: number;
    rowCountMax?: number;
    columnRules: TableRuleDtoColumnRuleDto[];
}

export interface SchemaUpdateDataMappingRequestTableDataMigrationRequest {
    tableName: string;
    sql: string;
}

export interface SchemaUpdateDtoComparison {
    before: TableMetaDataDto;
    after: TableMetaDataDto;
}

export interface TableMetaDataDtoColumn {
    name: string;
    type: string;
    nullable: boolean;
    defaultValue: string;
}

export interface TableMetaDataDtoPrimaryKey {
    name: string;
    columnNames: string[];
}

export interface TableMetaDataDtoForeignKey {
    name: string;
    columnNames: string[];
    pkTableName: string;
    pkColumnNames: string[];
}

export interface TableRuleDtoColumnRuleDto {
    columnName: string;
    generationMethodName: string;
    uniqueValues: boolean;
    nullPart: number;
    params: { [index: string]: any };
}

export type StatusMessageDtoKind = "INFO" | "SUCCESS" | "WARNING" | "ERROR";

export type TableRuleDtoFillMode = "APPEND" | "UPDATE";
