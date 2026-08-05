package com.example.vacationsheet.mainapp.service

import com.example.vacationsheet.mainapp.config.SecurityProperties
import org.springframework.stereotype.Component

@Component
class EmailDomainPolicy(properties: SecurityProperties) {
	private val allowedDomain = properties.allowedEmailDomain.trim().removePrefix("@").lowercase()

	fun isAllowed(email: String): Boolean {
		if (allowedDomain.isEmpty()) return true
		return email.substringAfterLast('@', missingDelimiterValue = "").lowercase() == allowedDomain
	}
}
