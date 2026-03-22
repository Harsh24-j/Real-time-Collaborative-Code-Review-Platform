import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import ReviewList from '../ReviewList';
import { BrowserRouter } from 'react-router-dom';

vi.mock('../../services/api', () => ({
  reviewAPI: {
    getAll: vi.fn(() => Promise.resolve({
      content: [
        { id: 1, title: 'Code Review A', status: 'OPEN', language: 'JavaScript', creatorUsername: 'user1' },
        { id: 2, title: 'Code Review B', status: 'APPROVED', language: 'Java', creatorUsername: 'user2' }
      ],
      totalPages: 1
    })),
  }
}));

describe('ReviewList Component', () => {
  it('renders list of reviews', async () => {
    render(
      <BrowserRouter>
        <ReviewList />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Code Review A')).toBeInTheDocument();
      expect(screen.getByText('Code Review B')).toBeInTheDocument();
      expect(screen.getByText('JavaScript')).toBeInTheDocument();
      expect(screen.getByText('Java')).toBeInTheDocument();
    });
  });
});
