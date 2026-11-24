/**
 * @vitest-environment jsdom
 */
import { describe, it, expect, afterEach } from 'vitest';
import { render, screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useForm } from 'react-hook-form';
import { PasswordField } from './password-field';

afterEach(() => {
  cleanup();
});

function TestPasswordField({ error }: { error?: string }) {
  const { register } = useForm<{ password: string }>();

  return (
    <PasswordField
      register={register}
      fieldName="password"
      error={error}
    />
  );
}

describe('PasswordField Component', () => {
  it('should hide password by default', () => {
    render(<TestPasswordField />);
    const passwordInput = screen.getByLabelText('Password');
    expect(passwordInput.getAttribute('type')).toBe('password');
  });

  it('should show password when Show password is clicked', async () => {
    const user = userEvent.setup();
    render(<TestPasswordField />);
    await user.click(screen.getByRole('button', { name: 'Show password' }));
    const passwordInput = screen.getByLabelText('Password');
    expect(passwordInput.getAttribute('type')).toBe('text');
  });

  it('should hide password again when Hide password is clicked', async () => {
    const user = userEvent.setup();
    render(<TestPasswordField />);
    await user.click(screen.getByRole('button', { name: 'Show password' }));
    await user.click(screen.getByRole('button', { name: 'Hide password' }));
    const passwordInput = screen.getByLabelText('Password');
    expect(passwordInput.getAttribute('type')).toBe('password');
  });

  it('should display validation error when provided', () => {
    render(<TestPasswordField error="Password is required" />);
    expect(screen.getByRole('alert').textContent).toBe('Password is required');
  });
});