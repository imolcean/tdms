package de.tu_berlin.imolcean.tdm.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// TODO Encrypted password?

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceDto
{
    private String driverClassName;

    private String url;

    private String username;

    private String password;
}
