#!/usr/bin/env bash
# ==============================================================
# SHOPSPHERE - REGISTRATION WITHOUT OTP VERIFICATION TEST SUITE
# ==============================================================

set -e
TIMESTAMP=$(date +%s)
BASE_URL="http://localhost:8080/api/auth"

echo "=== 1. Public BUYER Registration (Direct, No OTP) ==="
BUYER_EMAIL="direct_buyer_${TIMESTAMP}@example.com"
BUYER_PHONE="98${TIMESTAMP:0:8}"
BUYER_RESP=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"firstName\": \"Direct\",
    \"lastName\": \"Buyer\",
    \"email\": \"${BUYER_EMAIL}\",
    \"phone\": \"${BUYER_PHONE}\",
    \"password\": \"Password@123\",
    \"role\": \"BUYER\"
  }")
BUYER_STATUS=$(echo "$BUYER_RESP" | tail -n1)
BUYER_BODY=$(echo "$BUYER_RESP" | sed '$d')
echo "HTTP Status: $BUYER_STATUS"
echo "Response: $BUYER_BODY"

if [ "$BUYER_STATUS" -eq 201 ]; then
  echo "PASS: Buyer account registered directly without OTP blocking."
else
  echo "FAIL: Expected 201, got $BUYER_STATUS"
  exit 1
fi
echo ""

echo "=== 2. Immediate BUYER Login (Receives JWT Directly) ==="
LOGIN_RESP=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"${BUYER_EMAIL}\",
    \"password\": \"Password@123\"
  }")
LOGIN_STATUS=$(echo "$LOGIN_RESP" | tail -n1)
LOGIN_BODY=$(echo "$LOGIN_RESP" | sed '$d')
echo "HTTP Status: $LOGIN_STATUS"
echo "$LOGIN_BODY" | grep -o '"role":"BUYER"'
echo "$LOGIN_BODY" | grep -o '"token":"[^"]*"' | head -c 40
echo "...\""

if [ "$LOGIN_STATUS" -eq 200 ]; then
  echo "PASS: Buyer logged in directly and received JWT token."
else
  echo "FAIL: Expected 200, got $LOGIN_STATUS"
  exit 1
fi
echo ""

echo "=== 3. Public SELLER Registration & Immediate Login ==="
SELLER_EMAIL="direct_seller_${TIMESTAMP}@example.com"
SELLER_PHONE="97${TIMESTAMP:0:8}"
SELLER_RESP=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"firstName\": \"Direct\",
    \"lastName\": \"Seller\",
    \"email\": \"${SELLER_EMAIL}\",
    \"phone\": \"${SELLER_PHONE}\",
    \"password\": \"Password@123\",
    \"role\": \"SELLER\"
  }")
SELLER_STATUS=$(echo "$SELLER_RESP" | tail -n1)
if [ "$SELLER_STATUS" -eq 201 ]; then
  echo "PASS: Seller registered directly."
else
  echo "FAIL: Expected 201, got $SELLER_STATUS"
  exit 1
fi

SELLER_LOGIN_RESP=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"${SELLER_EMAIL}\",
    \"password\": \"Password@123\"
  }")
SELLER_LOGIN_STATUS=$(echo "$SELLER_LOGIN_RESP" | tail -n1)
if [ "$SELLER_LOGIN_STATUS" -eq 200 ]; then
  echo "PASS: Seller logged in directly and received JWT token."
else
  echo "FAIL: Expected 200, got $SELLER_LOGIN_STATUS"
  exit 1
fi
echo ""

echo "=== 4. ADMIN Login Test ==="
ADMIN_LOGIN_RESP=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@shopsphere.com",
    "password": "Password@123"
  }')
ADMIN_LOGIN_STATUS=$(echo "$ADMIN_LOGIN_RESP" | tail -n1)
if [ "$ADMIN_LOGIN_STATUS" -eq 200 ]; then
  echo "PASS: Admin login works unaffected."
else
  echo "FAIL: Expected 200, got $ADMIN_LOGIN_STATUS"
  exit 1
fi
echo ""

echo "=== 5. Validation Check: Duplicate Phone Rejection ==="
DUP_RESP=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"firstName\": \"Dup\",
    \"lastName\": \"Test\",
    \"email\": \"new_email_${TIMESTAMP}@example.com\",
    \"phone\": \"${BUYER_PHONE}\",
    \"password\": \"Password@123\",
    \"role\": \"BUYER\"
  }")
DUP_STATUS=$(echo "$DUP_RESP" | tail -n1)
if [ "$DUP_STATUS" -eq 400 ]; then
  echo "PASS: Duplicate phone correctly rejected with 400 Bad Request."
else
  echo "FAIL: Expected 400, got $DUP_STATUS"
  exit 1
fi
echo ""

echo "=== ALL REGISTRATION & AUTHENTICATION TESTS PASSED ==="
