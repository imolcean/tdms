package de.tu_berlin.imolcean.tdm.core.generation;

import de.tu_berlin.imolcean.tdm.api.exceptions.DataGenerationException;
import de.tu_berlin.imolcean.tdm.api.interfaces.generation.method.ColumnAwareGenerationMethod;
import de.tu_berlin.imolcean.tdm.api.interfaces.generation.method.PrimitiveGenerationMethod;
import de.tu_berlin.imolcean.tdm.core.generation.methods.*;
import schemacrawler.schema.Column;

import java.sql.JDBCType;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public class GenerationMethods
{
    private static final Map<String, Class<? extends PrimitiveGenerationMethod<?>>> defaultGenerationMethods;

    static
    {
        defaultGenerationMethods = new HashMap<>();

        Class<? extends PrimitiveGenerationMethod<?>> _string = StringGenerationMethod.class;
        Class<? extends PrimitiveGenerationMethod<?>> _byte = ByteGenerationMethod.class;
        Class<? extends PrimitiveGenerationMethod<?>> _tiny = TinyIntGenerationMethod.class;
        Class<? extends PrimitiveGenerationMethod<?>> _short = ShortGenerationMethod.class;
        Class<? extends PrimitiveGenerationMethod<?>> _int = IntegerGenerationMethod.class;
        Class<? extends PrimitiveGenerationMethod<?>> _long = LongGenerationMethod.class;
        Class<? extends PrimitiveGenerationMethod<?>> _float = FloatGenerationMethod.class;
        Class<? extends PrimitiveGenerationMethod<?>> _double = DoubleGenerationMethod.class;
        Class<? extends PrimitiveGenerationMethod<?>> _decimal = BigDecimalGenerationMethod.class;
        Class<? extends PrimitiveGenerationMethod<?>> _boolean = BooleanGenerationMethod.class;
        Class<? extends PrimitiveGenerationMethod<?>> _date = DateGenerationMethod.class;
        Class<? extends PrimitiveGenerationMethod<?>> _time = TimeGenerationMethod.class;
        Class<? extends PrimitiveGenerationMethod<?>> _timestamp = TimestampGenerationMethod.class;

        defaultGenerationMethods.put(JDBCType.BIT.getName(), _boolean);
        defaultGenerationMethods.put(JDBCType.SMALLINT.getName(), _short);
        defaultGenerationMethods.put(JDBCType.TINYINT.getName(), _tiny);
        defaultGenerationMethods.put(JDBCType.INTEGER.getName(), _int);
        defaultGenerationMethods.put(JDBCType.BIGINT.getName(), _long);

        defaultGenerationMethods.put(JDBCType.REAL.getName(), _float);
        defaultGenerationMethods.put(JDBCType.DOUBLE.getName(), _double);
        defaultGenerationMethods.put(JDBCType.DECIMAL.getName(), _decimal);
        defaultGenerationMethods.put(JDBCType.NUMERIC.getName(), _decimal);

        defaultGenerationMethods.put(JDBCType.CHAR.getName(), _string);
        defaultGenerationMethods.put(JDBCType.NCHAR.getName(), _string);
        defaultGenerationMethods.put(JDBCType.VARCHAR.getName(), _string);
        defaultGenerationMethods.put(JDBCType.NVARCHAR.getName(), _string);
//        defaultGenerationMethods.put(JDBCType.LONGVARCHAR.getName(), _string); // LONGVARCHAR Seems to be deprecated

        defaultGenerationMethods.put(JDBCType.DATE.getName(), _date);
        defaultGenerationMethods.put(JDBCType.TIME.getName(), _time);
        defaultGenerationMethods.put(JDBCType.TIMESTAMP.getName(), _timestamp);

        // TODO
//        defaultGenerationMethods.put(JDBCType.BINARY.getName(), _bytes);
//        defaultGenerationMethods.put(JDBCType.VARBINARY.getName(), _bytes);
//        defaultGenerationMethods.put(JDBCType.LONGVARBINARY.getName(), _bytes);
    }

    public static PrimitiveGenerationMethod<?> createByColumn(Column column)
    {
        Class<? extends PrimitiveGenerationMethod<?>> clazz = defaultGenerationMethods.get(column.getColumnDataType().getJavaSqlType().getName());

        if(clazz == null)
        {
            throw new IllegalArgumentException(
                    String.format(
                            "Cannot find a default generation method for column %s of type %s",
                            column.getFullName(),
                            column.getColumnDataType().getJavaSqlType().getName()));
        }

        try
        {
            if(ColumnAwareGenerationMethod.class.isAssignableFrom(clazz))
            {
                return clazz.getConstructor(Column.class).newInstance(column);
            }
            else
            {
                return clazz.getConstructor().newInstance();
            }
        }
        catch(Exception e)
        {
            throw new DataGenerationException(e);
        }

        // TODO Return FkGenerationMethod for FK Columns?
    }
}
