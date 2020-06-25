package de.tu_berlin.imolcean.tdm.core.dto;

import de.tu_berlin.imolcean.tdm.core.annotation.TsOptional;
import lombok.Data;
import lombok.Getter;

@Data
public class StatusMessageDto
{
    public enum Kind
    {
        INFO,
        SUCCESS,
        WARNING,
        ERROR
    }

    private Kind kind;

    private String content;

    @Getter(onMethod_ = { @TsOptional})
    private String addition;
}
