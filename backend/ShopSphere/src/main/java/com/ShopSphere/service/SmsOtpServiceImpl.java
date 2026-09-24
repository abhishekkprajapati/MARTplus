package com.ShopSphere.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class SmsOtpServiceImpl implements SmsOtpService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${sms.provider:DEV}")
    private String smsProvider;

    @Value("${sms.dev-mode:true}")
    private boolean devMode;

    // Twilio Credentials
    @Value("${twilio.account.sid:}")
    private String twilioAccountSid;

    @Value("${twilio.auth.token:}")
    private String twilioAuthToken;

    @Value("${twilio.from.number:}")
    private String twilioFromNumber;

    // MSG91 Credentials
    @Value("${msg91.auth.key:}")
    private String msg91AuthKey;

    @Value("${msg91.template.id:}")
    private String msg91TemplateId;

    @Value("${msg91.sender.id:SHPSPH}")
    private String msg91SenderId;

    @Override
    public String getProviderName() {
        return smsProvider != null ? smsProvider.trim().toUpperCase() : "UNCONFIGURED";
    }

    @Override
    public boolean isProviderConfigured() {
        String provider = getProviderName();
        if ("TWILIO".equals(provider)) {
            return twilioAccountSid != null && !twilioAccountSid.isBlank()
                    && twilioAuthToken != null && !twilioAuthToken.isBlank()
                    && twilioFromNumber != null && !twilioFromNumber.isBlank();
        } else if ("MSG91".equals(provider)) {
            return msg91AuthKey != null && !msg91AuthKey.isBlank()
                    && msg91TemplateId != null && !msg91TemplateId.isBlank();
        }
        return false;
    }

    @Override
    public void sendSmsOtp(String normalizedPhone, String otp, String purpose) {
        String masked = maskPhoneNumber(normalizedPhone);
        String provider = getProviderName();

        // Enforce provider configuration check: do NOT pretend SMS delivery works if unconfigured
        if (!isProviderConfigured()) {
            System.err.println("[SMS PROVIDER] SMS dispatch rejected: Provider '" + provider 
                    + "' is not configured with valid credentials.");
            throw new RuntimeException("SMS service is not configured. Please contact the administrator.");
        }

        System.out.println("[SMS PROVIDER] SMS OTP request initiated for phone " + masked + " (purpose: " + purpose + ")");

        if ("TWILIO".equals(provider)) {
            sendTwilioSms(normalizedPhone, otp, purpose);
        } else if ("MSG91".equals(provider)) {
            sendMsg91Sms(normalizedPhone, otp);
        } else {
            System.err.println("[SMS PROVIDER] Unsupported SMS provider: " + provider);
            throw new RuntimeException("SMS service configuration is invalid.");
        }
    }

    private void sendTwilioSms(String toPhone, String otp, String purpose) {
        String masked = maskPhoneNumber(toPhone);
        try {
            String urlStr = "https://api.twilio.com/2010-04-01/Accounts/" + twilioAccountSid.trim() + "/Messages.json";
            URL url = URI.create(urlStr).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            String auth = twilioAccountSid.trim() + ":" + twilioAuthToken.trim();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String messageBody = "Your ShopSphere verification code is " + otp + ". Valid for 10 minutes.";
            String postData = "To=" + URLEncoder.encode(toPhone, StandardCharsets.UTF_8)
                    + "&From=" + URLEncoder.encode(twilioFromNumber.trim(), StandardCharsets.UTF_8)
                    + "&Body=" + URLEncoder.encode(messageBody, StandardCharsets.UTF_8);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(postData.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            InputStream is = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
            String responseBody = readStream(is);

            JsonNode rootNode = null;
            try {
                rootNode = objectMapper.readTree(responseBody);
            } catch (Exception ignored) {
            }

            if (responseCode == 200 || responseCode == 201) {
                String sid = rootNode != null ? rootNode.path("sid").asText("") : "";
                String status = rootNode != null ? rootNode.path("status").asText("accepted") : "accepted";
                System.out.println("[SMS PROVIDER] Twilio SMS request accepted. HTTP Status: " + responseCode 
                        + ", Message SID: " + maskSid(sid) + ", Provider Status: " + status 
                        + " for " + masked);
                return; // REQUEST_ACCEPTED
            }

            int twilioErrorCode = rootNode != null ? rootNode.path("code").asInt(0) : 0;
            String twilioErrorMsg = rootNode != null ? rootNode.path("message").asText("") : "";
            System.err.println("[SMS PROVIDER] Twilio error: HTTP " + responseCode 
                    + ", Twilio Code: " + twilioErrorCode + ", Info: " + sanitizeForLogs(twilioErrorMsg));

            if (responseCode == 401 || responseCode == 403 || twilioErrorCode == 20003) {
                throw new RuntimeException("SMS service authentication failed.");
            } else if (responseCode == 429 || twilioErrorCode == 20008) {
                throw new RuntimeException("Too many OTP requests. Please try again later.");
            } else if (twilioErrorCode == 20005) {
                throw new RuntimeException("SMS service is temporarily unavailable.");
            } else if (twilioErrorCode == 21211 || twilioErrorCode == 21214 || twilioErrorCode == 21614 || twilioErrorCode == 21408) {
                throw new RuntimeException("Please enter a valid mobile number.");
            } else if (twilioErrorCode == 21404 || twilioErrorCode == 21612 || twilioErrorCode == 21617) {
                throw new RuntimeException("SMS service configuration is invalid.");
            } else if (twilioErrorCode == 21606 || twilioErrorCode == 21608) {
                throw new RuntimeException("Destination number is not verified with SMS provider test account.");
            } else {
                throw new RuntimeException("Unable to send verification code right now. Please try again later.");
            }
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            System.err.println("[SMS PROVIDER] Twilio dispatch exception: " + e.getMessage());
            throw new RuntimeException("Unable to send verification code right now. Please try again later.");
        }
    }

    private void sendMsg91Sms(String toPhone, String otp) {
        String masked = maskPhoneNumber(toPhone);
        String msg91Phone = toPhone.startsWith("+") ? toPhone.substring(1) : toPhone;

        try {
            String urlStr = "https://control.msg91.com/api/v5/otp";
            URL url = URI.create(urlStr).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            conn.setRequestProperty("authkey", msg91AuthKey.trim());
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");

            String payload = String.format(
                    "{\"template_id\":\"%s\",\"mobile\":\"%s\",\"otp\":\"%s\"}",
                    msg91TemplateId.trim(),
                    msg91Phone,
                    otp
            );

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            InputStream is = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
            String responseBody = readStream(is);

            JsonNode rootNode = null;
            try {
                rootNode = objectMapper.readTree(responseBody);
            } catch (Exception ignored) {
            }

            String type = rootNode != null ? rootNode.path("type").asText("") : "";
            String requestId = rootNode != null ? rootNode.path("request_id").asText("") : "";
            String msgText = rootNode != null ? rootNode.path("message").asText("") : "";

            System.out.println("[SMS PROVIDER] MSG91 response HTTP: " + responseCode 
                    + ", Type: " + type + ", Request ID: " + maskSid(requestId) + " for " + masked);

            if ((responseCode == 200 || responseCode == 201) && "success".equalsIgnoreCase(type)) {
                return; // REQUEST_ACCEPTED
            }

            System.err.println("[SMS PROVIDER] MSG91 error: HTTP " + responseCode + ", Response: " + sanitizeForLogs(msgText));
            String lower = msgText.toLowerCase();

            if (responseCode == 401 || lower.contains("auth") || lower.contains("invalid key")) {
                throw new RuntimeException("SMS service authentication failed.");
            } else if (responseCode == 429 || lower.contains("limit")) {
                throw new RuntimeException("Too many OTP requests. Please try again later.");
            } else if (lower.contains("balance") || lower.contains("credit") || lower.contains("no sms left")) {
                throw new RuntimeException("SMS service is temporarily unavailable.");
            } else if (lower.contains("template") || lower.contains("sender")) {
                throw new RuntimeException("SMS service configuration is invalid.");
            } else if (lower.contains("mobile") || lower.contains("number") || lower.contains("invalid mobile")) {
                throw new RuntimeException("Please enter a valid mobile number.");
            } else {
                throw new RuntimeException("Unable to send verification code right now. Please try again later.");
            }
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            System.err.println("[SMS PROVIDER] MSG91 dispatch exception: " + e.getMessage());
            throw new RuntimeException("Unable to send verification code right now. Please try again later.");
        }
    }

    private String readStream(InputStream is) {
        if (is == null) return "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private String maskSid(String sid) {
        if (sid == null || sid.length() < 8) return "****";
        return sid.substring(0, 4) + "..." + sid.substring(sid.length() - 4);
    }

    private String sanitizeForLogs(String msg) {
        if (msg == null) return "";
        return msg.replaceAll("(?i)(auth|key|token|password|secret)[=:]\\s*\\S+", "$1=****");
    }
}
