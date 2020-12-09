package io.github.imolcean.tdms.core.generation;

import io.github.imolcean.tdms.api.dto.TableRuleDto;
import io.github.imolcean.tdms.api.exceptions.ColumnNotFoundException;
import io.github.imolcean.tdms.api.exceptions.GenerationMethodNotFoundException;
import io.github.imolcean.tdms.api.interfaces.generation.method.GenerationMethod;
import io.github.imolcean.tdms.api.services.LowLevelDataService;
import io.github.imolcean.tdms.api.services.SchemaService;
import io.github.imolcean.tdms.core.generation.methods.*;
import io.github.imolcean.tdms.core.generation.methods.*;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import schemacrawler.schema.Column;
import schemacrawler.schema.Table;

import java.sql.Connection;

@Service
@Log
public class GenerationMethodCreator
{
    private final SchemaService schemaService;
    private final LowLevelDataService lowLevelDataService;
    private final FormulaEngineCreator formulaEngineCreator;

    public GenerationMethodCreator(SchemaService schemaService,
                                   LowLevelDataService lowLevelDataService,
                                   FormulaEngineCreator formulaEngineCreator)
    {
        this.schemaService = schemaService;
        this.lowLevelDataService = lowLevelDataService;
        this.formulaEngineCreator = formulaEngineCreator;
    }

    public GenerationMethod create(TableRuleDto.ColumnRuleDto dto, Table table, Connection connection)
    {
        Column column = schemaService.findColumn(table, dto.getColumnName())
                .orElseThrow(() -> new ColumnNotFoundException(table, dto.getColumnName()));

        switch(dto.getGenerationMethodName())
        {
            case "BigDecimalGenerationMethod":
                return new BigDecimalGenerationMethod(column);
            case "BooleanGenerationMethod":
                return new BooleanGenerationMethod();
            case "ByteGenerationMethod":
                return new ByteGenerationMethod();
            case "DateGenerationMethod":
                return new DateGenerationMethod();
            case "DoubleGenerationMethod":
                return new DoubleGenerationMethod();
            case "FkGenerationMethod":
                return new FkGenerationMethod(lowLevelDataService, connection, column);
            case "FloatGenerationMethod":
                return new FloatGenerationMethod();
            case "FormulaGenerationMethod":
                return new FormulaGenerationMethod(formulaEngineCreator.createEngine(column), column);
            case "IntegerGenerationMethod":
                return new IntegerGenerationMethod();
            case "LongGenerationMethod":
                return new LongGenerationMethod();
            case "RegexGenerationMethod":
                return new RegexGenerationMethod();
            case "ShortGenerationMethod":
                return new ShortGenerationMethod();
            case "StringGenerationMethod":
                return new StringGenerationMethod(column);
            case "TimeGenerationMethod":
                return new TimeGenerationMethod();
            case "TimestampGenerationMethod":
                return new TimestampGenerationMethod();
            case "TinyIntGenerationMethod":
                return new TinyIntGenerationMethod();
            case "ValueListGenerationMethod":
                return new ValueListGenerationMethod();
            default:
                throw new GenerationMethodNotFoundException(dto.getGenerationMethodName());
        }
    }
}
