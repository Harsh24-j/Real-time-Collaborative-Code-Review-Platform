import { create } from 'zustand'
import { persist } from 'zustand/middleware'

/**
 * useAuthStore — Zustand store for authentication state (persisted to localStorage).
 * Skills: JavaScript, Front-End Web Development
 */
export const useAuthStore = create(
    persist(
        (set) => ({
            user: null,
            token: null,
            isAuthenticated: false,

            /** Called after a successful login — stores token + user object. */
            login: (token, user) => {
                localStorage.setItem('token', token)
                set({ token, user, isAuthenticated: true })
            },

            /** Called on logout or 401 — clears all auth state. */
            logout: () => {
                localStorage.removeItem('token')
                set({ token: null, user: null, isAuthenticated: false })
            },

            /** Update the locally cached user profile (e.g. after a profile PUT). */
            updateUser: (updatedUser) =>
                set((state) => ({ user: { ...state.user, ...updatedUser } })),
        }),
        {
            name: 'auth-storage',       // localStorage key
            partialize: (state) => ({      // only persist token + user, not functions
                token: state.token,
                user: state.user,
                isAuthenticated: state.isAuthenticated,
            }),
        }
    )
)
