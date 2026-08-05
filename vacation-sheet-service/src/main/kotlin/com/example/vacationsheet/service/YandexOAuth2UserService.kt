package com.example.vacationsheet.service

import com.example.vacationsheet.entity.UserAccount
import com.example.vacationsheet.repository.UserAccountRepository
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class YandexOAuth2UserService(
	private val userAccountRepository: UserAccountRepository,
	private val emailDomainPolicy: EmailDomainPolicy,
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {
	private val delegate = DefaultOAuth2UserService()

	@Transactional
	override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
		val oauthUser = delegate.loadUser(userRequest)
		val yandexId = oauthUser.attributes["id"]?.toString()
			?: authenticationError("Yandex response does not contain an id")
		val email = oauthUser.attributes["default_email"]?.toString()?.trim()?.lowercase()
			?: authenticationError("Yandex response does not contain an email")

		if (!emailDomainPolicy.isAllowed(email)) {
			throw OAuth2AuthenticationException(
				OAuth2Error("email_domain_not_allowed"),
				"The email domain is not allowed",
			)
		}

		val displayName = oauthUser.attributes["display_name"]?.toString()
		val account = userAccountRepository.findByYandexId(yandexId)
		if (account == null) {
			userAccountRepository.save(UserAccount(yandexId = yandexId, email = email, displayName = displayName))
		} else {
			account.email = email
			account.displayName = displayName
			account.updatedAt = Instant.now()
		}

		return DefaultOAuth2User(oauthUser.authorities, oauthUser.attributes, "id")
	}

	private fun authenticationError(message: String): Nothing =
		throw OAuth2AuthenticationException(OAuth2Error("invalid_user_info"), message)
}
