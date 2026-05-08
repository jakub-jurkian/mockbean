package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "langchain4j.pgvector")
public record PgVectorProperties(
        String host,
        int port,
        String database,
        String user,
        String password,
        String table,
        int dimension
) {}
