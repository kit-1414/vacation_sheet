package com.example.vacationsheet.web

import com.example.vacationsheet.user.UserAccountRepository
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

data class CurrentUserResponse(
	val id: UUID,
	val email: String,
	val displayName: String?,
)

data class CsrfTokenResponse(
	val token: String,
)

@RestController
@RequestMapping("/api/auth")
class AuthController(
	private val userAccountRepository: UserAccountRepository,
) {
	@GetMapping("/me")
	fun currentUser(@AuthenticationPrincipal principal: OAuth2User): CurrentUserResponse {
		val yandexId = principal.getAttribute<Any>("id")?.toString()
			?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
		val account = userAccountRepository.findByYandexId(yandexId)
			?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
		return CurrentUserResponse(
			id = requireNotNull(account.id),
			email = account.email,
			displayName = account.displayName,
		)
	}

	@GetMapping("/csrf")
	fun csrf(csrfToken: CsrfToken) = CsrfTokenResponse(csrfToken.token)
}
