package de.tu_berlin.imolcean.tdm.core;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.imolcean.tdm.api.TableContent;
import de.tu_berlin.imolcean.tdm.api.dto.ProjectDto;
import de.tu_berlin.imolcean.tdm.api.dto.TableRuleDto;
import de.tu_berlin.imolcean.tdm.api.services.LowLevelDataService;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.api.services.DataService;
import de.tu_berlin.imolcean.tdm.core.generation.*;
import de.tu_berlin.imolcean.tdm.core.services.DataSourceService;
import de.tu_berlin.imolcean.tdm.core.services.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import schemacrawler.schema.Column;
import schemacrawler.schema.Table;

import java.sql.Connection;
import java.sql.Statement;
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
    private LowLevelDataService lowLevelDataService;

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private ValueLibraryLoader valueLibraryLoader;

    @Autowired
    private ScriptLoader scriptLoader;

    @Autowired
    private RuleBasedDataGenerator ruleBasedDataGenerator;

    @Autowired
    private FormulaEngineCreator formulaEngineCreator;

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


//        ruleBasedDataGenerator.generate(dataSourceService.getInternalDataSource(), createTableRulesAppendAll());
//        ruleBasedDataGenerator.generate(dataSourceService.getInternalDataSource(), createTableRulesUpdate());


//        ValueLibrary lib = valueLibraryService.getLists().get("$LibLastNamesDE");
//        System.out.println(lib.get("_list").getClass());
//        System.out.println(lib.getList().getClass());


        System.out.println("DONE!");
    }

    private List<TableRuleDto> createTableRulesUpdate()
    {
        Map<String, Object> paramsAa = new HashMap<>();
        paramsAa.put("options", new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});

        Map<String, Object> paramsAd = new HashMap<>();
        paramsAd.put("formula", "var val = RandBoolean.generate(); test(); test_param(val); val;");

        Map<String, Object> paramsCa = new HashMap<>();
        paramsCa.put("formula", "RandFrom.pick($LibLastNamesDE);");

        Map<String, Object> paramsCb = new HashMap<>();
        paramsCb.put("formula", "$a.split(\"\").reverse().join(\"\")");

        Map<String, Object> paramsCc = new HashMap<>();
        paramsCc.put("formula", "$a + \" \" + str_reverse($b)");

        List<TableRuleDto> trs = new ArrayList<>();

        List<TableRuleDto.ColumnRuleDto> crsA = new ArrayList<>();
        crsA.add(new TableRuleDto.ColumnRuleDto("id", "IntegerGenerationMethod", true, 0, null));
        crsA.add(new TableRuleDto.ColumnRuleDto("a", "ValueListGenerationMethod", false, 0, paramsAa));
        crsA.add(new TableRuleDto.ColumnRuleDto("d", "FormulaGenerationMethod", false, 0, paramsAd));
        trs.add(new TableRuleDto("A", TableRuleDto.FillMode.UPDATE, 0, crsA));

        List<TableRuleDto.ColumnRuleDto> crsC = new ArrayList<>();
        crsC.add(new TableRuleDto.ColumnRuleDto("a", "FormulaGenerationMethod", false, 0, paramsCa));
        crsC.add(new TableRuleDto.ColumnRuleDto("b", "FormulaGenerationMethod", false, 0, paramsCb));
        crsC.add(new TableRuleDto.ColumnRuleDto("c", "FormulaGenerationMethod", false, 0, paramsCc));
        trs.add(new TableRuleDto("C", TableRuleDto.FillMode.UPDATE, 0, crsC));

        List<TableRuleDto.ColumnRuleDto> crsE = new ArrayList<>();
        crsE.add(new TableRuleDto.ColumnRuleDto("A_id", "FkGenerationMethod", false, 0, null));
//        trs.add(new TableRuleDto("E", TableRuleDto.FillMode.UPDATE, 0, crsE));

        return trs;
    }

    private List<TableRuleDto> createTableRulesAppendAll()
    {
        List<TableRuleDto> trs = new ArrayList<>();

        List<TableRuleDto.ColumnRuleDto> crsA = new ArrayList<>();
        crsA.add(new TableRuleDto.ColumnRuleDto("id", "IntegerGenerationMethod", true, 0, null));
        crsA.add(new TableRuleDto.ColumnRuleDto("a", "LongGenerationMethod", true, 0.5, null));
        crsA.add(new TableRuleDto.ColumnRuleDto("b", "ShortGenerationMethod", false, 1, null));
        crsA.add(new TableRuleDto.ColumnRuleDto("c", "TinyIntGenerationMethod", false, 0.5, null));
        crsA.add(new TableRuleDto.ColumnRuleDto("d", "BooleanGenerationMethod", false, 0, null));
        crsA.add(new TableRuleDto.ColumnRuleDto("B_id", "FkGenerationMethod", false, 0, null));
        crsA.add(new TableRuleDto.ColumnRuleDto("D_id", "FkGenerationMethod", false, 0, null));
        trs.add(new TableRuleDto("A", TableRuleDto.FillMode.APPEND, 10, crsA));

        List<TableRuleDto.ColumnRuleDto> crsB = new ArrayList<>();
        crsB.add(new TableRuleDto.ColumnRuleDto("id", "IntegerGenerationMethod", true, 0, null));
        crsB.add(new TableRuleDto.ColumnRuleDto("a", "FloatGenerationMethod", false, 0, null));
        crsB.add(new TableRuleDto.ColumnRuleDto("b", "DoubleGenerationMethod", false, 0, null));
        crsB.add(new TableRuleDto.ColumnRuleDto("c", "BigDecimalGenerationMethod", false, 0, null));
        crsB.add(new TableRuleDto.ColumnRuleDto("d", "BigDecimalGenerationMethod", false, 0, null));
        crsB.add(new TableRuleDto.ColumnRuleDto("C_id", "FkGenerationMethod", false, 0, null));
        trs.add(new TableRuleDto("B", TableRuleDto.FillMode.APPEND, 10, crsB));

        List<TableRuleDto.ColumnRuleDto> crsC = new ArrayList<>();
        crsC.add(new TableRuleDto.ColumnRuleDto("id", "IntegerGenerationMethod", true, 0, null));
        crsC.add(new TableRuleDto.ColumnRuleDto("a", "StringGenerationMethod", false, 0, null));
        crsC.add(new TableRuleDto.ColumnRuleDto("b", "StringGenerationMethod", false, 0, null));
        crsC.add(new TableRuleDto.ColumnRuleDto("c", "StringGenerationMethod", false, 0, null));
        crsC.add(new TableRuleDto.ColumnRuleDto("d", "StringGenerationMethod", false, 0, null));
        crsC.add(new TableRuleDto.ColumnRuleDto("A_id", "FkGenerationMethod", false, 0, null));
        crsC.add(new TableRuleDto.ColumnRuleDto("B_id", "FkGenerationMethod", false, 0, null));
        crsC.add(new TableRuleDto.ColumnRuleDto("C_id", "FkGenerationMethod", false, 0, null));
        trs.add(new TableRuleDto("C", TableRuleDto.FillMode.APPEND, 10, crsC));

        List<TableRuleDto.ColumnRuleDto> crsD = new ArrayList<>();
        crsD.add(new TableRuleDto.ColumnRuleDto("id", "IntegerGenerationMethod", true, 0, null));
        crsD.add(new TableRuleDto.ColumnRuleDto("a", "DateGenerationMethod", false, 0, null));
        crsD.add(new TableRuleDto.ColumnRuleDto("b", "TimeGenerationMethod", false, 0, null));
        crsD.add(new TableRuleDto.ColumnRuleDto("c", "TimestampGenerationMethod", false, 0, null));
        crsD.add(new TableRuleDto.ColumnRuleDto("E_id", "FkGenerationMethod", false, 0, null));
        trs.add(new TableRuleDto("D", TableRuleDto.FillMode.APPEND, 10, crsD));

        List<TableRuleDto.ColumnRuleDto> crsE = new ArrayList<>();
        crsE.add(new TableRuleDto.ColumnRuleDto("id", "IntegerGenerationMethod", true, 0, null));
        crsE.add(new TableRuleDto.ColumnRuleDto("A_id", "FkGenerationMethod", false, 0, null));
        trs.add(new TableRuleDto("E", TableRuleDto.FillMode.APPEND, 10, crsE));

        return trs;
    }
}
