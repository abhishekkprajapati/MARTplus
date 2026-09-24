package com.ShopSphere.service;

public interface SmsOtpService {

    void sendSmsOtp(String normalizedPhone, String otp, String purpose);

    boolean isProviderConfigured();

    String getProviderName();

    /**
     * Normalizes an Indian phone number to international format: +91XXXXXXXXXX.
     * Supports user inputs: 9876543210, 09876543210, 919876543210, +919876543210.
     * Also safely preserves valid international numbers in E.164 format.
     */
    default String normalizeIndianPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new RuntimeException("Phone number is required.");
        }

        String trimmed = phone.trim();

        // Handle existing international format (+...)
        if (trimmed.startsWith("+")) {
            String digits = trimmed.substring(1).replaceAll("[^0-9]", "");
            if (trimmed.startsWith("+91")) {
                if (digits.length() == 12) {
                    char first = digits.charAt(2);
                    if (first >= '6' && first <= '9') {
                        return "+91" + digits.substring(2);
                    }
                }
                throw new RuntimeException("Please enter a valid 10-digit Indian mobile number.");
            }
            if (digits.length() >= 7 && digits.length() <= 15) {
                return "+" + digits;
            }
            throw new RuntimeException("Please enter a valid mobile number.");
        }

        String digitsOnly = trimmed.replaceAll("[^0-9]", "");

        if (digitsOnly.length() == 10) {
            char first = digitsOnly.charAt(0);
            if (first < '6' || first > '9') {
                throw new RuntimeException("Please enter a valid 10-digit Indian mobile number starting with 6, 7, 8, or 9.");
            }
            return "+91" + digitsOnly;
        } else if (digitsOnly.length() == 11 && digitsOnly.startsWith("0")) {
            String tenDigits = digitsOnly.substring(1);
            char first = tenDigits.charAt(0);
            if (first < '6' || first > '9') {
                throw new RuntimeException("Please enter a valid 10-digit Indian mobile number.");
            }
            return "+91" + tenDigits;
        } else if (digitsOnly.length() == 12 && digitsOnly.startsWith("91")) {
            String tenDigits = digitsOnly.substring(2);
            char first = tenDigits.charAt(0);
            if (first < '6' || first > '9') {
                throw new RuntimeException("Please enter a valid 10-digit Indian mobile number.");
            }
            return "+91" + tenDigits;
        } else {
            throw new RuntimeException("Please enter a valid 10-digit Indian mobile number.");
        }
    }

    default String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 8) {
            return "******";
        }
        if (phone.startsWith("+91") && phone.length() == 13) {
            return "+91******" + phone.substring(9);
        }
        return phone.substring(0, 3) + "******" + phone.substring(phone.length() - 2);
    }
}

