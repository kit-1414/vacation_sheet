package com.example.vacationsheet.user

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserAccountRepository : JpaRepository<UserAccount, UUID> {
	fun findByYandexId(yandexId: String): UserAccount?
}
