import { signal } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { AuthStore } from '../auth.store';
import { VacationRequestEditorPage } from './vacation-request-editor-page';
import { VacationRequestsStore } from './vacation-requests.store';

describe('VacationRequestEditorPage', () => {
  it('opens an approved request in read-only mode', async () => {
    const request = {
      id: 10,
      title: 'Vacation',
      requestState: 'APPROVED' as const,
      vacationType: 'PAYMENT_VACATION' as const,
      startDate: '2026-09-10',
      endDate: '2026-09-20',
      userComments: null,
      managerComments: 'Approved',
      author: { id: 1, email: 'user@example.com', firstName: 'Test', lastName: 'User' },
      manager: null,
      ctime: null,
      utime: null,
    };
    await TestBed.configureTestingModule({
      imports: [VacationRequestEditorPage],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => '10' }, data: {} } },
        },
        {
          provide: AuthStore,
          useValue: { user: signal({ id: 1 }), canManageVacationRequests: () => true },
        },
        {
          provide: VacationRequestsStore,
          useValue: {
            error: signal<string | null>(null),
            saving: signal(false),
            loadOne: () => of(request),
          },
        },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(VacationRequestEditorPage);
    fixture.detectChanges();
    const component = fixture.componentInstance as unknown as {
      readOnly: () => boolean;
      form: FormGroup;
    };

    expect(component.readOnly()).toBe(true);
    expect(component.form.disabled).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('нельзя изменить');
  });

  it('opens another user ready request in read-only mode', async () => {
    const request = {
      id: 10,
      title: 'Vacation',
      requestState: 'READY' as const,
      vacationType: 'PAYMENT_VACATION' as const,
      startDate: '2026-09-10',
      endDate: '2026-09-20',
      userComments: null,
      managerComments: null,
      author: { id: 2, email: 'other@example.com', firstName: null, lastName: null },
      manager: null,
      ctime: null,
      utime: null,
    };
    await TestBed.configureTestingModule({
      imports: [VacationRequestEditorPage],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => '10' }, data: {} } },
        },
        {
          provide: AuthStore,
          useValue: { user: signal({ id: 1 }), canManageVacationRequests: () => true },
        },
        {
          provide: VacationRequestsStore,
          useValue: {
            error: signal<string | null>(null),
            saving: signal(false),
            loadOne: () => of(request),
          },
        },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(VacationRequestEditorPage);
    fixture.detectChanges();
    const component = fixture.componentInstance as unknown as {
      readOnly: () => boolean;
      form: FormGroup;
    };

    expect(component.readOnly()).toBe(true);
    expect(component.form.disabled).toBe(true);
  });
});
