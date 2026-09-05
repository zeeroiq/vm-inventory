import React from 'react';
import type { ChartDataPoint } from '../api';

export const BarChart: React.FC<{ data: ChartDataPoint[]; title?: string }> = ({ data, title }) => {
  if (!data || data.length === 0) {
    return <div className="text-gray-400 text-sm py-8 text-center">No chart data available</div>;
  }

  const maxValue = Math.max(...data.map(d => d.value), 1);

  return (
    <div className="w-full">
      {title && <h4 className="text-sm font-semibold text-gray-700 mb-3">{title}</h4>}
      <div className="flex items-end gap-3 h-44 pt-4 border-b border-gray-100">
        {data.map((item, idx) => {
          const heightPercent = (item.value / maxValue) * 100;
          return (
            <div key={idx} className="flex-1 flex flex-col items-center group relative">
              {/* Tooltip */}
              <div className="absolute -top-7 opacity-0 group-hover:opacity-100 transition-opacity bg-gray-900 text-white text-xs px-2 py-1 rounded shadow pointer-events-none whitespace-nowrap z-10">
                {item.label}: <span className="font-bold">{item.value} VMs</span>
              </div>
              <span className="text-xs font-semibold text-gray-600 mb-1">{item.value}</span>
              <div className="w-full max-w-[42px] bg-indigo-50 rounded-t-md overflow-hidden flex items-end h-32">
                <div
                  className="w-full bg-indigo-600 group-hover:bg-indigo-700 transition-all rounded-t-md"
                  style={{ height: `${Math.max(heightPercent, 8)}%` }}
                />
              </div>
              <span className="text-[11px] text-gray-500 mt-2 truncate w-full text-center" title={item.label}>
                {item.label.split(' ')[0]}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export const DonutChart: React.FC<{ data: ChartDataPoint[]; title?: string }> = ({ data, title }) => {
  if (!data || data.length === 0) {
    return <div className="text-gray-400 text-sm py-8 text-center">No environment data available</div>;
  }

  const total = data.reduce((acc, d) => acc + d.value, 0) || 1;
  const colors: Record<string, string> = {
    Dev: '#3b82f6',
    QA: '#10b981',
    UAT: '#f59e0b',
    Staging: '#8b5cf6',
    Performance: '#ec4899',
    Production: '#ef4444',
  };

  let cumulativePercent = 0;

  return (
    <div className="w-full">
      {title && <h4 className="text-sm font-semibold text-gray-700 mb-3">{title}</h4>}
      <div className="flex flex-col sm:flex-row items-center gap-6">
        <div className="relative w-36 h-36 flex-shrink-0">
          <svg viewBox="0 0 100 100" className="w-full h-full transform -rotate-90">
            {data.map((item, idx) => {
              const percent = item.value / total;
              const strokeDasharray = `${percent * 283} 283`;
              const strokeDashoffset = -cumulativePercent * 283;
              cumulativePercent += percent;
              const color = colors[item.label] || '#64748b';

              return (
                <circle
                  key={idx}
                  cx="50"
                  cy="50"
                  r="45"
                  fill="transparent"
                  stroke={color}
                  strokeWidth="10"
                  strokeDasharray={strokeDasharray}
                  strokeDashoffset={strokeDashoffset}
                  className="transition-all duration-500 hover:opacity-80"
                />
              );
            })}
          </svg>
          <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
            <span className="text-2xl font-bold text-gray-800">{total}</span>
            <span className="text-[10px] text-gray-400 uppercase tracking-wider">VMs</span>
          </div>
        </div>

        <div className="flex-1 grid grid-cols-2 gap-2 text-xs w-full">
          {data.map((item, idx) => {
            const color = colors[item.label] || '#64748b';
            const pct = Math.round((item.value / total) * 100);
            return (
              <div key={idx} className="flex items-center gap-2">
                <span className="w-2.5 h-2.5 rounded-full flex-shrink-0" style={{ backgroundColor: color }} />
                <span className="text-gray-600 truncate flex-1">{item.label}</span>
                <span className="font-semibold text-gray-800">{item.value} ({pct}%)</span>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};

export const VersionDistributionChart: React.FC<{ data: ChartDataPoint[]; title?: string }> = ({ data, title }) => {
  if (!data || data.length === 0) {
    return <div className="text-gray-400 text-sm py-8 text-center">No version data available</div>;
  }

  const maxVal = Math.max(...data.map(d => d.value), 1);

  return (
    <div className="w-full space-y-3">
      {title && <h4 className="text-sm font-semibold text-gray-700 mb-2">{title}</h4>}
      {data.map((item, idx) => {
        const pct = Math.round((item.value / maxVal) * 100);
        return (
          <div key={idx} className="space-y-1">
            <div className="flex justify-between text-xs">
              <span className="font-medium text-gray-700 truncate max-w-[200px]" title={item.label}>
                {item.label}
              </span>
              <span className="text-gray-500 font-semibold">{item.value} deployments</span>
            </div>
            <div className="w-full bg-gray-100 rounded-full h-2 overflow-hidden">
              <div
                className="bg-emerald-500 h-2 rounded-full transition-all duration-500"
                style={{ width: `${Math.max(pct, 5)}%` }}
              />
            </div>
          </div>
        );
      })}
    </div>
  );
};

export const DeploymentTrendChart: React.FC<{ data: ChartDataPoint[]; title?: string }> = ({ data, title }) => {
  if (!data || data.length === 0) {
    return <div className="text-gray-400 text-sm py-8 text-center">No trend data available</div>;
  }

  const values = data.map(d => d.value);
  const maxVal = Math.max(...values, 5);
  const width = 500;
  const height = 150;
  const padding = 30;

  const points = data.map((d, i) => {
    const x = padding + (i * (width - 2 * padding)) / Math.max(data.length - 1, 1);
    const y = height - padding - (d.value / maxVal) * (height - 2 * padding);
    return { x, y, ...d };
  });

  const pathD = points.reduce((acc, p, i) => {
    return i === 0 ? `M ${p.x},${p.y}` : `${acc} L ${p.x},${p.y}`;
  }, '');

  const areaD = `${pathD} L ${points[points.length - 1].x},${height - padding} L ${points[0].x},${height - padding} Z`;

  return (
    <div className="w-full">
      {title && <h4 className="text-sm font-semibold text-gray-700 mb-3">{title}</h4>}
      <div className="relative w-full h-40">
        <svg viewBox={`0 0 ${width} ${height}`} className="w-full h-full overflow-visible">
          <defs>
            <linearGradient id="trendGradient" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#3b82f6" stopOpacity="0.3" />
              <stop offset="100%" stopColor="#3b82f6" stopOpacity="0.0" />
            </linearGradient>
          </defs>

          {/* Grid lines */}
          <line x1={padding} y1={height - padding} x2={width - padding} y2={height - padding} stroke="#e2e8f0" strokeWidth="1" />
          <line x1={padding} y1={padding} x2={width - padding} y2={padding} stroke="#f1f5f9" strokeWidth="1" strokeDasharray="4" />

          {/* Area fill */}
          <path d={areaD} fill="url(#trendGradient)" />

          {/* Line */}
          <path d={pathD} fill="none" stroke="#2563eb" strokeWidth="2.5" strokeLinecap="round" />

          {/* Data Points */}
          {points.map((p, idx) => (
            <g key={idx} className="group cursor-pointer">
              <circle cx={p.x} cy={p.y} r="4" fill="#2563eb" stroke="#ffffff" strokeWidth="2" />
              <text
                x={p.x}
                y={p.y - 8}
                textAnchor="middle"
                className="text-[10px] font-bold fill-gray-700 opacity-0 group-hover:opacity-100 transition-opacity"
              >
                {p.value}
              </text>
              <text
                x={p.x}
                y={height - padding + 15}
                textAnchor="middle"
                className="text-[9px] fill-gray-400"
              >
                {p.label.substring(5)}
              </text>
            </g>
          ))}
        </svg>
      </div>
    </div>
  );
};
