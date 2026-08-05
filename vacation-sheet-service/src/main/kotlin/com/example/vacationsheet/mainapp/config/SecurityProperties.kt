package com.example.vacationsheet.mainapp.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.security")
data class SecurityProperties(
	val allowedEmailDomain: String = "",
)
