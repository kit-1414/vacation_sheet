package com.example.vacationsheet.mainapp.service

import com.example.vacationsheet.mainapp.hql.model.UserAccountEntity
import com.example.vacationsheet.mainapp.hql.repository.UserAccountRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import io.mockk.unmockkConstructor
import io.mockk.verify
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class YandexOAuth2UserServiceTest {
	private val userAccountRepository = mockk<UserAccountRepository>()
	private val emailDomainPolicy = mockk<EmailDomainPolicy>()
	private val userRequest = mockk<OAuth2UserRequest>()

	@Test
	fun `first login saves normalized email and names`() = withOAuthUser(
		mapOf(
			"id" to "yandex-id",
			"default_email" to "  User@Example.COM ",
			"first_name" to " Test ",
			"last_name" to " User ",
		),
	) {
		val savedAccount = slot<UserAccountEntity>()
		every { emailDomainPolicy.isAllowed("user@example.com") } returns true
		every { userAccountRepository.findByEmail("user@example.com") } returns null
		every { userAccountRepository.count() } returns 0L
		every { userAccountRepository.save(capture(savedAccount)) } answers { savedAccount.captured }

		YandexOAuth2UserService(userAccountRepository, emailDomainPolicy).loadUser(userRequest)

		assertEquals("user@example.com", savedAccount.captured.email)
		assertEquals("Test", savedAccount.captured.firstName)
		assertEquals("User", savedAccount.captured.lastName)
		assertTrue(savedAccount.captured.isAdmin)
		assertTrue(savedAccount.captured.isActive)
	}

	@Test
	fun `later login creates active non-admin user`() = withOAuthUser(
		mapOf(
			"id" to "yandex-id",
			"default_email" to "user@example.com",
		),
	) {
		val savedAccount = slot<UserAccountEntity>()
		every { emailDomainPolicy.isAllowed("user@example.com") } returns true
		every { userAccountRepository.findByEmail("user@example.com") } returns null
		every { userAccountRepository.count() } returns 1L
		every { userAccountRepository.save(capture(savedAccount)) } answers { savedAccount.captured }

		YandexOAuth2UserService(userAccountRepository, emailDomainPolicy).loadUser(userRequest)

		assertFalse(savedAccount.captured.isAdmin)
		assertTrue(savedAccount.captured.isActive)
	}

	@Test
	fun `subsequent login does not update saved user`() = withOAuthUser(
		mapOf(
			"id" to "yandex-id",
			"default_email" to "user@example.com",
			"first_name" to "Changed",
			"last_name" to "Name",
		),
	) {
		val account = UserAccountEntity("user@example.com", "Original", "User", isAdmin = true, isActive = false)
		every { emailDomainPolicy.isAllowed("user@example.com") } returns true
		every { userAccountRepository.findByEmail("user@example.com") } returns account

		YandexOAuth2UserService(userAccountRepository, emailDomainPolicy).loadUser(userRequest)

		assertEquals("Original", account.firstName)
		assertEquals("User", account.lastName)
		assertTrue(account.isAdmin)
		assertFalse(account.isActive)
		verify(exactly = 0) { userAccountRepository.save(any()) }
		verify(exactly = 0) { userAccountRepository.count() }
	}

	private fun withOAuthUser(attributes: Map<String, Any>, test: () -> Unit) {
		mockkConstructor(DefaultOAuth2UserService::class)
		try {
			val oauthUser = DefaultOAuth2User(
				setOf(SimpleGrantedAuthority("ROLE_USER")),
				attributes,
				"id",
			)
			every { anyConstructed<DefaultOAuth2UserService>().loadUser(userRequest) } returns oauthUser
			test()
		} finally {
			unmockkConstructor(DefaultOAuth2UserService::class)
		}
	}
}
