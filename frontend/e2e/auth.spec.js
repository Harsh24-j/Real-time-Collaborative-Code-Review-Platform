import { test, expect } from '@playwright/test';

test.describe('Authentication Flow', () => {
  test('should allow a user to navigate to login page', async ({ page }) => {
    await page.goto('/');
    
    // Check if redirect to login or presence of login elements
    const loginTitle = page.locator('h2', { hasText: 'Login' });
    await expect(loginTitle).toBeVisible();
  });

  test('should show error message on invalid login', async ({ page }) => {
    await page.goto('/login');
    
    await page.fill('input[name="username"]', 'invalid_user');
    await page.fill('input[name="password"]', 'wrong_password');
    await page.click('button[type="submit"]');

    // Assuming a toast or error message appears
    const errorMessage = page.locator('text=Invalid credentials');
    await expect(errorMessage).toBeVisible();
  });
});
