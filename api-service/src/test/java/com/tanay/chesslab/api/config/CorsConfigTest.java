package com.tanay.chesslab.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

class CorsConfigTest {

	@Test
	void allowsPreflightFromLocalKubernetesFrontendPort() {
		InspectableCorsRegistry registry = new InspectableCorsRegistry();

		new CorsConfig().addCorsMappings(registry);

		CorsConfiguration configuration = registry.configurations().get("/api/**");
		assertThat(configuration).isNotNull();
		assertThat(configuration.checkOrigin("http://127.0.0.1:15175"))
				.isEqualTo("http://127.0.0.1:15175");
		assertThat(configuration.checkHttpMethod(org.springframework.http.HttpMethod.POST)).isNotNull();
		assertThat(configuration.getAllowCredentials()).isTrue();
	}

	private static final class InspectableCorsRegistry extends CorsRegistry {

		private Map<String, CorsConfiguration> configurations() {
			return getCorsConfigurations();
		}
	}
}
