package com.example.nsu_backend.utils;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostgresUtils {
    private final JdbcClient jdbcClient;

    public void clear() {
        List<String> tables = jdbcClient.sql("""
                SELECT tablename
                FROM pg_catalog.pg_tables
                WHERE schemaname = 'public'
                """).query(String.class).list();
        tables.forEach(table ->
                jdbcClient.sql("TRUNCATE TABLE " + table + " CASCADE").update()
        );
    }
}
