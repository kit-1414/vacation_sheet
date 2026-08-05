package com.example.vacationsheet.mainapp.service

import com.example.vacationsheet.mainapp.config.SecurityProperties
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmailDomainPolicyTest {
	@Test
	fun `empty domain allows every email`() {
		assertTrue(EmailDomainPolicy(SecurityProperties()).isAllowed("user@yandex.ru"))
	}

	@Test
	fun `configured domain must match exactly ignoring case`() {
		val policy = EmailDomainPolicy(SecurityProperties("Example.COM"))

		assertTrue(policy.isAllowed("user@example.com"))
		assertFalse(policy.isAllowed("user@sub.example.com"))
		assertFalse(policy.isAllowed("user@other.com"))
	}
}
