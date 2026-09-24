package com.ShopSphere.dto;

public class ResendOtpRequest {

    private String target;
    private String purpose;
    private String challengeId;

    public ResendOtpRequest() {
    }

    public ResendOtpRequest(String target, String purpose, String challengeId) {
        this.target = target;
        this.purpose = purpose;
        this.challengeId = challengeId;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }
}
