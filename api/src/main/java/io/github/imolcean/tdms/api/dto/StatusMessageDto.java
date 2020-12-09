package io.github.imolcean.tdms.api.dto;

import io.github.imolcean.tdms.api.annotations.TsOptional;
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

    @Getter(onMethod_ = {@TsOptional})
    private String addition;
}
