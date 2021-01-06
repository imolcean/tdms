package io.github.imolcean.tdms.core;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.imolcean.tdms.core.services.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TdmApplication implements CommandLineRunner
{
    @Autowired
    private ProjectService projectService;

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
        System.out.println("DONE!");
    }
}
