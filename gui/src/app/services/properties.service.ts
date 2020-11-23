import {Injectable} from '@angular/core';
import {TableMetaDataDto, TableMetaDataDtoColumn} from "../dto/dto";
import {HttpClient} from "@angular/common/http";
import {BehaviorSubject, Observable} from "rxjs";

export interface Property
{
  key: string,
  value: string | number | boolean
}

@Injectable({
  providedIn: 'root',
})
export class PropertiesService
{
  private properties$: BehaviorSubject<Property[]>;

  constructor(private http: HttpClient)
  {
    this.properties$ = new BehaviorSubject<Property[]>([]);
  }

  public getProperties(): Observable<Property[]>
  {
    return this.properties$.asObservable();
  }

  public clearProperties(): void
  {
    this.properties$.next([]);
  }

  public selectPropertiesFromTable(obj: TableMetaDataDto): void
  {
    const props: Property[] = [
      {key: "Name", value: obj.name},
      {key: "Columns", value: obj.columns.length},
      {key: "Primary key", value: obj.pk !== null},
      {key: "Foreign keys", value: obj.fks.length}
    ];

    this.properties$.next(props);
  }

  public selectPropertiesFromColumn(obj: TableMetaDataDtoColumn): void
  {
    const props: Property[] = [
      {key: "Name", value: obj.name},
      {key: "Type", value: obj.type},
      {key: "Nullable", value: obj.nullable}
    ];

    this.properties$.next(props);
  }
}
