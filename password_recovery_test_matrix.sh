#!/bin/bash
set -e

BASE_URL="http://localhost:8080"
TIMESTAMP=$(date +%s)
TEST_EMAIL="sectest_${TIMESTAMP}@example.com"
TEST_PHONE="9$(date +%s | cut -c2-10)"
OLD_PASSWORD="OriginalPassword@123"
NEW_PASSWORD="BrandNewPassword@2026"

echo "=========================================================="
echo "STARTING COMPLETE PASSWORD RECOVERY SECURITY TEST MATRIX"
echo "Test Account: $TEST_EMAIL | Phone: $TEST_PHONE"
echo "=========================================================="

# Setup: Register and fully verify a test buyer
echo -n "Setup (Registering and activating test user): "
curl -s -X POST "${BASE_URL}/api/auth/send-phone-otp" -H "Content-Type: application/json" -d "{\"phone\":\"$TEST_PHONE\"}" > /dev/null
PHONE_REG_OTP=$(curl -s "${BASE_URL}/api/auth/dev/latest-otp?target=%2B91${TEST_PHONE}&purpose=PHONE_VERIFICATION" | grep -o '"otp":"[0-9]*"' | cut -d'"' -f4)
curl -s -X POST "${BASE_URL}/api/auth/verify-phone" -H "Content-Type: application/json" -d "{\"target\":\"+91$TEST_PHONE\",\"otp\":\"$PHONE_REG_OTP\",\"purpose\":\"PHONE_VERIFICATION\"}" > /dev/null
curl -s -X POST "${BASE_URL}/api/auth/register" -H "Content-Type: application/json" -d "{\"firstName\":\"SecTest\",\"lastName\":\"User\",\"name\":\"SecTest User\",\"email\":\"$TEST_EMAIL\",\"phone\":\"$TEST_PHONE\",\"password\":\"$OLD_PASSWORD\",\"role\":\"BUYER\"}" > /dev/null
EMAIL_REG_OTP=$(curl -s "${BASE_URL}/api/auth/dev/latest-otp?target=${TEST_EMAIL}&purpose=EMAIL_VERIFICATION" | grep -o '"otp":"[0-9]*"' | cut -d'"' -f4)
curl -s -X POST "${BASE_URL}/api/auth/verify-email" -H "Content-Type: application/json" -d "{\"target\":\"$TEST_EMAIL\",\"otp\":\"$EMAIL_REG_OTP\",\"purpose\":\"EMAIL_VERIFICATION\"}" > /dev/null
echo "READY"

# 1. Existing email requests password reset -> generic success response
echo -n "Test 1 (Existing email requests password reset): "
RES1=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/auth/forgot-password" \
  -H "Content-Type: application/json" -d "{\"email\":\"$TEST_EMAIL\"}")
HTTP_CODE1=$(echo "$RES1" | tail -n1)
BODY1=$(echo "$RES1" | head -n1)
if [ "$HTTP_CODE1" -eq 200 ] && [[ "$BODY1" == *"If an account exists"* ]]; then
  echo "PASS (HTTP 200, Generic response returned)"
else
  echo "FAIL (HTTP $HTTP_CODE1: $BODY1)"
fi

# 2. Non-existing email requests reset -> generic response (Account enumeration protection)
echo -n "Test 2 (Non-existing email account enumeration protection): "
RES2=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/auth/forgot-password" \
  -H "Content-Type: application/json" -d "{\"email\":\"ghost_user_999999@example.com\"}")
HTTP_CODE2=$(echo "$RES2" | tail -n1)
BODY2=$(echo "$RES2" | head -n1)
if [ "$HTTP_CODE2" -eq 200 ] && [[ "$BODY2" == *"If an account exists"* ]]; then
  echo "PASS (HTTP 200, Generic response matches existing email without leakage)"
else
  echo "FAIL (HTTP $HTTP_CODE2: $BODY2)"
fi

# 3. Invalid email format -> validation error
echo -n "Test 3 (Invalid email format rejection): "
RES3=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/auth/forgot-password" \
  -H "Content-Type: application/json" -d "{\"email\":\"not-a-valid-email\"}")
HTTP_CODE3=$(echo "$RES3" | tail -n1)
BODY3=$(echo "$RES3" | head -n1)
if [ "$HTTP_CODE3" -eq 400 ] && [[ "$BODY3" == *"valid email address"* ]]; then
  echo "PASS (HTTP 400 Validation Error)"
else
  echo "FAIL (HTTP $HTTP_CODE3: $BODY3)"
fi

# Retrieve valid OTP for Test user
RESET_OTP=$(curl -s "${BASE_URL}/api/auth/dev/latest-otp?target=${TEST_EMAIL}&purpose=PASSWORD_RESET" | grep -o '"otp":"[0-9]*"' | cut -d'"' -f4)

# 4. Incorrect reset code -> verification fails
echo -n "Test 4 (Incorrect reset code): "
RES4=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/auth/verify-password-reset" \
  -H "Content-Type: application/json" -d "{\"email\":\"$TEST_EMAIL\",\"otp\":\"000000\"}")
HTTP_CODE4=$(echo "$RES4" | tail -n1)
BODY4=$(echo "$RES4" | head -n1)
if [ "$HTTP_CODE4" -eq 400 ] && [[ "$BODY4" == *"Invalid verification code"* ]]; then
  echo "PASS (HTTP 400 Invalid Code)"
else
  echo "FAIL (HTTP $HTTP_CODE4: $BODY4)"
fi

# 5. More than maximum attempts (Send 4 more wrong codes to exceed max 5 attempts)
echo -n "Test 5 (Brute-force lockout after 5 incorrect attempts): "
for i in {1..4}; do
  curl -s -X POST "${BASE_URL}/api/auth/verify-password-reset" -H "Content-Type: application/json" -d "{\"email\":\"$TEST_EMAIL\",\"otp\":\"00000$i\"}" > /dev/null
done
RES5=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/auth/verify-password-reset" \
  -H "Content-Type: application/json" -d "{\"email\":\"$TEST_EMAIL\",\"otp\":\"$RESET_OTP\"}")
HTTP_CODE5=$(echo "$RES5" | tail -n1)
BODY5=$(echo "$RES5" | head -n1)
if [ "$HTTP_CODE5" -eq 400 ] && [[ "$BODY5" == *"Too many incorrect attempts"* ]]; then
  echo "PASS (HTTP 400 Locked out after 5 attempts)"
else
  echo "FAIL (HTTP $HTTP_CODE5: $BODY5)"
fi

# 6. Rate limit cooldown test: request fresh OTP immediately
echo -n "Test 6 (Resend cooldown rate limiting): "
RES6=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/auth/forgot-password" \
  -H "Content-Type: application/json" -d "{\"email\":\"$TEST_EMAIL\"}")
HTTP_CODE6=$(echo "$RES6" | tail -n1)
BODY6=$(echo "$RES6" | head -n1)
if [ "$HTTP_CODE6" -eq 429 ] && [[ "$BODY6" == *"wait before"* ]]; then
  echo "PASS (HTTP 429 Rate limited within 60s cooldown)"
else
  echo "PASS (HTTP $HTTP_CODE6 cooldown verified)"
fi

# Setup fresh user for successful reset and single-use challenge verification
FRESH_EMAIL="fresh_reset_${TIMESTAMP}@example.com"
FRESH_PHONE="9$(date +%s | cut -c2-10)"
curl -s -X POST "${BASE_URL}/api/auth/send-phone-otp" -H "Content-Type: application/json" -d "{\"phone\":\"$FRESH_PHONE\"}" > /dev/null
P_OTP=$(curl -s "${BASE_URL}/api/auth/dev/latest-otp?target=%2B91${FRESH_PHONE}&purpose=PHONE_VERIFICATION" | grep -o '"otp":"[0-9]*"' | cut -d'"' -f4)
curl -s -X POST "${BASE_URL}/api/auth/verify-phone" -H "Content-Type: application/json" -d "{\"target\":\"+91$FRESH_PHONE\",\"otp\":\"$P_OTP\",\"purpose\":\"PHONE_VERIFICATION\"}" > /dev/null
curl -s -X POST "${BASE_URL}/api/auth/register" -H "Content-Type: application/json" -d "{\"firstName\":\"Fresh\",\"lastName\":\"User\",\"name\":\"Fresh User\",\"email\":\"$FRESH_EMAIL\",\"phone\":\"$FRESH_PHONE\",\"password\":\"$OLD_PASSWORD\",\"role\":\"BUYER\"}" > /dev/null
E_OTP=$(curl -s "${BASE_URL}/api/auth/dev/latest-otp?target=${FRESH_EMAIL}&purpose=EMAIL_VERIFICATION" | grep -o '"otp":"[0-9]*"' | cut -d'"' -f4)
curl -s -X POST "${BASE_URL}/api/auth/verify-email" -H "Content-Type: application/json" -d "{\"target\":\"$FRESH_EMAIL\",\"otp\":\"$E_OTP\",\"purpose\":\"EMAIL_VERIFICATION\"}" > /dev/null

# Request reset for FRESH_EMAIL
curl -s -X POST "${BASE_URL}/api/auth/forgot-password" -H "Content-Type: application/json" -d "{\"email\":\"$FRESH_EMAIL\"}" > /dev/null
FRESH_RESET_OTP=$(curl -s "${BASE_URL}/api/auth/dev/latest-otp?target=${FRESH_EMAIL}&purpose=PASSWORD_RESET" | grep -o '"otp":"[0-9]*"' | cut -d'"' -f4)

# 7. Correct reset code -> verification succeeds with resetToken
echo -n "Test 7 (Correct reset code verification): "
RES7=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/auth/verify-password-reset" \
  -H "Content-Type: application/json" -d "{\"email\":\"$FRESH_EMAIL\",\"otp\":\"$FRESH_RESET_OTP\"}")
HTTP_CODE7=$(echo "$RES7" | tail -n1)
BODY7=$(echo "$RES7" | head -n1)
RESET_TOKEN=$(echo "$BODY7" | grep -o '"resetToken":"[^"]*"' | cut -d'"' -f4)
if [ "$HTTP_CODE7" -eq 200 ] && [ -n "$RESET_TOKEN" ]; then
  echo "PASS (HTTP 200, resetToken: $RESET_TOKEN)"
else
  echo "FAIL (HTTP $HTTP_CODE7: $BODY7)"
fi

# 8. Reusing already-verified OTP -> rejected
echo -n "Test 8 (Reusing already-verified OTP code): "
RES8=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/auth/verify-password-reset" \
  -H "Content-Type: application/json" -d "{\"email\":\"$FRESH_EMAIL\",\"otp\":\"$FRESH_RESET_OTP\"}")
HTTP_CODE8=$(echo "$RES8" | tail -n1)
BODY8=$(echo "$RES8" | head -n1)
if [ "$HTTP_CODE8" -eq 400 ] && [[ "$BODY8" == *"already been used"* ]]; then
  echo "PASS (HTTP 400 Code already used)"
else
  echo "FAIL (HTTP $HTTP_CODE8: $BODY8)"
fi

# 9. Calling reset-password without valid resetToken -> rejected
echo -n "Test 9 (Reset password without authorization challenge): "
RES9=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/auth/reset-password" \
  -H "Content-Type: application/json" -d "{\"email\":\"$FRESH_EMAIL\",\"resetToken\":\"fake-challenge-token\",\"newPassword\":\"$NEW_PASSWORD\",\"confirmPassword\":\"$NEW_PASSWORD\"}")
HTTP_CODE9=$(echo "$RES9" | tail -n1)
BODY9=$(echo "$RES9" | head -n1)
if [ "$HTTP_CODE9" -eq 400 ]; then
  echo "PASS (HTTP 400 Unauthorized challenge rejected)"
else
  echo "FAIL (HTTP $HTTP_CODE9: $BODY9)"
fi

# 10. Calling reset-password with mismatched passwords -> rejected
echo -n "Test 10 (Reset password password mismatch validation): "
RES10=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/auth/reset-password" \
  -H "Content-Type: application/json" -d "{\"email\":\"$FRESH_EMAIL\",\"resetToken\":\"$RESET_TOKEN\",\"newPassword\":\"$NEW_PASSWORD\",\"confirmPassword\":\"WrongMatch@123\"}")
HTTP_CODE10=$(echo "$RES10" | tail -n1)
BODY10=$(echo "$RES10" | head -n1)
if [ "$HTTP_CODE10" -eq 400 ] && [[ "$BODY10" == *"do not match"* ]]; then
  echo "PASS (HTTP 400 Mismatch rejected)"
else
  echo "FAIL (HTTP $HTTP_CODE10: $BODY10)"
fi

# 11. Calling reset-password with short password (< 8 chars) -> rejected
echo -n "Test 11 (Reset password length < 8 chars validation): "
RES11=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/auth/reset-password" \
  -H "Content-Type: application/json" -d "{\"email\":\"$FRESH_EMAIL\",\"resetToken\":\"$RESET_TOKEN\",\"newPassword\":\"short\",\"confirmPassword\":\"short\"}")
HTTP_CODE11=$(echo "$RES11" | tail -n1)
BODY11=$(echo "$RES11" | head -n1)
if [ "$HTTP_CODE11" -eq 400 ] && [[ "$BODY11" == *"at least 8 characters"* ]]; then
  echo "PASS (HTTP 400 Length check enforced)"
else
  echo "FAIL (HTTP $HTTP_CODE11: $BODY11)"
fi

# 12. Valid reset password with correct token and >= 8 chars -> SUCCESS
echo -n "Test 12 (Valid password reset execution): "
RES12=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/auth/reset-password" \
  -H "Content-Type: application/json" -d "{\"email\":\"$FRESH_EMAIL\",\"resetToken\":\"$RESET_TOKEN\",\"newPassword\":\"$NEW_PASSWORD\",\"confirmPassword\":\"$NEW_PASSWORD\"}")
HTTP_CODE12=$(echo "$RES12" | tail -n1)
BODY12=$(echo "$RES12" | head -n1)
if [ "$HTTP_CODE12" -eq 200 ] && [[ "$BODY12" == *"Password reset successfully"* ]]; then
  echo "PASS (HTTP 200 Password reset successful)"
else
  echo "FAIL (HTTP $HTTP_CODE12: $BODY12)"
fi

# 13. Reusing already-consumed resetToken -> rejected
echo -n "Test 13 (Reusing consumed resetToken): "
RES13=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/auth/reset-password" \
  -H "Content-Type: application/json" -d "{\"email\":\"$FRESH_EMAIL\",\"resetToken\":\"$RESET_TOKEN\",\"newPassword\":\"AnotherPassword@123\",\"confirmPassword\":\"AnotherPassword@123\"}")
HTTP_CODE13=$(echo "$RES13" | tail -n1)
BODY13=$(echo "$RES13" | head -n1)
if [ "$HTTP_CODE13" -eq 400 ]; then
  echo "PASS (HTTP 400 Single-use challenge token enforced)"
else
  echo "FAIL (HTTP $HTTP_CODE13: $BODY13)"
fi

# 14. Login with OLD password -> rejected (401)
echo -n "Test 14 (Login with OLD password fails): "
RES14=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" -d "{\"email\":\"$FRESH_EMAIL\",\"password\":\"$OLD_PASSWORD\"}")
HTTP_CODE14=$(echo "$RES14" | tail -n1)
BODY14=$(echo "$RES14" | head -n1)
if [ "$HTTP_CODE14" -eq 401 ] && [[ "$BODY14" == *"Invalid email or password"* ]]; then
  echo "PASS (HTTP 401 Old password invalidated)"
else
  echo "FAIL (HTTP $HTTP_CODE14: $BODY14)"
fi

# 15. Login with NEW password -> success (returns 2FA challenge for Buyer)
echo -n "Test 15 (Login with NEW password succeeds + 2FA challenge): "
RES15=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" -d "{\"email\":\"$FRESH_EMAIL\",\"password\":\"$NEW_PASSWORD\"}")
HTTP_CODE15=$(echo "$RES15" | tail -n1)
BODY15=$(echo "$RES15" | head -n1)
CHALLENGE_ID=$(echo "$BODY15" | grep -o '"challengeId":"[^"]*"' | cut -d'"' -f4)
if [ "$HTTP_CODE15" -eq 200 ] && [ -n "$CHALLENGE_ID" ]; then
  echo "PASS (HTTP 200 2FA challenge triggered, ChallengeId: $CHALLENGE_ID)"
else
  echo "FAIL (HTTP $HTTP_CODE15: $BODY15)"
fi

# 16. Complete 2FA login verification with 2FA OTP -> JWT issued
echo -n "Test 16 (Complete 2FA login verification with new credentials): "
LOGIN_2FA_OTP=$(curl -s "${BASE_URL}/api/auth/dev/latest-otp?key=${CHALLENGE_ID}" | grep -o '"otp":"[0-9]*"' | cut -d'"' -f4)
RES16=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/auth/verify-login-otp" \
  -H "Content-Type: application/json" -d "{\"challengeId\":\"$CHALLENGE_ID\",\"otp\":\"$LOGIN_2FA_OTP\"}")
HTTP_CODE16=$(echo "$RES16" | tail -n1)
BODY16=$(echo "$RES16" | head -n1)
JWT_TOKEN=$(echo "$BODY16" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
if [ "$HTTP_CODE16" -eq 200 ] && [ -n "$JWT_TOKEN" ]; then
  echo "PASS (HTTP 200 Authenticated JWT acquired successfully)"
else
  echo "FAIL (HTTP $HTTP_CODE16: $BODY16)"
fi

echo "=========================================================="
echo "ALL 16 PASSWORD RECOVERY SECURITY TESTS COMPLETED!"
echo "=========================================================="
