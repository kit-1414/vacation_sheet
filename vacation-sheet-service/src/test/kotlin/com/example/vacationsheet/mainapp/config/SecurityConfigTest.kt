package com.example.vacationsheet.mainapp.config

import com.example.vacationsheet.mainapp.controller.AuthController
import com.example.vacationsheet.mainapp.controller.LoginController
import com.example.vacationsheet.mainapp.controller.ProjectController
import com.example.vacationsheet.mainapp.service.ProjectService
import com.example.vacationsheet.mainapp.service.UserService
import com.example.vacationsheet.mainapp.service.YandexOAuth2UserService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [AuthController::class, LoginController::class, ProjectController::class])
@Import(SecurityConfig::class)
class SecurityConfigTest {

	@Autowired
	lateinit var mockMvc: MockMvc

	@MockitoBean
	lateinit var yandexOAuth2UserService: YandexOAuth2UserService

	@MockitoBean
	lateinit var userService: UserService

	@MockitoBean
	lateinit var projectService: ProjectService

	@MockitoBean
	lateinit var clientRegistrationRepository: ClientRegistrationRepository

	@Test
	fun `unauthenticated API request returns 403`() {
		mockMvc.perform(get("/api/projects"))
			.andExpect(status().isForbidden)
	}

	@Test
	fun `unauthenticated auth me request returns 403`() {
		mockMvc.perform(get("/api/auth/me"))
			.andExpect(status().isForbidden)
	}

	@Test
	fun `login redirects to oauth2 authorization`() {
		mockMvc.perform(get("/login"))
			.andExpect(status().is3xxRedirection)
			.andExpect(redirectedUrl("/oauth2/authorization/yandex"))
	}

	@Test
	fun `csrf is available without authentication`() {
		mockMvc.perform(get("/api/auth/csrf"))
			.andExpect(status().isOk)
	}
}
