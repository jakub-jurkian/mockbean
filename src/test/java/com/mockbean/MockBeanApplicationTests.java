package com.mockbean;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

// Ingestion needs a running Ollama, so it is disabled here; this test checks wiring and migrations.
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "mockbean.ingestion.enabled=false")
class MockBeanApplicationTests {

	@Test
	void contextLoads() {
	}

}
