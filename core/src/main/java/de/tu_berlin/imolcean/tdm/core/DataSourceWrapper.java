package de.tu_berlin.imolcean.tdm.core;

import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

@Getter
public class DataSourceWrapper implements DataSource
{
    @Getter(AccessLevel.NONE)
    private final DataSource ds;

    private final String driverClassName;

    private final String url;

    private final String username;

    private final String password;

    public DataSourceWrapper(String driverClassName, String url, String username, String password)
    {
        this.driverClassName = driverClassName;
        this.url = url;
        this.username = username;
        this.password = password;

        this.ds = DataSourceBuilder.create()
                .driverClassName(this.driverClassName)
                .url(this.url)
                .username(this.username)
                .password(this.password)
                .build();
    }

    @Override
    public Connection getConnection() throws SQLException
    {
        return ds.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException
    {
        return ds.getConnection(username, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException
    {
        return ds.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException
    {
        ds.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException
    {
        ds.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException
    {
        return ds.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException
    {
        return ds.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        return ds.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        return ds.isWrapperFor(iface);
    }
}
