package com.example.vacationsheet.mainapp.hql.mapper

import com.example.vacationsheet.mainapp.hql.dto.UserAccountDto
import com.example.vacationsheet.mainapp.hql.model.UserAccountEntity
import org.springframework.stereotype.Component

@Component
class UserAccountMapper {
	fun toDto(entity: UserAccountEntity) = UserAccountDto(
		id = requireNotNull(entity.id),
		email = entity.email,
		displayName = entity.displayName,
		ctime = entity.ctime,
		utime = entity.utime,
	)
}
