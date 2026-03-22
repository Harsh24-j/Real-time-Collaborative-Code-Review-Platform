import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import LoginForm from '../LoginForm';
import { authAPI } from '../../../services/api';
import { useAuthStore } from '../../../hooks/useAuthStore';

// Mock the API
vi.mock('../../../services/api', () => ({
  authAPI: {
    login: vi.fn(),
  },
}));

// Mock the Auth Store
vi.mock('../../../hooks/useAuthStore', () => ({
  useAuthStore: vi.fn(),
}));

// Mock react-hot-toast
vi.mock('react-hot-toast', () => ({
  default: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

// Mock useNavigate
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
      ...actual,
      useNavigate: () => mockNavigate,
    };
});

// Mock components that might be problematic in tests (e.g. Typewriter with timers)
vi.mock('../../common/Typewriter', () => ({
    default: ({ text }) => <span>{text}</span>
}));
vi.mock('../../common/FloatingOrbs', () => ({
    default: () => <div data-testid="floating-orbs" />
}));
vi.mock('../../common/BackgroundWrapper', () => ({
    default: ({ children }) => <div>{children}</div>
}));

describe('LoginForm Component', () => {
  const mockLogin = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    useAuthStore.mockReturnValue({ login: mockLogin });
  });

  it('renders login form correctly', () => {
    render(
      <BrowserRouter>
        <LoginForm />
      </BrowserRouter>
    );

    expect(screen.getByPlaceholderText(/Enter your username/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Enter your password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Sign In/i })).toBeInTheDocument();
  });

  it('handles input changes', () => {
    render(
      <BrowserRouter>
        <LoginForm />
      </BrowserRouter>
    );

    const usernameInput = screen.getByPlaceholderText(/Enter your username/i);
    const passwordInput = screen.getByPlaceholderText(/Enter your password/i);

    fireEvent.change(usernameInput, { target: { value: 'testuser', name: 'username' } });
    fireEvent.change(passwordInput, { target: { value: 'password123', name: 'password' } });

    expect(usernameInput.value).toBe('testuser');
    expect(passwordInput.value).toBe('password123');
  });

  it('submits form successfully', async () => {
    const loginData = {
      token: 'fake-jwt-token',
      userId: 1,
      username: 'testuser',
    };

    authAPI.login.mockResolvedValueOnce(loginData);

    render(
      <BrowserRouter>
        <LoginForm />
      </BrowserRouter>
    );

    fireEvent.change(screen.getByPlaceholderText(/Enter your username/i), {
      target: { value: 'testuser', name: 'username' },
    });
    fireEvent.change(screen.getByPlaceholderText(/Enter your password/i), {
      target: { value: 'password123', name: 'password' },
    });

    fireEvent.click(screen.getByRole('button', { name: /Sign In/i }));

    await waitFor(() => {
      expect(authAPI.login).toHaveBeenCalledWith({
        username: 'testuser',
        password: 'password123',
      });
      expect(mockLogin).toHaveBeenCalledWith('fake-jwt-token', expect.any(Object));
    });
  });

  it('shows error toast on login failure', async () => {
    authAPI.login.mockRejectedValueOnce({
      response: { data: { message: 'Invalid credentials' } },
    });

    render(
      <BrowserRouter>
        <LoginForm />
      </BrowserRouter>
    );

    fireEvent.click(screen.getByRole('button', { name: /Sign In/i }));

    await waitFor(() => {
      expect(authAPI.login).toHaveBeenCalled();
    });
  });
});
