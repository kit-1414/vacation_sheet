package com.example.vacationsheet.config

import com.example.vacationsheet.security.YandexOAuth2UserService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.util.matcher.RequestMatcher

@Configuration
class SecurityConfig {
	@Bean
	fun securityFilterChain(
		http: HttpSecurity,
		yandexOAuth2UserService: YandexOAuth2UserService,
	): SecurityFilterChain {
		http
			.authorizeHttpRequests {
				it.requestMatchers(
					"/swagger-ui.html",
					"/swagger-ui/**",
					"/v3/api-docs/**",
					"/actuator/health/**",
					"/api/auth/csrf",
					"/error",
				).permitAll()
					.requestMatchers("/api/**").authenticated()
					.anyRequest().permitAll()
			}
			.csrf {
				it.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
			}
			.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) }
			.oauth2Login {
				it.userInfoEndpoint { endpoint -> endpoint.userService(yandexOAuth2UserService) }
				it.defaultSuccessUrl("/", true)
			}
			.logout {
				it.logoutUrl("/api/auth/logout")
				it.logoutSuccessHandler { _, response, _ -> response.status = HttpServletResponse.SC_NO_CONTENT }
			}
			.exceptionHandling {
				it.defaultAuthenticationEntryPointFor(
					HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
					RequestMatcher { request -> request.servletPath.startsWith("/api/") },
				)
			}

		return http.build()
	}
}
