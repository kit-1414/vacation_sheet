package com.example.vacationsheet.mainapp.config

import com.example.vacationsheet.mainapp.service.YandexOAuth2UserService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
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
					"/login",
					"/login/start",
					"/swagger-ui.html",
					"/swagger-ui/**",
					"/v3/api-docs/**",
					"/actuator/health/**",
					"/error",
				).permitAll()
					.requestMatchers(HttpMethod.GET, "/api/**").authenticated()
					.requestMatchers(
						HttpMethod.PUT,
						"/api/projects/*/users/*",
						"/api/projects/*/managers/*",
					).hasAnyRole("MANAGER", "ADMIN")
					.requestMatchers(
						HttpMethod.DELETE,
						"/api/projects/*/users/*",
						"/api/projects/*/managers/*",
					).hasAnyRole("MANAGER", "ADMIN")
					.requestMatchers(HttpMethod.POST, "/api/users", "/api/projects").hasRole("ADMIN")
					.requestMatchers(HttpMethod.PUT, "/api/users/*", "/api/projects/*").hasRole("ADMIN")
					.requestMatchers(HttpMethod.DELETE, "/api/users/*", "/api/projects/*").hasRole("ADMIN")
					.requestMatchers("/api/**").authenticated()
					.anyRequest().permitAll()
			}
			.csrf { it.disable() }
			.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) }
			.oauth2Login {
				it.userInfoEndpoint { endpoint -> endpoint.userService(yandexOAuth2UserService) }
				it.loginPage("/login/start")
				it.defaultSuccessUrl("/", true)
			}
			.logout {
				it.logoutUrl("/api/auth/logout")
				it.logoutSuccessHandler { _, response, authentication ->
					response.status = if (authentication == null) {
						HttpServletResponse.SC_FORBIDDEN
					} else {
						HttpServletResponse.SC_NO_CONTENT
					}
				}
			}
			.exceptionHandling {
				it.defaultAuthenticationEntryPointFor(
					HttpStatusEntryPoint(HttpStatus.FORBIDDEN),
					RequestMatcher { request -> request.servletPath.startsWith("/api/") },
				)
			}

		return http.build()
	}
}
