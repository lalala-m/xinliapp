package com.example.tongyangyuan.database;

public class DatabaseConfig {
    public static final String DB_HOST = "localhost";
    public static final int DB_PORT = 3306;
    public static final String DB_NAME = "xin_psychology";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "123456";

    public static String getJdbcUrl() {
        return "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }
}
