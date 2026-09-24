package com.ShopSphere.dto;

public class VerifyOtpRequest {

    private String target;
    private String otp;
    private String purpose;

    public VerifyOtpRequest() {
    }

    public VerifyOtpRequest(String target, String otp, String purpose) {
        this.target = target;
        this.otp = otp;
        this.purpose = purpose;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}
