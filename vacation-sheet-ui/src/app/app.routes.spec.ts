import { routes } from './app.routes';

describe('vacation request routes', () => {
  it('allows every authenticated role to open the all requests list and schedule', () => {
    expect(routes.find((route) => route.path === 'profile/vacation-requests/review')?.canActivate).toBeUndefined();
    expect(
      routes.find((route) => route.path === 'profile/vacation-requests/review/schedule')?.canActivate,
    ).toBeUndefined();
  });

  it('keeps manager review protected and exposes a read-only route', () => {
    expect(
      routes.find((route) => route.path === 'profile/vacation-requests/review/:id')?.canActivate,
    ).toBeDefined();
    expect(routes.find((route) => route.path === 'profile/vacation-requests/:id/view')?.data).toEqual({
      viewOnly: true,
    });
  });
});
