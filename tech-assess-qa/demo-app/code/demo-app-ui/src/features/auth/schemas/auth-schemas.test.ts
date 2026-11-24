
import { describe, it, expect } from 'vitest';
import { loginSchema, signupSchema } from './auth-schemas';

describe('Login Schema Validation', () => {
  it('should accept valid email and password', () => {
    const result = loginSchema.safeParse({
      email: 'claimant@example.com',
      password: 'Password123',
    });

    expect(result.success).toBe(true);
  });

  it('should reject invalid email format', () => {
    const result = loginSchema.safeParse({
      email: 'invalid-email',
      password: 'Password123',
    });

    expect(result.success).toBe(false);
  });

  it('should reject empty password', () => {
    const result = loginSchema.safeParse({
      email: 'claimant@example.com',
      password: '',
    });

    expect(result.success).toBe(false);
  });

  it('should reject empty email', () => {
    const result = loginSchema.safeParse({
      email: '',
      password: 'Password123',
    });

    expect(result.success).toBe(false);
  });
});

describe('Signup Schema Validation', () => {
  it('should accept valid signup details', () => {
    const result = signupSchema.safeParse({
      name: 'Test Claimant',
      email: 'claimant@example.com',
      password: 'Password123',
    });

    expect(result.success).toBe(true);
  });

  it('should reject password shorter than 8 characters', () => {
    const result = signupSchema.safeParse({
      name: 'Test Claimant',
      email: 'claimant@example.com',
      password: 'Pass1',
    });

    expect(result.success).toBe(false);
  });

  it('should reject password without uppercase letter', () => {
    const result = signupSchema.safeParse({
      name: 'Test Claimant',
      email: 'claimant@example.com',
      password: 'password123',
    });

    expect(result.success).toBe(false);
  });

  it('should reject password without a number', () => {
    const result = signupSchema.safeParse({
      name: 'Test Claimant',
      email: 'claimant@example.com',
      password: 'PasswordOnly',
    });

    expect(result.success).toBe(false);
  });
});
