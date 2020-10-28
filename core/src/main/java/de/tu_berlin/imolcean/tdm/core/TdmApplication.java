package de.tu_berlin.imolcean.tdm.core;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.imolcean.tdm.api.DataSourceWrapper;
import de.tu_berlin.imolcean.tdm.api.TableContent;
import de.tu_berlin.imolcean.tdm.api.ValueLibrary;
import de.tu_berlin.imolcean.tdm.api.dto.ProjectDto;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.api.services.DataService;
import de.tu_berlin.imolcean.tdm.core.generation.*;
import de.tu_berlin.imolcean.tdm.core.generation.methods.*;
import de.tu_berlin.imolcean.tdm.core.services.DataSourceService;
import de.tu_berlin.imolcean.tdm.core.services.ProjectService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import schemacrawler.schema.Column;
import schemacrawler.schema.Table;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.*;

@SpringBootApplication
public class TdmApplication implements CommandLineRunner
{
    @Autowired
    private ProjectService projectService;

    @Autowired
    private SchemaService schemaService;

    @Autowired
    private DataService dataService;

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private ValueLibraryService valueLibraryService;

    @Autowired
    private FormulaFunctionService formulaFunctionService;

    @Autowired
    private DefaultDataGenerator defaultDataGenerator;

    @Autowired
    private FormulaService formulaService;

    private final ObjectMapper mapper = new ObjectMapper()
            .setDefaultPrettyPrinter(
                    new DefaultPrettyPrinter().withArrayIndenter(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE));

    public static void main(String[] args)
    {
        SpringApplication.run(TdmApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception
    {
        ProjectDto project = mapper.readValue(new ClassPathResource("EXP.tdm.json").getInputStream(), ProjectDto.class);
        projectService.open(project);

        StageContextHolder.setStageName("exp");


//        for(String tableName : schemaService.getTableNames(dataSourceService.getInternalDataSource()))
//        {
//            System.out.println(tableName + ":");
//
//            for(Column c : schemaService.getTable(dataSourceService.getInternalDataSource(), tableName).getColumns())
//            {
//                System.out.printf("%s: %s: %s%n", c.getName(), c.getColumnDataType().getJavaSqlType().getName(), c.getColumnDataType().getTypeMappedClass());
//            }
//        }


//        System.setProperty("polyglot.js.nashorn-compat", "true");
//        ScriptEngine js = new ScriptEngineManager().getEngineByName("graal.js");
//
//        if(js == null)
//        {
//            System.err.println("JS failed :(");
//            System.exit(10);
//        }
//
//        js.put("Rand", new DateGenerationMethod());
//        js.eval("print(Rand.generate(null, null));");
//        js.eval("print(Rand.generate(null, java.sql.Date.valueOf('2021-09-01')));");
//        js.eval("print(Rand.generate(java.sql.Date.valueOf('2013-09-01'), null));");
//        js.eval("print(Rand.generate(java.sql.Date.valueOf('2013-09-01'), java.sql.Date.valueOf('2021-09-01')));");
//        js.eval("print(Rand.generate(java.sql.Date.valueOf('1834-09-01'), java.sql.Date.valueOf('3457-09-01')));");


//        Column column = schemaService.getTable(dataSourceService.getInternalDataSource(), "person").getColumns().get(1);
//        System.out.println(column.getFullName());
//
//        GenerationMethod rand = GenerationMethods.createByColumn(column);
//        System.out.println(rand.getClass());
//
//        Map<String, Object> params = new HashMap<>();
//
//        params.put("minLength", 3);
//        params.put("maxLength", 6);
//        params.put("capitalization", "mixed");
//        System.out.println(rand.generate(params));
//
//        params.clear();
//        params.put("minLength", 3);
//        params.put("maxLength", 6);
//        System.out.println(rand.generate(params));
//
//        params.clear();
//        params.put("minLength", 3);
//        params.put("capitalization", "lower");
//        System.out.println(rand.generate(params));
//
//        params.clear();
//        params.put("maxLength", 6);
//        params.put("capitalization", "upper");
//        System.out.println(rand.generate(params));
//
//        params.clear();
//        params.put("capitalization", "first_upper");
//        System.out.println(rand.generate(params));


        Map<Table, TableContent> generated = new HashMap<>();
//        defaultDataGenerator.generate(createTableRulesAppendAll(generated), generated);
        defaultDataGenerator.generate(createTableRulesUpdate(generated), generated);


//        ValueLibrary lib = valueLibraryService.getLists().get("$LibLastNamesDE");
//        System.out.println(lib.get("_list").getClass());
//        System.out.println(lib.getList().getClass());


        System.out.println("DONE!");
    }

    @SneakyThrows
    private Map<Table, TableRule> createTableRulesUpdate(Map<Table, TableContent> generated)
    {
        DataSourceWrapper ds = dataSourceService.getInternalDataSource();
        Map<Table, TableRule> map = new HashMap<>();

        Map<String, Object> params0 = new HashMap<>();
        params0.put("options", new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});

        Map<String, Object> paramsAd = new HashMap<>();
        paramsAd.put("formula", "RandBoolean.generate();");

        Map<String, Object> paramsCa = new HashMap<>();
        paramsCa.put("formula", "RandFrom.pick($LibLastNamesDE);");

        Map<String, Object> paramsCb = new HashMap<>();
        paramsCb.put("formula", "$a.split(\"\").reverse().join(\"\")");

        Map<String, Object> paramsCc = new HashMap<>();
        paramsCc.put("formula", "$a + \" \" + $b");

        Table A = schemaService.getTable(ds, "A");
        TableRule trA = new TableRule(A, TableRule.FillMode.UPDATE, 100);
        trA.setColumnRule(new ColumnRule(trA, A.getColumns().get(0), new IntegerGenerationMethod()));
        trA.setColumnRule(new ColumnRule(trA, A.getColumns().get(1), new ValueListGenerationMethod(), params0));
        trA.setColumnRule(new ColumnRule(trA, A.getColumns().get(4), new FormulaGenerationMethod(formulaService.createEngine(A.getColumns().get(4)), A.getColumns().get(4)), paramsAd));

        Table C = schemaService.getTable(ds, "C");
        TableRule trC = new TableRule(C, TableRule.FillMode.UPDATE, 100);
        trC.setColumnRule(new ColumnRule(trC, C.getColumns().get(1), new FormulaGenerationMethod(formulaService.createEngine(C.getColumns().get(1)), C.getColumns().get(1)), paramsCa));
        trC.setColumnRule(new ColumnRule(trC, C.getColumns().get(2), new FormulaGenerationMethod(formulaService.createEngine(C.getColumns().get(2)), C.getColumns().get(2)), paramsCb));
        trC.setColumnRule(new ColumnRule(trC, C.getColumns().get(3), new FormulaGenerationMethod(formulaService.createEngine(C.getColumns().get(3)), C.getColumns().get(3)), paramsCc));

        Table E = schemaService.getTable(ds, "E");
        TableRule trE = new TableRule(E, TableRule.FillMode.UPDATE, 100);
        trE.setColumnRule(new ColumnRule(trE, E.getColumns().get(1), new FkGenerationMethod(ds, generated, E.getColumns().get(1))));

//        map.put(A, trA);
        map.put(C, trC);
//        map.put(E, trE);

        return map;
    }

    @SneakyThrows
    private Map<Table, TableRule> createTableRulesAppendAll(Map<Table, TableContent> generated)
    {
        DataSourceWrapper ds = dataSourceService.getInternalDataSource();
        Map<Table, TableRule> map = new HashMap<>();

        Table A = schemaService.getTable(ds, "A");
        TableRule trA = new TableRule(A, TableRule.FillMode.APPEND, 100);
        trA.setColumnRule(new ColumnRule(trA, A.getColumns().get(0), new IntegerGenerationMethod(), true, 0));
        trA.setColumnRule(new ColumnRule(trA, A.getColumns().get(1), new LongGenerationMethod(), true, 0.5));
        trA.setColumnRule(new ColumnRule(trA, A.getColumns().get(2), new ShortGenerationMethod(), false, 1));
        trA.setColumnRule(new ColumnRule(trA, A.getColumns().get(3), new TinyIntGenerationMethod(), false, 0.5)); // Tinyint: [0, 255]
        trA.setColumnRule(new ColumnRule(trA, A.getColumns().get(4), new BooleanGenerationMethod()));
        trA.setColumnRule(new ColumnRule(trA, A.getColumns().get(5), new FkGenerationMethod(ds, generated, A.getColumns().get(5))));
        trA.setColumnRule(new ColumnRule(trA, A.getColumns().get(6), new FkGenerationMethod(ds, generated, A.getColumns().get(6))));

        Table B = schemaService.getTable(ds, "B");
        TableRule trB = new TableRule(B, TableRule.FillMode.APPEND, 10);
        trB.setColumnRule(new ColumnRule(trB, B.getColumns().get(0), new IntegerGenerationMethod(), true, 0));
        trB.setColumnRule(new ColumnRule(trB, B.getColumns().get(1), new FloatGenerationMethod()));
        trB.setColumnRule(new ColumnRule(trB, B.getColumns().get(2), new DoubleGenerationMethod()));
        trB.setColumnRule(new ColumnRule(trB, B.getColumns().get(3), new BigDecimalGenerationMethod(B.getColumns().get(3))));
        trB.setColumnRule(new ColumnRule(trB, B.getColumns().get(4), new BigDecimalGenerationMethod(B.getColumns().get(4))));
        trB.setColumnRule(new ColumnRule(trB, B.getColumns().get(5), new FkGenerationMethod(ds, generated, B.getColumns().get(5))));

        Table C = schemaService.getTable(ds, "C");
        TableRule trC = new TableRule(C, TableRule.FillMode.APPEND, 10);
        trC.setColumnRule(new ColumnRule(trC, C.getColumns().get(0), new IntegerGenerationMethod(), true, 0));
        trC.setColumnRule(new ColumnRule(trC, C.getColumns().get(1), new StringGenerationMethod(C.getColumns().get(1))));
        trC.setColumnRule(new ColumnRule(trC, C.getColumns().get(2), new StringGenerationMethod(C.getColumns().get(2))));
        trC.setColumnRule(new ColumnRule(trC, C.getColumns().get(3), new StringGenerationMethod(C.getColumns().get(3))));
        trC.setColumnRule(new ColumnRule(trC, C.getColumns().get(4), new StringGenerationMethod(C.getColumns().get(4))));
        trC.setColumnRule(new ColumnRule(trC, C.getColumns().get(5), new FkGenerationMethod(ds, generated, C.getColumns().get(5))));
        trC.setColumnRule(new ColumnRule(trC, C.getColumns().get(6), new FkGenerationMethod(ds, generated, C.getColumns().get(6))));
        trC.setColumnRule(new ColumnRule(trC, C.getColumns().get(7), new FkGenerationMethod(ds, generated, C.getColumns().get(7))));

        Table D = schemaService.getTable(ds, "D");
        TableRule trD = new TableRule(D, TableRule.FillMode.APPEND, 10);
        trD.setColumnRule(new ColumnRule(trD, D.getColumns().get(0), new IntegerGenerationMethod(), true, 0));
        trD.setColumnRule(new ColumnRule(trD, D.getColumns().get(1), new DateGenerationMethod()));
        trD.setColumnRule(new ColumnRule(trD, D.getColumns().get(2), new TimeGenerationMethod()));
        trD.setColumnRule(new ColumnRule(trD, D.getColumns().get(3), new TimestampGenerationMethod())); // Datetime starts from 1753, Datetime2 - from 0001
        trD.setColumnRule(new ColumnRule(trD, D.getColumns().get(4), new FkGenerationMethod(ds, generated, D.getColumns().get(4))));

        Table E = schemaService.getTable(ds, "E");
        TableRule trE = new TableRule(E, TableRule.FillMode.APPEND, 10);
        trE.setColumnRule(new ColumnRule(trE, E.getColumns().get(0), new IntegerGenerationMethod(), true, 0));
        trE.setColumnRule(new ColumnRule(trE, E.getColumns().get(1), new FkGenerationMethod(ds, generated, E.getColumns().get(1))));

        map.put(A, trA);
        map.put(B, trB);
        map.put(C, trC);
        map.put(D, trD);
        map.put(E, trE);

        return map;
    }
}
