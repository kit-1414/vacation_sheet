import {
  ManagerVacationRequest,
  ManagerVacationRequestState,
} from './manager-vacation-requests.store';

export interface ManagerVacationRequestFilters {
  email: string;
  firstName: string;
  lastName: string;
  states: ManagerVacationRequestState[];
  periodStart: string;
  periodEnd: string;
  projectIds: number[];
}

export function filterManagerVacationRequests(
  requests: ManagerVacationRequest[],
  filters: ManagerVacationRequestFilters,
): ManagerVacationRequest[] {
  const email = filters.email.trim().toLocaleLowerCase();
  const firstName = filters.firstName.trim().toLocaleLowerCase();
  const lastName = filters.lastName.trim().toLocaleLowerCase();
  return requests.filter(({ request, authorProjects }) => {
    const author = request.author;
    if (email && !author.email.toLocaleLowerCase().includes(email)) return false;
    if (firstName && !(author.firstName ?? '').toLocaleLowerCase().includes(firstName))
      return false;
    if (lastName && !(author.lastName ?? '').toLocaleLowerCase().includes(lastName)) return false;
    if (
      filters.states.length &&
      !filters.states.includes(request.requestState as ManagerVacationRequestState)
    ) {
      return false;
    }
    if (filters.periodStart && request.endDate < filters.periodStart) return false;
    if (filters.periodEnd && request.startDate > filters.periodEnd) return false;
    if (
      filters.projectIds.length &&
      !authorProjects.some((project) => filters.projectIds.includes(project.id))
    ) {
      return false;
    }
    return true;
  });
}
