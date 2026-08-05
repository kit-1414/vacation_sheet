import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { Project, ProjectsStore } from './projects.store';

describe('ProjectsStore', () => {
  let store: ProjectsStore;
  let http: HttpTestingController;

  const project: Project = {
    id: 'project-1',
    name: 'Vacation Sheet',
    description: null,
    members: [],
    createdAt: '2026-08-05T00:00:00Z',
    updatedAt: '2026-08-05T00:00:00Z',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    store = TestBed.inject(ProjectsStore);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads projects and users together', () => {
    store.load();

    http.expectOne('/api/projects').flush([project]);
    http.expectOne('/api/users').flush([
      { id: 'user-1', email: 'user@example.com', displayName: null },
    ]);

    expect(store.projects()).toEqual([project]);
    expect(store.users()).toHaveLength(1);
  });

  it('creates a project', () => {
    store.create({ name: 'Vacation Sheet', description: null });

    const request = http.expectOne('/api/projects');
    expect(request.request.method).toBe('POST');
    request.flush(project);

    expect(store.projects()).toEqual([project]);
  });
});
