import { FormControl, FormGroup } from '@angular/forms';

import { dateRangeValidator, strictDateValidator } from './vacation-request-editor-page';

describe('strictDateValidator', () => {
  it.each(['92026-03-01', '2026-13-01', '2026-04-31', '2025-02-29', '0000-01-01'])(
    'rejects invalid calendar date %s',
    (value) => {
      const control = new FormControl(value, strictDateValidator);

      expect(control.hasError('invalidDate')).toBe(true);
    },
  );

  it.each(['2026-03-01', '2024-02-29', '2000-02-29'])('accepts valid calendar date %s', (value) => {
    const control = new FormControl(value, strictDateValidator);

    expect(control.valid).toBe(true);
  });
});

describe('dateRangeValidator', () => {
  it('rejects an end date before the start date', () => {
    const form = new FormGroup(
      {
        startDate: new FormControl('2026-09-20'),
        endDate: new FormControl('2026-09-10'),
      },
      dateRangeValidator,
    );

    expect(form.hasError('dateRange')).toBe(true);
  });

  it('accepts an end date equal to the start date', () => {
    const form = new FormGroup(
      {
        startDate: new FormControl('2026-09-20'),
        endDate: new FormControl('2026-09-20'),
      },
      dateRangeValidator,
    );

    expect(form.valid).toBe(true);
  });
});
