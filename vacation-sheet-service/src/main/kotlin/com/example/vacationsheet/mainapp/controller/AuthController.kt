package com.example.vacationsheet.mainapp.controller

import com.example.vacationsheet.mainapp.dto.CurrentUserDto
import com.example.vacationsheet.mainapp.service.CurrentUserService
import com.example.vacationsheet.mainapp.utils.logaspect.LogPublicMethods
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView

@Tag(name = "Authentication")
@RestController
@LogPublicMethods
class AuthController(
	private val currentUserService: CurrentUserService,
	@Value("\${app.frontend.url}") private val frontendUrl: String,
) {
	@GetMapping("/")
	fun oauthSuccess(): RedirectView = RedirectView(frontendUrl)

	@Operation(summary = "Get the current authenticated user")
	@GetMapping("/api/auth/me")
	fun currentUser(): CurrentUserDto = currentUserService.getCurrentUser()

}
