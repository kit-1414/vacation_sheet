package com.example.vacationsheet.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.security")
data class SecurityProperties(
	val allowedEmailDomain: String = "",
)
