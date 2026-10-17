package com.pulsepass;

import com.pulsepass.security.SecurityTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
// PRD §9.1: sin la clave no arranca; en pruebas se inyecta la clave EXCLUSIVA del entorno de test.
@TestPropertySource(properties =
        "pulsepass.security.jwt.secret-base64=" + SecurityTestConfiguration.TEST_SECRET_BASE64)
class PulsepassApplicationTests {

	@Test
	void contextLoads() {
	}

}
