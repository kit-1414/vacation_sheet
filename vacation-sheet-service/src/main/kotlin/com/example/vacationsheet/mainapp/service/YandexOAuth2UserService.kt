package com.example.vacationsheet.mainapp.service

import com.example.vacationsheet.mainapp.hql.model.UserAccountEntity
import com.example.vacationsheet.mainapp.hql.repository.UserAccountRepository
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class YandexOAuth2UserService(
	private val userAccountRepository: UserAccountRepository,
	private val emailDomainPolicy: EmailDomainPolicy,
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {
	private val delegate = DefaultOAuth2UserService()

	@Transactional
	override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
		val oauthUser = delegate.loadUser(userRequest)
		val email = oauthUser.attributes["default_email"]?.toString()?.trim()?.lowercase()
			?: authenticationError("Yandex response does not contain an email")

		if (!emailDomainPolicy.isAllowed(email)) {
			throw OAuth2AuthenticationException(
				OAuth2Error("email_domain_not_allowed"),
				"The email domain is not allowed",
			)
		}

		if (userAccountRepository.findByEmail(email) == null) {
			val isFirstUser = userAccountRepository.count() == 0L
			userAccountRepository.save(
				UserAccountEntity(
					email = email,
					firstName = oauthUser.optionalTextAttribute("first_name"),
					lastName = oauthUser.optionalTextAttribute("last_name"),
					isAdmin = isFirstUser,
					isActive = true,
				),
			)
		}

		return DefaultOAuth2User(oauthUser.authorities, oauthUser.attributes, "id")
	}

	private fun authenticationError(message: String): Nothing =
		throw OAuth2AuthenticationException(OAuth2Error("invalid_user_info"), message)

	private fun OAuth2User.optionalTextAttribute(name: String): String? =
		attributes[name]?.toString()?.trim()?.takeIf(String::isNotEmpty)
}
