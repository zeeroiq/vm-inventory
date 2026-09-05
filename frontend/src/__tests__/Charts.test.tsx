import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BarChart, DonutChart, VersionDistributionChart, DeploymentTrendChart } from '../components/Charts';

describe('Charts Components', () => {
  const mockData = [
    { label: 'Payments Dev', value: 12 },
    { label: 'Checkout QA', value: 8 },
    { label: 'Analytics Stg', value: 4 },
  ];

  it('renders BarChart with items', () => {
    render(<BarChart data={mockData} title="VM Allocation" />);
    expect(screen.getByText('VM Allocation')).toBeDefined();
    expect(screen.getByText('12')).toBeDefined();
    expect(screen.getByText('8')).toBeDefined();
  });

  it('renders DonutChart with total count and legend', () => {
    render(<DonutChart data={mockData} title="Environment Breakdown" />);
    expect(screen.getByText('Environment Breakdown')).toBeDefined();
    expect(screen.getByText('24')).toBeDefined(); // 12 + 8 + 4 = 24
  });

  it('renders VersionDistributionChart', () => {
    const versions = [
      { label: 'v3.2.1', value: 15 },
      { label: 'v3.2.0', value: 7 },
    ];
    render(<VersionDistributionChart data={versions} title="Artifact Versions" />);
    expect(screen.getByText('Artifact Versions')).toBeDefined();
    expect(screen.getByText('v3.2.1')).toBeDefined();
    expect(screen.getByText('15 deployments')).toBeDefined();
  });

  it('renders DeploymentTrendChart', () => {
    const trend = [
      { label: '2026-07-01', value: 5 },
      { label: '2026-07-02', value: 10 },
    ];
    render(<DeploymentTrendChart data={trend} title="Trend Chart" />);
    expect(screen.getByText('Trend Chart')).toBeDefined();
  });
});
