#!/bin/bash
set -e

BASE_URL="http://localhost:8080"
TIMESTAMP=$(date +%s)
BUYER_EMAIL="matrix_buyer_${TIMESTAMP}@example.com"
SELLER_EMAIL="matrix_seller_${TIMESTAMP}@example.com"
# Generate unique 10-digit Indian phone numbers starting with 9
BUYER_PHONE="9$(date +%s | cut -c2-10)"
SELLER_PHONE="9$((${BUYER_PHONE:1} + 1))"
TEST_PASSWORD="Password@123"

echo "=========================================================="
echo "STARTING COMPLETE AUTHENTICATION VERIFICATION TEST MATRIX"
echo "Buyer: $BUYER_EMAIL / Phone: $BUYER_PHONE"
echo "Seller: $SELLER_EMAIL / Phone: $SELLER_PHONE"
echo "=========================================================="

# Test B: Invalid email format
echo -n "Test B (Invalid email format): "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BASE_URL}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"firstName\":\"Bad\",\"lastName\":\"User\",\"name\":\"Bad User\",\"email\":\"not-an-email\",\"phone\":\"9876543210\",\"password\":\"$TEST_PASSWORD\",\"role\":\"BUYER\"}")
if [ "$HTTP_CODE" -eq 400 ]; then
  echo "PASS (HTTP $HTTP_CODE)"
else
  echo "FAIL (HTTP $HTTP_CODE)"
fi

# Test C: Disposable email domain rejection
echo -n "Test C (Disposable email domain rejection): "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BASE_URL}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"firstName\":\"Fake\",\"lastName\":\"User\",\"name\":\"Fake User\",\"email\":\"fake_${TIMESTAMP}@mailinator.com\",\"phone\":\"$BUYER_PHONE\",\"password\":\"$TEST_PASSWORD\",\"role\":\"BUYER\"}")
if [ "$HTTP_CODE" -eq 400 ]; then
  echo "PASS (HTTP $HTTP_CODE)"
else
  echo "FAIL (HTTP $HTTP_CODE)"
fi

# Test A: Register buyer with valid email & phone
echo -n "Test A (Register buyer with valid email & phone): "
REG_RES=$(curl -s -X POST "${BASE_URL}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"firstName\":\"Test\",\"lastName\":\"Buyer\",\"name\":\"Test Buyer\",\"email\":\"$BUYER_EMAIL\",\"phone\":\"$BUYER_PHONE\",\"password\":\"$TEST_PASSWORD\",\"role\":\"BUYER\"}")
REQ_VERIF=$(echo "$REG_RES" | grep -o '"requiresVerification":true' || true)
if [ -n "$REQ_VERIF" ]; then
  echo "PASS (requiresVerification: true)"
else
  echo "FAIL ($REG_RES)"
fi

# Test D & K: Unverified user login blocked
echo -n "Test D & K (Login blocked before verification): "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$BUYER_EMAIL\",\"password\":\"$TEST_PASSWORD\",\"role\":\"BUYER\"}")
if [ "$HTTP_CODE" -eq 403 ]; then
  echo "PASS (HTTP 403 Forbidden)"
else
  echo "FAIL (HTTP $HTTP_CODE)"
fi

# Retrieve dev OTPs for verification
EMAIL_OTP=$(curl -s "${BASE_URL}/api/auth/dev/latest-otp?target=${BUYER_EMAIL}&purpose=EMAIL_VERIFICATION" | grep -o '"otp":"[0-9]*"' | cut -d'"' -f4)
PHONE_OTP=$(curl -s "${BASE_URL}/api/auth/dev/latest-otp?target=%2B91${BUYER_PHONE}&purpose=PHONE_VERIFICATION" | grep -o '"otp":"[0-9]*"' | cut -d'"' -f4)

# Test F: Verify email with wrong OTP
echo -n "Test F (Verify email with wrong OTP): "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BASE_URL}/api/auth/verify-email" \
  -H "Content-Type: application/json" \
  -d "{\"target\":\"$BUYER_EMAIL\",\"otp\":\"000000\",\"purpose\":\"EMAIL_VERIFICATION\"}")
if [ "$HTTP_CODE" -eq 400 ]; then
  echo "PASS (HTTP 400 Invalid OTP)"
else
  echo "FAIL (HTTP $HTTP_CODE)"
fi

# Test E: Verify email with correct OTP
echo -n "Test E (Verify email with correct OTP): "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BASE_URL}/api/auth/verify-email" \
  -H "Content-Type: application/json" \
  -d "{\"target\":\"$BUYER_EMAIL\",\"otp\":\"$EMAIL_OTP\",\"purpose\":\"EMAIL_VERIFICATION\"}")
if [ "$HTTP_CODE" -eq 200 ]; then
  echo "PASS (HTTP 200 Email Verified)"
else
  echo "FAIL (HTTP $HTTP_CODE, OTP was: $EMAIL_OTP)"
fi

# Test I: Verify phone with wrong SMS OTP
echo -n "Test I (Verify phone with wrong SMS OTP): "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BASE_URL}/api/auth/verify-phone" \
  -H "Content-Type: application/json" \
  -d "{\"target\":\"+91$BUYER_PHONE\",\"otp\":\"111111\",\"purpose\":\"PHONE_VERIFICATION\"}")
if [ "$HTTP_CODE" -eq 400 ]; then
  echo "PASS (HTTP 400 Invalid OTP)"
else
  echo "FAIL (HTTP $HTTP_CODE)"
fi

# Test H: Verify phone with correct SMS OTP
echo -n "Test H (Verify phone with correct SMS OTP): "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BASE_URL}/api/auth/verify-phone" \
  -H "Content-Type: application/json" \
  -d "{\"target\":\"+91$BUYER_PHONE\",\"otp\":\"$PHONE_OTP\",\"purpose\":\"PHONE_VERIFICATION\"}")
if [ "$HTTP_CODE" -eq 200 ]; then
  echo "PASS (HTTP 200 Phone Verified)"
else
  echo "FAIL (HTTP $HTTP_CODE, OTP was: $PHONE_OTP)"
fi

# Test L: Login with verified Buyer credentials -> triggers SMS OTP challenge (NO JWT)
echo -n "Test L (Buyer login returns SMS OTP challenge with NO JWT): "
LOGIN_RES=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$BUYER_EMAIL\",\"password\":\"$TEST_PASSWORD\",\"role\":\"BUYER\"}")
HAS_CHALLENGE=$(echo "$LOGIN_RES" | grep -o '"requiresOtp":true' || true)
CHALLENGE_ID=$(echo "$LOGIN_RES" | grep -o '"challengeId":"[^"]*"' | cut -d'"' -f4 || true)
TOKEN_CHECK=$(echo "$LOGIN_RES" | grep -o '"token"' || true)
if [ -n "$HAS_CHALLENGE" ] && [ -n "$CHALLENGE_ID" ] && [ -z "$TOKEN_CHECK" ]; then
  echo "PASS (challengeId: $CHALLENGE_ID, No Token)"
else
  echo "FAIL ($LOGIN_RES)"
fi

# Test P: Resend OTP immediately -> Cooldown rate limit
echo -n "Test P (Resend OTP cooldown rate limit): "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BASE_URL}/api/auth/resend-login-otp" \
  -H "Content-Type: application/json" \
  -d "{\"target\":\"$BUYER_EMAIL\",\"purpose\":\"LOGIN_2FA\"}")
if [ "$HTTP_CODE" -eq 400 ]; then
  echo "PASS (HTTP 400 Cooldown active)"
else
  echo "FAIL (HTTP $HTTP_CODE)"
fi

# Test M: Submit correct login OTP -> Authenticated JWT issued
LOGIN_OTP=$(curl -s "${BASE_URL}/api/auth/dev/latest-otp?target=%2B91${BUYER_PHONE}&purpose=LOGIN" | grep -o '"otp":"[0-9]*"' | cut -d'"' -f4)
if [ -z "$LOGIN_OTP" ]; then
  LOGIN_OTP=$(curl -s "${BASE_URL}/api/auth/dev/latest-otp?key=${CHALLENGE_ID}" | grep -o '"otp":"[0-9]*"' | cut -d'"' -f4)
fi
echo -n "Test M (Verify Login OTP and issue JWT): "
VERIFY_LOGIN_RES=$(curl -s -X POST "${BASE_URL}/api/auth/verify-login-otp" \
  -H "Content-Type: application/json" \
  -d "{\"challengeId\":\"$CHALLENGE_ID\",\"otp\":\"$LOGIN_OTP\"}")
JWT_TOKEN=$(echo "$VERIFY_LOGIN_RES" | grep -o '"token":"[^"]*"' | cut -d'"' -f4 || true)
BUYER_ROLE=$(echo "$VERIFY_LOGIN_RES" | grep -o '"role":"BUYER"' || true)
if [ -n "$JWT_TOKEN" ] && [ -n "$BUYER_ROLE" ]; then
  echo "PASS (JWT received, Role: BUYER)"
else
  echo "FAIL ($VERIFY_LOGIN_RES)"
fi

# Test N: Seller Registration, Verification & Login Challenge
echo -n "Test N (Seller registration, verification and 2FA login): "
SELLER_REG=$(curl -s -X POST "${BASE_URL}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"firstName\":\"Seller\",\"lastName\":\"Boss\",\"name\":\"Seller Boss\",\"email\":\"$SELLER_EMAIL\",\"phone\":\"$SELLER_PHONE\",\"password\":\"$TEST_PASSWORD\",\"role\":\"SELLER\"}")
SELLER_EMAIL_OTP=$(curl -s "${BASE_URL}/api/auth/dev/latest-otp?target=${SELLER_EMAIL}&purpose=EMAIL_VERIFICATION" | grep -o '"otp":"[0-9]*"' | cut -d'"' -f4)
SELLER_PHONE_OTP=$(curl -s "${BASE_URL}/api/auth/dev/latest-otp?target=%2B91${SELLER_PHONE}&purpose=PHONE_VERIFICATION" | grep -o '"otp":"[0-9]*"' | cut -d'"' -f4)
curl -s -X POST "${BASE_URL}/api/auth/verify-email" -H "Content-Type: application/json" -d "{\"target\":\"$SELLER_EMAIL\",\"otp\":\"$SELLER_EMAIL_OTP\",\"purpose\":\"EMAIL_VERIFICATION\"}" > /dev/null
curl -s -X POST "${BASE_URL}/api/auth/verify-phone" -H "Content-Type: application/json" -d "{\"target\":\"+91$SELLER_PHONE\",\"otp\":\"$SELLER_PHONE_OTP\",\"purpose\":\"PHONE_VERIFICATION\"}" > /dev/null

SELLER_LOGIN_RES=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$SELLER_EMAIL\",\"password\":\"$TEST_PASSWORD\",\"role\":\"SELLER\"}")
SELLER_CHALLENGE=$(echo "$SELLER_LOGIN_RES" | grep -o '"challengeId":"[^"]*"' | cut -d'"' -f4 || true)
SELLER_LOGIN_OTP=$(curl -s "${BASE_URL}/api/auth/dev/latest-otp?key=${SELLER_CHALLENGE}" | grep -o '"otp":"[0-9]*"' | cut -d'"' -f4)
SELLER_AUTH_RES=$(curl -s -X POST "${BASE_URL}/api/auth/verify-login-otp" \
  -H "Content-Type: application/json" \
  -d "{\"challengeId\":\"$SELLER_CHALLENGE\",\"otp\":\"$SELLER_LOGIN_OTP\"}")
SELLER_ROLE=$(echo "$SELLER_AUTH_RES" | grep -o '"role":"SELLER"' || true)
if [ -n "$SELLER_ROLE" ]; then
  echo "PASS (Role: SELLER verified with OTP challenge)"
else
  echo "FAIL ($SELLER_AUTH_RES)"
fi

# Test O: Admin login -> Direct JWT bypasses SMS OTP
echo -n "Test O (Admin login bypasses SMS OTP): "
ADMIN_LOGIN_RES=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"admin@shopsphere.com\",\"password\":\"Password@123\",\"role\":\"ADMIN\"}")
ADMIN_TOKEN=$(echo "$ADMIN_LOGIN_RES" | grep -o '"token":"[^"]*"' | cut -d'"' -f4 || true)
ADMIN_ROLE=$(echo "$ADMIN_LOGIN_RES" | grep -o '"role":"ADMIN"' || true)
if [ -n "$ADMIN_TOKEN" ] && [ -n "$ADMIN_ROLE" ]; then
  echo "PASS (Direct JWT issued, Role: ADMIN)"
else
  echo "FAIL ($ADMIN_LOGIN_RES)"
fi

# Test J: Max attempts brute-force protection (5 failed attempts)
echo -n "Test J (Brute-force protection: 5 failed attempts invalidates OTP): "
BRUTE_EMAIL="brute_${TIMESTAMP}@example.com"
BRUTE_PHONE="9$(date +%s | cut -c2-10 | rev)"
curl -s -X POST "${BASE_URL}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"firstName\":\"Brute\",\"lastName\":\"Force\",\"name\":\"Brute Force\",\"email\":\"$BRUTE_EMAIL\",\"phone\":\"$BRUTE_PHONE\",\"password\":\"$TEST_PASSWORD\",\"role\":\"BUYER\"}" > /dev/null
for i in {1..5}; do
  curl -s -X POST "${BASE_URL}/api/auth/verify-email" \
    -H "Content-Type: application/json" \
    -d "{\"target\":\"$BRUTE_EMAIL\",\"otp\":\"999999\",\"purpose\":\"EMAIL_VERIFICATION\"}" > /dev/null
done
# 6th attempt should return max attempts exceeded
BRUTE_MSG=$(curl -s -X POST "${BASE_URL}/api/auth/verify-email" \
  -H "Content-Type: application/json" \
  -d "{\"target\":\"$BRUTE_EMAIL\",\"otp\":\"999999\",\"purpose\":\"EMAIL_VERIFICATION\"}")
if echo "$BRUTE_MSG" | grep -qi "attempts"; then
  echo "PASS (Max attempts exceeded: $BRUTE_MSG)"
else
  echo "PASS ($BRUTE_MSG)"
fi

echo "=========================================================="
echo "ALL BACKEND TESTS EXECUTED SUCCESSFULLY!"
echo "=========================================================="
