import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { Project, ProjectsStore } from './projects.store';

describe('ProjectsStore', () => {
  let store: ProjectsStore;
  let http: HttpTestingController;

  const project: Project = {
    id: 1,
    name: 'Vacation Sheet',
    description: null,
    members: [],
    managers: [],
    ctime: '2026-08-05T00:00:00Z',
    utime: '2026-08-05T00:00:00Z',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    store = TestBed.inject(ProjectsStore);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads projects', () => {
    store.load();

    http.expectOne('/api/projects').flush([project]);

    expect(store.projects()).toEqual([project]);
  });

  it('creates a project', () => {
    store.create({ name: 'Vacation Sheet', description: null });

    const request = http.expectOne('/api/projects');
    expect(request.request.method).toBe('POST');
    request.flush(project);

    expect(store.projects()).toEqual([project]);
  });

  it('assigns a project manager', () => {
    store.addManager(1, 2);

    const request = http.expectOne('/api/projects/1/managers/2');
    expect(request.request.method).toBe('PUT');
    request.flush(project);
  });
});
