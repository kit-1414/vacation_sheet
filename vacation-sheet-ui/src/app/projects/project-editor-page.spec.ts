import { signal } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';

import { AuthStore } from '../auth.store';
import { ProjectEditorPage } from './project-editor-page';
import { ProjectsStore } from './projects.store';

describe('ProjectEditorPage', () => {
  it('validates project names according to the documented character set', async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectEditorPage],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => null } } } },
        { provide: AuthStore, useValue: { canAdminister: () => true } },
        { provide: ProjectsStore, useValue: { saving: signal(false) } },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(ProjectEditorPage);
    const component = fixture.componentInstance as unknown as { form: FormGroup };
    const control = component.form.controls['name'];

    for (const name of ['Project123', 'Проект123', 'project-name.test_value']) {
      control.setValue(name);
      expect(control.valid).toBe(true);
    }

    for (const name of ['Project name', 'Project/name', 'Project@name']) {
      control.setValue(name);
      expect(control.hasError('pattern')).toBe(true);
    }

    control.setValue('');
    expect(control.hasError('required')).toBe(true);
    control.setValue('a'.repeat(101));
    expect(control.hasError('maxlength')).toBe(true);
  });
});
