import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import Dashboard from '../Dashboard';
import { BrowserRouter } from 'react-router-dom';

// Mock the services/hooks
vi.mock('../../services/api', () => ({
  analyticsAPI: {
    getDashboard: vi.fn(() => Promise.resolve({
      totalReviews: 10,
      totalComments: 25,
      avgQualityScore: 8.5,
      openReviews: 3,
      reviewsLast24h: 2,
      criticalIssues: 1
    })),
    getTrends: vi.fn(() => Promise.resolve({
      languageDistribution: { java: 5, javascript: 3 }
    })),
  },
  reviewAPI: {
    getAll: vi.fn(() => Promise.resolve({
      content: [
        { id: 1, title: 'Test Review 1', status: 'OPEN', language: 'Java', creatorUsername: 'dev1' }
      ]
    })),
  }
}));

vi.mock('../../hooks/useAuthStore', () => ({
  useAuthStore: () => ({
    user: { username: 'testuser', points: 150, fullName: 'Test User' }
  }),
}));

describe('Dashboard Component', () => {
  it('renders dashboard statistics after loading', async () => {
    render(
      <BrowserRouter>
        <Dashboard />
      </BrowserRouter>
    );

    // Should show welcome message
    expect(screen.getByText(/Welcome back/i)).toBeInTheDocument();
    expect(screen.getByText(/Test/i)).toBeInTheDocument();

    // Wait for stats to load
    await waitFor(() => {
      expect(screen.getByText('10')).toBeInTheDocument(); // total reviews
      expect(screen.getByText('25')).toBeInTheDocument(); // total comments
      expect(screen.getByText('150')).toBeInTheDocument(); // points
    });

    // Check recent review
    expect(screen.getByText('Test Review 1')).toBeInTheDocument();
  });
});
