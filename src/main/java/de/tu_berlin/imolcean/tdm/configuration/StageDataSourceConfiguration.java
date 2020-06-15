package de.tu_berlin.imolcean.tdm.configuration;

import de.tu_berlin.imolcean.tdm.StageDataSourceRouter;
import de.tu_berlin.imolcean.tdm.StageDataSourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Map;

@Configuration
public class StageDataSourceConfiguration
{
    @Autowired
    private StageDataSourceLoader loader;

    @Bean(name = "StageDataSource")
    public DataSource stageDataSource() throws IOException
    {
        Map<Object, Object> targetDataSources = loader.loadAll();

        StageDataSourceRouter router = new StageDataSourceRouter();

        router.setTargetDataSources(targetDataSources);
        router.setLenientFallback(false);

        return router;
    }
}
