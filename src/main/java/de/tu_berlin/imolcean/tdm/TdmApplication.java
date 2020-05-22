package de.tu_berlin.imolcean.tdm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.*;

@SpringBootApplication
public class TdmApplication implements CommandLineRunner
{
    @Value("${data.mssql.server}")
    private String serverName;

    @Value("${data.mssql.port}")
    private int portNumber;

    @Value("${data.mssql.db}")
    private String databaseName;

    @Value("${data.mssql.user}")
    private String username;

    @Value("${data.mssql.password}")
    private String password;

    public static void main(String[] args)
    {
        SpringApplication.run(TdmApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception
    {
        String url = String.format("jdbc:sqlserver://%s:%d;database=%s;user=%s;password=%s", serverName, portNumber, databaseName, username, password);

        System.out.println("Connection URL: " + url);

        try(Connection connection = DriverManager.getConnection(url))
        {
            System.out.println(String.format("Connection with %s:%d:%s established", this.serverName, this.portNumber, this.databaseName));

            DatabaseMetaData dbMetaData = connection.getMetaData();
            ResultSet rs;

            System.out.println("Type TABLE");
            System.out.println("------------------------------------");
            rs = dbMetaData.getTables(null, null, null, new String[] {"TABLE"});
            while(rs.next())
            {
                System.out.println(rs.getString("TABLE_NAME"));
            }
            System.out.println();

            System.out.println("Type SYSTEM TABLE");
            System.out.println("------------------------------------");
            rs = dbMetaData.getTables(null, null, null, new String[] {"SYSTEM TABLE"});
            while(rs.next())
            {
                System.out.println(rs.getString("TABLE_NAME"));
            }
            System.out.println();

            System.out.println("Type VIEW");
            System.out.println("------------------------------------");
            rs = dbMetaData.getTables(null, null, null, new String[] {"VIEW"});
            while(rs.next())
            {
                System.out.println(rs.getString("TABLE_NAME"));
            }
            System.out.println();
        }
    }
}
