/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 2.20.583 on 2020-06-29 11:26:56.

export interface DataSourceDto {
    driverClassName: string;
    url: string;
    user: string;
    password: string;
}

export interface StatusMessageDto {
    kind: StatusMessageDtoKind;
    content: string;
    addition?: string;
}

export interface TableMetaDataDto {
    name: string;
    columns: TableMetaDataDtoColumn[];
}

export interface TableMetaDataDtoColumn {
    name: string;
    type: string;
    primaryKey: boolean;
}

export type StatusMessageDtoKind = "INFO" | "SUCCESS" | "WARNING" | "ERROR";
