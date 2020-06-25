/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 2.20.583 on 2020-06-25 12:15:27.

export interface StatusMessageDto {
    kind: StatusMessageDtoKind;
    content: string;
    addition?: string;
}

export type StatusMessageDtoKind = "INFO" | "SUCCESS" | "WARNING" | "ERROR";
