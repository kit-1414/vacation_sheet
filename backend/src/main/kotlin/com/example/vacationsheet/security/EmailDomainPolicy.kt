package com.example.vacationsheet.security

import com.example.vacationsheet.config.SecurityProperties
import org.springframework.stereotype.Component

@Component
class EmailDomainPolicy(properties: SecurityProperties) {
	private val allowedDomain = properties.allowedEmailDomain.trim().removePrefix("@").lowercase()

	fun isAllowed(email: String): Boolean {
		if (allowedDomain.isEmpty()) return true
		return email.substringAfterLast('@', missingDelimiterValue = "").lowercase() == allowedDomain
	}
}
