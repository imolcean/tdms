package de.tu_berlin.imolcean.tdm.api.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GitRepositoryDto
{
    private String url;
    private String dir;
    private String token;
}
