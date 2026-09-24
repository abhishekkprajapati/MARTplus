package com.ShopSphere.dto;

public class VerifyLoginOtpRequest {

    private String challengeId;
    private String otp;

    public VerifyLoginOtpRequest() {
    }

    public VerifyLoginOtpRequest(String challengeId, String otp) {
        this.challengeId = challengeId;
        this.otp = otp;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
