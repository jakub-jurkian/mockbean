package com.mockbean;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	// Flyway's V1 migration creates the vector extension, which plain postgres images lack.
	static final DockerImageName PGVECTOR_IMAGE =
			DockerImageName.parse("pgvector/pgvector:pg16").asCompatibleSubstituteFor("postgres");

	@Bean
	@ServiceConnection
	PostgreSQLContainer<?> postgresContainer() {
		return new PostgreSQLContainer<>(PGVECTOR_IMAGE);
	}

	// The pgvector store has its own connection settings, separate from the datasource.
	@Bean
	DynamicPropertyRegistrar pgVectorStoreProperties(PostgreSQLContainer<?> postgres) {
		return registry -> {
			registry.add("langchain4j.pgvector.host", postgres::getHost);
			registry.add("langchain4j.pgvector.port", () -> postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT));
			registry.add("langchain4j.pgvector.database", postgres::getDatabaseName);
			registry.add("langchain4j.pgvector.user", postgres::getUsername);
			registry.add("langchain4j.pgvector.password", postgres::getPassword);
		};
	}
}
