package de.tu_berlin.imolcean.tdm.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

// TODO Encrypted password?

@Data
@AllArgsConstructor
public class DataSourceDto
{
    private String driverClassName;

    private String url;

    private String user;

    private String password;
}
