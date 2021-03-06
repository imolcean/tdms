package io.github.imolcean.tdms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceDto
{
    private String driverClassName;
    private String url;
    private String database;
    private String username;
    private String password;
}
