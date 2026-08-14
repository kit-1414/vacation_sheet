package com.example.vacationsheet.mainapp.service

import com.example.vacationsheet.mainapp.hql.dto.UserAccountDto
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.web.server.ResponseStatusException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CurrentUserServiceTest {
	private val userService = mockk<UserService>()
	private val service = CurrentUserService(userService)

	@AfterEach
	fun clearSecurityContext() {
		SecurityContextHolder.clearContext()
	}

	@Test
	fun `returns local user and roles from authenticated principal`() {
		val authorities = listOf(
			SimpleGrantedAuthority(ROLE_USER),
			SimpleGrantedAuthority(ROLE_ADMIN),
		)
		val principal = DefaultOAuth2User(
			authorities,
			mapOf("default_email" to "User@Example.com"),
			"default_email",
		)
		SecurityContextHolder.getContext().authentication = OAuth2AuthenticationToken(principal, authorities, "yandex")
		every { userService.findCurrent("User@Example.com") } returns user()

		val result = service.getCurrentUser()

		assertEquals(1L, result.id)
		assertEquals(setOf(UserRole.USER, UserRole.ADMIN), result.roles)
		verify(exactly = 1) { userService.findCurrent("User@Example.com") }
	}

	@Test
	fun `rejects missing authentication`() {
		SecurityContextHolder.clearContext()

		val exception = assertFailsWith<ResponseStatusException> { service.getCurrentUser() }

		assertEquals(403, exception.statusCode.value())
	}

	@Test
	fun `rejects principal without email`() {
		val authorities = listOf(SimpleGrantedAuthority(ROLE_USER))
		val principal = DefaultOAuth2User(authorities, mapOf("login" to "user"), "login")
		SecurityContextHolder.getContext().authentication = OAuth2AuthenticationToken(principal, authorities, "yandex")

		val exception = assertFailsWith<ResponseStatusException> { service.getCurrentUser() }

		assertEquals(403, exception.statusCode.value())
		verify(exactly = 0) { userService.findCurrent(any()) }
	}

	private fun user() = UserAccountDto(
		id = 1L,
		email = "user@example.com",
		firstName = "Test",
		lastName = "User",
		isAdmin = true,
		isActive = true,
		ctime = null,
		utime = null,
	)
}
