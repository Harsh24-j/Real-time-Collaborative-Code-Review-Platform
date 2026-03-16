import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import Profile from '../Profile';
import { BrowserRouter } from 'react-router-dom';

vi.mock('../../services/api', () => ({
  userAPI: {
    getProfile: vi.fn(() => Promise.resolve({
      username: 'testuser',
      fullName: 'Test User',
      email: 'test@example.com',
      points: 500,
      badges: [{ id: 1, name: 'Top Reviewer', description: 'Awarded for many reviews' }]
    })),
  }
}));

vi.mock('../../hooks/useAuthStore', () => ({
  useAuthStore: () => ({
    user: { username: 'testuser' }
  }),
}));

describe('Profile Component', () => {
  it('renders user profile details', async () => {
    render(
      <BrowserRouter>
        <Profile />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Test User')).toBeInTheDocument();
      expect(screen.getByText('test@example.com')).toBeInTheDocument();
      expect(screen.getByText('500')).toBeInTheDocument();
      expect(screen.getByText('Top Reviewer')).toBeInTheDocument();
    });
  });
});
