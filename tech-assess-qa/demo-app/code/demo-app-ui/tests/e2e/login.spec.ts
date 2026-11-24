
import { test, expect } from './fixtures';

test.describe('Login Page UI Tests', () => {

  test('TC_UI_001 - Login page should load successfully', async ({ page, loginPage }) => {
    await loginPage.goto();

    await expect(page.getByRole('heading', { name: 'Log In' })).toBeVisible();
    await expect(loginPage.emailInput).toBeVisible();
    await expect(loginPage.passwordInput).toBeVisible();
    await expect(loginPage.loginButton).toBeVisible();
    await expect(loginPage.signupLink).toBeVisible();
  });

  test('TC_UI_002 - Password visibility toggle should work', async ({ page, loginPage }) => {
    await loginPage.goto();

    await loginPage.passwordInput.fill('TestPassword123');

    await expect(loginPage.passwordInput).toHaveAttribute('type', 'password');

    await page.getByRole('button', { name: 'Show password' }).click();

    await expect(loginPage.passwordInput).toHaveAttribute('type', 'text');

    await page.getByRole('button', { name: 'Hide password' }).click();

    await expect(loginPage.passwordInput).toHaveAttribute('type', 'password');
  });

  test('TC_UI_003 - Invalid email should show validation error', async ({ page, loginPage }) => {
    await loginPage.goto();

    await loginPage.emailInput.fill('invalid-email');
    await loginPage.passwordInput.fill('TestPassword123');
    await loginPage.loginButton.click();

    await expect(page.getByText('Invalid email format')).toBeVisible();
  });

  test('TC_UI_004 - Empty login form should show validation errors', async ({ page, loginPage }) => {
    await loginPage.goto();

    await loginPage.loginButton.click();

    await expect(page.getByText('Invalid email format')).toBeVisible();
    await expect(page.getByText('Password is required')).toBeVisible();
  });

  test('TC_UI_005 - Expired session should display an error message', async ({ page }) => {
    await page.goto('/login?session_expired=true');

    await expect(
      page.getByText('Your session has expired. Please log in again.', { exact: true })
    ).toBeVisible();

    await expect(page).toHaveURL(/\/login$/);
  });

  test('TC_UI_006 - Signup link should navigate to signup page', async ({ page, loginPage }) => {
    await loginPage.goto();

    await loginPage.signupLink.click();

    await expect(page).toHaveURL(/\/signup$/);

    await expect(
      page.getByRole('heading', { name: 'Create Account' })
    ).toBeVisible();
  });

});
