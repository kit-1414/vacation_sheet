package com.example.vacationsheet.mainapp.service

import com.example.vacationsheet.mainapp.hql.model.UserAccountEntity
import com.example.vacationsheet.mainapp.hql.repository.ProjectRepository
import org.springframework.stereotype.Service

@Service
class UserRoleService(
	private val projectRepository: ProjectRepository,
) {
	fun getRoles(user: UserAccountEntity): Set<UserRole> {
		if (!user.isActive) return setOf(UserRole.NOBODY)

		return buildSet {
			if (user.isAdmin) add(UserRole.ADMIN)
			if (projectRepository.existsByManagersId(requireNotNull(user.id))) {
				add(UserRole.MANAGER)
			} else {
				add(UserRole.USER)
			}
		}
	}
}
