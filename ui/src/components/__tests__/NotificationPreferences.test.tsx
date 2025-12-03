/// <reference types="@testing-library/jest-dom" />
import React from 'react';
import { describe, it, expect, beforeEach, jest } from '@jest/globals';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { NotificationPreferences } from '../NotificationPreferences';
import { orderService } from '../../services/orderService';
import type { NotificationPreferences as NotificationPreferencesType } from '../../types';

jest.mock('../../services/orderService');

describe('NotificationPreferences', () => {
  const mockPreferences: NotificationPreferencesType = {
    emailEnabled: true,
    smsEnabled: false,
    phoneNumber: '',
    notificationFrequency: 'ALL',
  };

  const mockUpdatedPreferences: NotificationPreferencesType = {
    emailEnabled: false,
    smsEnabled: true,
    phoneNumber: '+1234567890',
    notificationFrequency: 'CRITICAL_ONLY',
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render loading state initially', async () => {
    (orderService.getNotificationPreferences as jest.MockedFunction<typeof orderService.getNotificationPreferences>).mockImplementation(
      () => new Promise((resolve) => setTimeout(() => resolve(mockPreferences), 100))
    );

    render(<NotificationPreferences userId="user-123" />);
    
    expect(screen.getByText('Loading preferences...')).toBeInTheDocument();
    
    await waitFor(() => {
      expect(screen.queryByText('Loading preferences...')).not.toBeInTheDocument();
    });
  });

  it('should load and display preferences', async () => {
    (orderService.getNotificationPreferences as jest.MockedFunction<typeof orderService.getNotificationPreferences>).mockResolvedValue(mockPreferences);

    render(<NotificationPreferences userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByText('Notification Preferences')).toBeInTheDocument();
      expect(screen.getByLabelText(/Email Notifications/i)).toBeChecked();
      expect(screen.getByLabelText(/SMS Notifications/i)).not.toBeChecked();
    });
  });

  it('should display error message when loading fails', async () => {
    const error = new Error('Failed to load');
    (orderService.getNotificationPreferences as jest.MockedFunction<typeof orderService.getNotificationPreferences>).mockRejectedValue(error);

    render(<NotificationPreferences userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByText('Failed to load preferences')).toBeInTheDocument();
    });
  });

  it('should toggle email notifications', async () => {
    (orderService.getNotificationPreferences as jest.MockedFunction<typeof orderService.getNotificationPreferences>).mockResolvedValue(mockPreferences);

    render(<NotificationPreferences userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByLabelText(/Email Notifications/i)).toBeChecked();
    });

    const emailCheckbox = screen.getByLabelText(/Email Notifications/i);
    fireEvent.click(emailCheckbox);

    expect(emailCheckbox).not.toBeChecked();
  });

  it('should toggle SMS notifications', async () => {
    (orderService.getNotificationPreferences as jest.MockedFunction<typeof orderService.getNotificationPreferences>).mockResolvedValue(mockPreferences);

    render(<NotificationPreferences userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByLabelText(/SMS Notifications/i)).not.toBeChecked();
    });

    const smsCheckbox = screen.getByLabelText(/SMS Notifications/i);
    fireEvent.click(smsCheckbox);

    expect(smsCheckbox).toBeChecked();
  });

  it('should show phone number input when SMS is enabled', async () => {
    const prefsWithSMS: NotificationPreferencesType = {
      ...mockPreferences,
      smsEnabled: true,
    };
    (orderService.getNotificationPreferences as jest.MockedFunction<typeof orderService.getNotificationPreferences>).mockResolvedValue(prefsWithSMS);

    render(<NotificationPreferences userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByLabelText(/Phone Number/i)).toBeInTheDocument();
    });
  });

  it('should update phone number', async () => {
    const prefsWithSMS: NotificationPreferencesType = {
      ...mockPreferences,
      smsEnabled: true,
    };
    (orderService.getNotificationPreferences as jest.MockedFunction<typeof orderService.getNotificationPreferences>).mockResolvedValue(prefsWithSMS);

    render(<NotificationPreferences userId="user-123" />);

    await waitFor(() => {
      const phoneInput = screen.getByLabelText(/Phone Number/i);
      fireEvent.change(phoneInput, { target: { value: '+1234567890' } });
      expect(phoneInput).toHaveValue('+1234567890');
    });
  });

  it('should update notification frequency', async () => {
    (orderService.getNotificationPreferences as jest.MockedFunction<typeof orderService.getNotificationPreferences>).mockResolvedValue(mockPreferences);

    render(<NotificationPreferences userId="user-123" />);

    await waitFor(() => {
      const frequencySelect = screen.getByLabelText(/Notification Frequency/i);
      fireEvent.change(frequencySelect, { target: { value: 'CRITICAL_ONLY' } });
      expect((frequencySelect as HTMLSelectElement).value).toBe('CRITICAL_ONLY');
    });
  });

  it('should save preferences successfully', async () => {
    (orderService.getNotificationPreferences as jest.MockedFunction<typeof orderService.getNotificationPreferences>).mockResolvedValue(mockPreferences);
    (orderService.updateNotificationPreferences as jest.MockedFunction<typeof orderService.updateNotificationPreferences>).mockResolvedValue(mockUpdatedPreferences);

    render(<NotificationPreferences userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByText('Save Preferences')).toBeInTheDocument();
    });

    const saveButton = screen.getByText('Save Preferences');
    fireEvent.click(saveButton);

    await waitFor(() => {
      expect(orderService.updateNotificationPreferences).toHaveBeenCalledWith('user-123', mockPreferences);
      expect(screen.getByText('Preferences saved successfully!')).toBeInTheDocument();
    });
  });

  it('should display error message when save fails', async () => {
    (orderService.getNotificationPreferences as jest.MockedFunction<typeof orderService.getNotificationPreferences>).mockResolvedValue(mockPreferences);
    const error = new Error('Failed to save');
    (orderService.updateNotificationPreferences as jest.MockedFunction<typeof orderService.updateNotificationPreferences>).mockRejectedValue(error);

    render(<NotificationPreferences userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByText('Save Preferences')).toBeInTheDocument();
    });

    const saveButton = screen.getByText('Save Preferences');
    fireEvent.click(saveButton);

    await waitFor(() => {
      expect(screen.getByText('Failed to save preferences')).toBeInTheDocument();
    });
  });

  it('should disable save button while saving', async () => {
    (orderService.getNotificationPreferences as jest.MockedFunction<typeof orderService.getNotificationPreferences>).mockResolvedValue(mockPreferences);
    (orderService.updateNotificationPreferences as jest.MockedFunction<typeof orderService.updateNotificationPreferences>).mockImplementation(
      () => new Promise((resolve) => setTimeout(() => resolve(mockUpdatedPreferences), 100))
    );

    render(<NotificationPreferences userId="user-123" />);

    await waitFor(() => {
      expect(screen.getByText('Save Preferences')).toBeInTheDocument();
    });

    const saveButton = screen.getByText('Save Preferences');
    fireEvent.click(saveButton);

    expect((saveButton as HTMLButtonElement).disabled).toBe(true);
    expect(screen.getByText('Saving...')).toBeInTheDocument();

    await waitFor(() => {
      expect((saveButton as HTMLButtonElement).disabled).toBe(false);
    });
  });

  it('should reload preferences when userId changes', async () => {
    (orderService.getNotificationPreferences as jest.MockedFunction<typeof orderService.getNotificationPreferences>).mockResolvedValue(mockPreferences);

    const { rerender } = render(<NotificationPreferences userId="user-123" />);

    await waitFor(() => {
      expect(orderService.getNotificationPreferences).toHaveBeenCalledWith('user-123');
    });

    jest.clearAllMocks();
    (orderService.getNotificationPreferences as jest.MockedFunction<typeof orderService.getNotificationPreferences>).mockResolvedValue(mockPreferences);

    rerender(<NotificationPreferences userId="user-456" />);

    await waitFor(() => {
      expect(orderService.getNotificationPreferences).toHaveBeenCalledWith('user-456');
    });
  });
});
