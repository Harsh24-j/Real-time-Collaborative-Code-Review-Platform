import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import ReviewForm from '../ReviewForm';
import { BrowserRouter } from 'react-router-dom';

// Mock the services/toast
vi.mock('react-hot-toast', () => ({
  default: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

vi.mock('../../services/api', () => ({
  reviewService: {
    create: vi.fn(() => Promise.resolve({ id: 1 })),
  },
}));

describe('ReviewForm Component', () => {
  it('renders all form fields', () => {
    render(
      <BrowserRouter>
        <ReviewForm />
      </BrowserRouter>
    );

    expect(screen.getByLabelText(/Review Title/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Language/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Paste your code here/i)).toBeInTheDocument();
  });

  it('updates input values on change', () => {
    render(
      <BrowserRouter>
        <ReviewForm />
      </BrowserRouter>
    );

    const titleInput = screen.getByLabelText(/Review Title/i);
    fireEvent.change(titleInput, { target: { value: 'New Review' } });
    expect(titleInput.value).toBe('New Review');
  });
});
