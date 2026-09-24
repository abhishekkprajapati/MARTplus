package com.ShopSphere.dto;

public class LoginChallengeResponse {

    private boolean requiresOtp;
    private String challengeId;
    private String maskedPhone;
    private String message;

    public LoginChallengeResponse() {
    }

    public LoginChallengeResponse(boolean requiresOtp, String challengeId, String maskedPhone, String message) {
        this.requiresOtp = requiresOtp;
        this.challengeId = challengeId;
        this.maskedPhone = maskedPhone;
        this.message = message;
    }

    public boolean isRequiresOtp() {
        return requiresOtp;
    }

    public void setRequiresOtp(boolean requiresOtp) {
        this.requiresOtp = requiresOtp;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public String getMaskedPhone() {
        return maskedPhone;
    }

    public void setMaskedPhone(String maskedPhone) {
        this.maskedPhone = maskedPhone;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
