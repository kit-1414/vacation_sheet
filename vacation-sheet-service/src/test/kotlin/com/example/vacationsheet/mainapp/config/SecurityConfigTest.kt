package com.example.vacationsheet.mainapp.config

import com.example.vacationsheet.mainapp.controller.AuthController
import com.example.vacationsheet.mainapp.controller.LoginController
import com.example.vacationsheet.mainapp.controller.ProjectController
import com.example.vacationsheet.mainapp.controller.UserController
import com.example.vacationsheet.mainapp.controller.VacationRequestUserController
import com.example.vacationsheet.mainapp.dto.CurrentUserDto
import com.example.vacationsheet.mainapp.service.CurrentUserService
import com.example.vacationsheet.mainapp.service.ProjectService
import com.example.vacationsheet.mainapp.service.UserRole
import com.example.vacationsheet.mainapp.service.UserService
import com.example.vacationsheet.mainapp.service.VacationRequestService
import com.example.vacationsheet.mainapp.service.YandexOAuth2UserService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.mockito.BDDMockito.given

@WebMvcTest(
	controllers = [
		AuthController::class,
		LoginController::class,
		ProjectController::class,
		UserController::class,
		VacationRequestUserController::class,
	],
)
@Import(SecurityConfig::class)
class SecurityConfigTest {

	@Autowired
	lateinit var mockMvc: MockMvc

	@MockitoBean
	lateinit var yandexOAuth2UserService: YandexOAuth2UserService

	@MockitoBean
	lateinit var userService: UserService

	@MockitoBean
	lateinit var currentUserService: CurrentUserService

	@MockitoBean
	lateinit var projectService: ProjectService

	@MockitoBean
	lateinit var vacationRequestService: VacationRequestService

	@MockitoBean
	lateinit var clientRegistrationRepository: ClientRegistrationRepository

	@Test
	fun `unauthenticated API request returns 403`() {
		mockMvc.perform(get("/api/projects"))
			.andExpect(status().isForbidden)
	}

	@Test
	fun `unauthenticated users request returns 403`() {
		mockMvc.perform(get("/api/users"))
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
	fun `authenticated post does not require csrf token`() {
		mockMvc.perform(post("/api/auth/logout").with(oauth2Login()))
			.andExpect(status().isNoContent)
	}

	@Test
	fun `unauthenticated logout returns 403`() {
		mockMvc.perform(post("/api/auth/logout"))
			.andExpect(status().isForbidden)
	}

	@Test
	fun `user cannot create users`() {
		mockMvc.perform(
			post("/api/users")
				.with(oauth2Login().authorities(SimpleGrantedAuthority("ROLE_USER")))
				.contentType("application/json")
				.content("""{"email":"user@example.com"}"""),
		).andExpect(status().isForbidden)
	}

	@Test
	fun `manager cannot delete projects`() {
		mockMvc.perform(
			delete("/api/projects/1")
				.with(oauth2Login().authorities(SimpleGrantedAuthority("ROLE_MANAGER"))),
		).andExpect(status().isForbidden)
	}

	@Test
	fun `user cannot change project members`() {
		mockMvc.perform(
			put("/api/projects/1/users/2")
				.with(oauth2Login().authorities(SimpleGrantedAuthority("ROLE_USER"))),
		).andExpect(status().isForbidden)
	}

	@Test
	fun `admin can change project members`() {
		mockMvc.perform(
			put("/api/projects/1/users/2")
				.with(oauth2Login().authorities(SimpleGrantedAuthority("ROLE_ADMIN"))),
		).andExpect(status().isOk)
	}

	@Test
	fun `nobody can list own vacation requests`() {
		given(currentUserService.getCurrentUser()).willReturn(currentUser())
		given(vacationRequestService.getRequestsByOwnerId(1L)).willReturn(emptyList())

		mockMvc.perform(
			get("/api/user/actions/vacation_request")
				.with(
					oauth2Login()
						.attributes { it["default_email"] = "user@example.com" }
						.authorities(SimpleGrantedAuthority("ROLE_NOBODY")),
				),
		).andExpect(status().isOk)
	}

	@Test
	fun `nobody cannot create vacation request`() {
		mockMvc.perform(
			post("/api/user/actions/vacation_request")
				.with(oauth2Login().authorities(SimpleGrantedAuthority("ROLE_NOBODY")))
				.contentType("application/json")
				.content(validVacationRequestJson),
		).andExpect(status().isForbidden)
	}

	@Test
	fun `user can access vacation request create endpoint`() {
		given(currentUserService.getCurrentUser()).willReturn(currentUser())

		mockMvc.perform(
			post("/api/user/actions/vacation_request")
				.with(
					oauth2Login()
						.attributes { it["default_email"] = "user@example.com" }
						.authorities(SimpleGrantedAuthority("ROLE_USER")),
				)
				.contentType("application/json")
				.content(validVacationRequestJson),
		).andExpect(status().isCreated)
	}

	private fun currentUser() = CurrentUserDto(
		id = 1L,
		email = "user@example.com",
		firstName = "Test",
		lastName = "User",
		isAdmin = false,
		isActive = true,
		ctime = null,
		utime = null,
		roles = setOf(UserRole.USER),
	)

	private companion object {
		const val validVacationRequestJson =
			"""{"title":"Vacation","requestState":"DRAFT","vacationType":"PAYMENT_VACATION","startDate":"2026-09-01","endDate":"2026-09-14"}"""
	}
}
