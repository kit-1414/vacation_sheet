package com.example.vacationsheet.mainapp.controller

import com.example.vacationsheet.mainapp.utils.logaspect.LogPublicMethods
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.view.RedirectView

@Controller
@LogPublicMethods
class LoginController {
	@GetMapping("/login")
	fun login(): RedirectView = RedirectView("/oauth2/authorization/yandex")

	@GetMapping("/login/start")
	fun loginStart(): RedirectView = RedirectView("/oauth2/authorization/yandex")
}
