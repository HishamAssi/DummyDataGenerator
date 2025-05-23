package com.hisham.dummydatagenerator.datasource;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

public class DatasourceProvider {

    public static DataSource createDataSource(String jdbcUrl, String username, String password) {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setUrl(jdbcUrl);
        ds.setUsername(username);
        ds.setPassword(password);
        return ds;
    }
}

