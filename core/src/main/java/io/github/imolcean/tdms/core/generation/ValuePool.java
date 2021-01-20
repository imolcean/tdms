package io.github.imolcean.tdms.core.generation;

import lombok.Getter;
import schemacrawler.schema.Column;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValuePool
{
    // (pkColumn -> (fkColumn -> Value))
    @Getter
    private static final Map<Column, Map<Column, List<Object>>> pool;

    static
    {
        pool = new HashMap<>();
    }
}
