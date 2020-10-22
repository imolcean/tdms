package de.tu_berlin.imolcean.tdm.core.generation.methods;

import lombok.extern.java.Log;
import schemacrawler.schema.Column;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Log
public class BigDecimalGenerationMethod implements NumberGenerationMethod<BigDecimal>, ColumnAwareGenerationMethod
{
    private final Column column;

    public BigDecimalGenerationMethod(Column column)
    {
        this.column = column;
    }

    @Override
    public BigDecimal generate(Number min, Number max)
    {
        int precision = column.getSize();
        int scale = column.getDecimalDigits();
        long limit = (long) (Math.pow(10, precision - scale) - 1);

        if(min == null || min.longValue() < -limit)
        {
            min = -limit;
        }

        if(max == null || max.longValue() > limit)
        {
            max = limit;
        }

        BigDecimal _min = new BigDecimal(min.toString());
        BigDecimal _max = new BigDecimal(max.toString());

        log.fine(String.format("Generating a BigDecimal between %s and %s (precision = %s, scale = %s)", _min, _max, precision, scale));

        //noinspection UnpredictableBigDecimalConstructorCall
        return _min.add(new BigDecimal(Math.random()).multiply(_max.subtract(_min))).setScale(scale, RoundingMode.HALF_UP);
    }
}
