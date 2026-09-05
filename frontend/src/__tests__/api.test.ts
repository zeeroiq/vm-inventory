import { describe, it, expect } from 'vitest';
import { exportInstancesCsvUrl } from '../api';

describe('API Utilities', () => {
  it('formats exportInstancesCsvUrl correctly with query params', () => {
    const url = exportInstancesCsvUrl({
      projectId: 'payments-dev',
      environment: 'Dev',
      status: 'RUNNING',
    });

    expect(url).toContain('/instances/export?');
    expect(url).toContain('projectId=payments-dev');
    expect(url).toContain('environment=Dev');
    expect(url).toContain('status=RUNNING');
  });

  it('formats exportInstancesCsvUrl when params are empty', () => {
    const url = exportInstancesCsvUrl({});
    expect(url).toContain('/instances/export');
  });
});
