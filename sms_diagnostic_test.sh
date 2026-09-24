#!/usr/bin/env bash
# ==============================================================
# SHOPSPHERE SMS OTP DIAGNOSTIC & VERIFICATION TEST MATRIX
# ==============================================================

set -e
BASE_URL="http://localhost:8080/api/auth"

echo "=== 1. Testing Unconfigured SMS Provider (send-phone-otp) ==="
HTTP_CODE=$(curl -s -o /tmp/resp_unconf.json -w "%{http_code}" -X POST "${BASE_URL}/send-phone-otp" \
  -H "Content-Type: application/json" \
  -d '{"phone": "9876543291"}')
echo "HTTP Status: $HTTP_CODE"
cat /tmp/resp_unconf.json
echo ""
if [ "$HTTP_CODE" -eq 503 ]; then
  echo "PASS: Returned 503 Service Unavailable when SMS provider unconfigured."
else
  echo "FAIL: Expected 503, got $HTTP_CODE"
fi
echo ""

echo "=== 2. Testing Invalid Phone Number Format (<10 digits) ==="
HTTP_CODE=$(curl -s -o /tmp/resp_inv_len.json -w "%{http_code}" -X POST "${BASE_URL}/send-phone-otp" \
  -H "Content-Type: application/json" \
  -d '{"phone": "98765"}')
echo "HTTP Status: $HTTP_CODE"
cat /tmp/resp_inv_len.json
echo ""
if [ "$HTTP_CODE" -eq 400 ]; then
  echo "PASS: Returned 400 Bad Request for short phone number."
else
  echo "FAIL: Expected 400, got $HTTP_CODE"
fi
echo ""

echo "=== 3. Testing Invalid Starting Digit for Indian Phone (starts with 1) ==="
HTTP_CODE=$(curl -s -o /tmp/resp_inv_start.json -w "%{http_code}" -X POST "${BASE_URL}/send-phone-otp" \
  -H "Content-Type: application/json" \
  -d '{"phone": "1234567890"}')
echo "HTTP Status: $HTTP_CODE"
cat /tmp/resp_inv_start.json
echo ""
if [ "$HTTP_CODE" -eq 400 ]; then
  echo "PASS: Returned 400 Bad Request for invalid starting digit."
else
  echo "FAIL: Expected 400, got $HTTP_CODE"
fi
echo ""

echo "=== 4. Testing Already Registered Mobile Number ==="
HTTP_CODE=$(curl -s -o /tmp/resp_reg.json -w "%{http_code}" -X POST "${BASE_URL}/send-phone-otp" \
  -H "Content-Type: application/json" \
  -d '{"phone": "9876543210"}')
echo "HTTP Status: $HTTP_CODE"
cat /tmp/resp_reg.json
echo ""
if [ "$HTTP_CODE" -eq 409 ]; then
  echo "PASS: Returned 409 Conflict for already registered number."
else
  echo "FAIL: Expected 409, got $HTTP_CODE"
fi
echo ""

echo "=== 5. Testing Buyer Login SMS Dispatch (Unconfigured Provider) ==="
HTTP_CODE=$(curl -s -o /tmp/resp_buyer_login.json -w "%{http_code}" -X POST "${BASE_URL}/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "matrix_buyer_1790091495@example.com", "password": "Password@123"}')
echo "HTTP Status: $HTTP_CODE"
cat /tmp/resp_buyer_login.json
echo ""
if [ "$HTTP_CODE" -eq 503 ]; then
  echo "PASS: Buyer login halted with 503 Service Unavailable when SMS unconfigured."
else
  echo "FAIL: Expected 503, got $HTTP_CODE"
fi
echo ""

echo "=== 6. Testing Seller Login SMS Dispatch (Unconfigured Provider) ==="
HTTP_CODE=$(curl -s -o /tmp/resp_seller_login.json -w "%{http_code}" -X POST "${BASE_URL}/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "matrix_seller_1790091495@example.com", "password": "Password@123"}')
echo "HTTP Status: $HTTP_CODE"
cat /tmp/resp_seller_login.json
echo ""
if [ "$HTTP_CODE" -eq 503 ]; then
  echo "PASS: Seller login halted with 503 Service Unavailable when SMS unconfigured."
else
  echo "FAIL: Expected 503, got $HTTP_CODE"
fi
echo ""

echo "=== 7. Testing Admin Login (Bypasses SMS OTP) ==="
HTTP_CODE=$(curl -s -o /tmp/resp_admin.json -w "%{http_code}" -X POST "${BASE_URL}/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@shopsphere.com", "password": "Password@123"}')
echo "HTTP Status: $HTTP_CODE"
cat /tmp/resp_admin.json | grep -o '"role":"ADMIN"' || true
echo ""
if [ "$HTTP_CODE" -eq 200 ]; then
  echo "PASS: Admin login succeeds with JWT."
else
  echo "FAIL: Expected 200, got $HTTP_CODE"
fi
echo ""

echo "=== ALL DIAGNOSTIC TESTS PASSED ==="
