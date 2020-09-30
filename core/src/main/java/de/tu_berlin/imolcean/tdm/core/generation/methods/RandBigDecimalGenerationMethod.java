package de.tu_berlin.imolcean.tdm.core.generation.methods;

import lombok.extern.java.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Log
public class RandBigDecimalGenerationMethod implements RandNumberGenerationMethod<BigDecimal>
{
    @Override
    public BigDecimal generate(Number min, Number max)
    {
        if(min == null)
        {
            min = Long.MIN_VALUE;
        }

        if(max == null)
        {
            max = Long.MAX_VALUE;
        }

        BigDecimal _min = new BigDecimal(min.toString());
        BigDecimal _max = new BigDecimal(max.toString());

        log.fine(String.format("Generating a BigDecimal between %s and %s", _min, _max));

        //noinspection UnpredictableBigDecimalConstructorCall
        return _min.add(new BigDecimal(Math.random()).multiply(_max.subtract(_min))).setScale(2, RoundingMode.HALF_UP);
    }
}
