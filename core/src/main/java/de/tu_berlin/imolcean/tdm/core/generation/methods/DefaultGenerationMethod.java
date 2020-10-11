package de.tu_berlin.imolcean.tdm.core.generation.methods;

import schemacrawler.schema.Column;

import java.sql.JDBCType;
import java.sql.SQLType;
import java.util.HashMap;
import java.util.Map;

public class DefaultGenerationMethod implements GenerationMethod
{
    private static final Map<SQLType, GenerationMethod> defaultGenerationMethods;

    static
    {
        defaultGenerationMethods = new HashMap<>();

        GenerationMethod _string = new StringGenerationMethod();
        GenerationMethod _byte = new ByteGenerationMethod();
        GenerationMethod _short = new ShortGenerationMethod();
        GenerationMethod _int = new IntegerGenerationMethod();
        GenerationMethod _long = new LongGenerationMethod();
        GenerationMethod _float = new FloatGenerationMethod();
        GenerationMethod _double = new DoubleGenerationMethod();
        GenerationMethod _decimal = new BigDecimalGenerationMethod();
        GenerationMethod _boolean = new BooleanGenerationMethod();
        GenerationMethod _date = new DateGenerationMethod();
        GenerationMethod _time = new TimeGenerationMethod();
        GenerationMethod _timestamp = new TimestampGenerationMethod();

        defaultGenerationMethods.put(JDBCType.BIT, _boolean);
        defaultGenerationMethods.put(JDBCType.SMALLINT, _short);
        defaultGenerationMethods.put(JDBCType.TINYINT, _short);
        defaultGenerationMethods.put(JDBCType.INTEGER, _int);
        defaultGenerationMethods.put(JDBCType.BIGINT, _long);

        defaultGenerationMethods.put(JDBCType.REAL, _float);
        defaultGenerationMethods.put(JDBCType.DOUBLE, _double);
        defaultGenerationMethods.put(JDBCType.DECIMAL, _decimal);
        defaultGenerationMethods.put(JDBCType.NUMERIC, _decimal);

        defaultGenerationMethods.put(JDBCType.CHAR, _string);
        defaultGenerationMethods.put(JDBCType.NCHAR, _string);
        defaultGenerationMethods.put(JDBCType.VARCHAR, _string);
        defaultGenerationMethods.put(JDBCType.NVARCHAR, _string);
        defaultGenerationMethods.put(JDBCType.LONGVARCHAR, _string);

        defaultGenerationMethods.put(JDBCType.DATE, _date);
        defaultGenerationMethods.put(JDBCType.TIME, _time);
        defaultGenerationMethods.put(JDBCType.TIMESTAMP, _timestamp);

        // TODO
//        defaultGenerationMethods.put(JDBCType.BINARY, _bytes);
//        defaultGenerationMethods.put(JDBCType.VARBINARY, _bytes);
//        defaultGenerationMethods.put(JDBCType.LONGVARBINARY, _bytes);
    }

    Object generate(Column column)
    {
        GenerationMethod gm = defaultGenerationMethods.get(column.getType().getJavaSqlType());

        if(gm == null)
        {
            throw new IllegalArgumentException(
                    String.format(
                            "No GenerationMethod found for column %s of type %s",
                            column.getFullName(),
                            column.getType().getJavaSqlType().getName()));
        }

        if(gm instanceof StringGenerationMethod)
        {
            int max = column.getType().getMaximumScale(); // TODO Check that this method return string length

            return ((StringGenerationMethod) gm).generate(1, max, StringGenerationMethod.Capitalization.MIXED);
        }
        else
        {
            return ((PrimitiveGenerationMethod<?>) gm).generate();
        }
    }
}
