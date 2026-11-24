import { test as base, expect } from '@playwright/test';
import { LoginPage } from './login.page';

type TestFixtures = {
  loginPage: LoginPage;
};

export const test = base.extend<TestFixtures>({
  loginPage: async ({ page }, use) => {
    const loginPage = new LoginPage(page);
    await use(loginPage);
  },
});

export { expect };