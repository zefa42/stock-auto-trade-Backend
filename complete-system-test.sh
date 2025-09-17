#!/bin/bash

# Complete System Test Script for Auto Trading Backend
# This script tests all endpoints: health, signup, login, KIS token, watchlist, stock details, admin functions

BASE_URL="http://localhost:8080"
ADMIN_USER="admin"
ADMIN_PASS="admin123"
TEST_USER="testuser$(date +%s)"
TEST_PASS="testpass123"
TEST_EMAIL="test$(date +%s)@example.com"

echo "=== Complete System Test for Auto Trading Backend ==="
echo "Base URL: $BASE_URL"
echo "Test User: $TEST_USER"
echo "Admin User: $ADMIN_USER"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to test endpoint
test_endpoint() {
    local name="$1"
    local method="$2"
    local url="$3"
    local auth="$4"
    local data="$5"
    local expected_status="$6"
    
    echo -n "Testing $name... "
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X "$method" -H "Content-Type: application/json" $auth -d "$data" "$url")
    else
        response=$(curl -s -w "\n%{http_code}" $auth -X "$method" "$url")
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" = "$expected_status" ]; then
        echo -e "${GREEN}✓ PASS${NC} (HTTP $http_code)"
        return 0
    else
        echo -e "${RED}✗ FAIL${NC} (Expected HTTP $expected_status, got HTTP $http_code)"
        echo "Response: $body"
        return 1
    fi
}

# Function to extract value from JSON response
extract_json_value() {
    local json="$1"
    local key="$2"
    echo "$json" | grep -o "\"$key\"[[:space:]]*:[[:space:]]*\"[^\"]*\"" | cut -d'"' -f4
}

# Function to extract number from JSON response
extract_json_number() {
    local json="$1"
    local key="$2"
    echo "$json" | grep -o "\"$key\"[[:space:]]*:[[:space:]]*[0-9]*" | cut -d':' -f2 | tr -d ' '
}

# Test 1: Health Check
echo "1. Testing Health Check..."
if test_endpoint "Health Check" "GET" "$BASE_URL/actuator/health" "" "" "200"; then
    echo "   ✓ System is healthy"
else
    echo "   ✗ System health check failed"
    exit 1
fi
echo ""

# Test 2: User Signup
echo "2. Testing User Signup..."
signup_data="{\"username\":\"$TEST_USER\",\"password\":\"$TEST_PASS\",\"email\":\"$TEST_EMAIL\"}"
if test_endpoint "User Signup" "POST" "$BASE_URL/auth/signup" "" "$signup_data" "200"; then
    echo "   ✓ User signup successful"
else
    echo "   ✗ User signup failed"
    exit 1
fi
echo ""

# Test 3: User Login
echo "3. Testing User Login..."
login_data="{\"username\":\"$TEST_USER\",\"password\":\"$TEST_PASS\"}"
login_response=$(curl -s -X POST -H "Content-Type: application/json" -d "$login_data" "$BASE_URL/auth/login")
if test_endpoint "User Login" "POST" "$BASE_URL/auth/login" "" "$login_data" "200"; then
    ACCESS_TOKEN=$(extract_json_value "$login_response" "accessToken")
    REFRESH_TOKEN=$(extract_json_value "$login_response" "refreshToken")
    echo "   ✓ User login successful"
    echo "   Access Token: ${ACCESS_TOKEN:0:20}..."
else
    echo "   ✗ User login failed"
    exit 1
fi
echo ""

# Test 4: Get Symbols
echo "4. Testing Symbol Search..."
symbols_response=$(curl -s "$BASE_URL/api/symbols?market=KRX&search=삼성")
if test_endpoint "Symbol Search" "GET" "$BASE_URL/api/symbols?market=KRX&search=삼성" "" "" "200"; then
    # Extract first symbol ID and ticker
    SYMBOL_ID=$(echo "$symbols_response" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    SYMBOL_TICKER=$(echo "$symbols_response" | grep -o '"ticker":"[^"]*"' | head -1 | cut -d'"' -f4)
    echo "   ✓ Symbol search successful"
    echo "   Found Symbol ID: $SYMBOL_ID, Ticker: $SYMBOL_TICKER"
else
    echo "   ✗ Symbol search failed"
    exit 1
fi
echo ""

# Test 5: Add to Watchlist
echo "5. Testing Add to Watchlist..."
watchlist_data="{\"symbolId\":$SYMBOL_ID}"
if test_endpoint "Add to Watchlist" "POST" "$BASE_URL/api/watchlist" "-H \"Authorization: Bearer $ACCESS_TOKEN\"" "$watchlist_data" "200"; then
    echo "   ✓ Added symbol to watchlist"
else
    echo "   ✗ Failed to add symbol to watchlist"
    exit 1
fi
echo ""

# Test 6: Get Watchlist
echo "6. Testing Get Watchlist..."
if test_endpoint "Get Watchlist" "GET" "$BASE_URL/api/watchlist" "-H \"Authorization: Bearer $ACCESS_TOKEN\"" "" "200"; then
    echo "   ✓ Retrieved watchlist successfully"
else
    echo "   ✗ Failed to retrieve watchlist"
    exit 1
fi
echo ""

# Test 7: KIS Token Issue
echo "7. Testing KIS Token Issue..."
kis_token_response=$(curl -s -X POST -H "Content-Type: application/json" -d "{}" "$BASE_URL/api/kis/token")
if test_endpoint "KIS Token Issue" "POST" "$BASE_URL/api/kis/token" "" "{}" "200"; then
    KIS_ACCESS_TOKEN=$(extract_json_value "$kis_token_response" "accessToken")
    echo "   ✓ KIS token issued successfully"
    echo "   KIS Token: ${KIS_ACCESS_TOKEN:0:20}..."
else
    echo "   ✗ KIS token issue failed"
    exit 1
fi
echo ""

# Test 8: Get Stock Detail
echo "8. Testing Get Stock Detail..."
if test_endpoint "Get Stock Detail" "GET" "$BASE_URL/api/quote/stock/$SYMBOL_ID" "-H \"Authorization: Bearer $ACCESS_TOKEN\"" "" "200"; then
    echo "   ✓ Retrieved stock detail successfully"
else
    echo "   ✗ Failed to retrieve stock detail"
    exit 1
fi
echo ""

# Test 9: Admin - Refresh Quotes
echo "9. Testing Admin Quote Refresh..."
admin_auth="-u $ADMIN_USER:$ADMIN_PASS"
refresh_response=$(curl -s $admin_auth -X POST "$BASE_URL/admin/quotes/refresh")
if test_endpoint "Admin Quote Refresh" "POST" "$BASE_URL/admin/quotes/refresh" "$admin_auth" "" "200"; then
    UPDATED_COUNT=$(extract_json_number "$refresh_response" "updated")
    echo "   ✓ Admin quote refresh successful"
    echo "   Updated quotes: $UPDATED_COUNT"
else
    echo "   ✗ Admin quote refresh failed"
    exit 1
fi
echo ""

# Test 10: Admin - Get Quotes
echo "10. Testing Admin Get Quotes..."
if test_endpoint "Admin Get Quotes" "GET" "$BASE_URL/admin/quotes?limit=10&page=0" "$admin_auth" "" "200"; then
    echo "   ✓ Admin get quotes successful"
else
    echo "   ✗ Admin get quotes failed"
    exit 1
fi
echo ""

# Test 11: Admin - Get Quotes by Tickers
echo "11. Testing Admin Get Quotes by Tickers..."
if test_endpoint "Admin Get Quotes by Tickers" "GET" "$BASE_URL/admin/quotes/by-tickers?tickers=$SYMBOL_TICKER" "$admin_auth" "" "200"; then
    echo "   ✓ Admin get quotes by tickers successful"
else
    echo "   ✗ Admin get quotes by tickers failed"
    exit 1
fi
echo ""

# Test 12: Token Refresh
echo "12. Testing Token Refresh..."
refresh_data="{\"refreshToken\":\"$REFRESH_TOKEN\"}"
if test_endpoint "Token Refresh" "POST" "$BASE_URL/auth/refresh" "" "$refresh_data" "200"; then
    echo "   ✓ Token refresh successful"
else
    echo "   ✗ Token refresh failed"
    exit 1
fi
echo ""

echo -e "${GREEN}=== All Tests Completed Successfully! ===${NC}"
echo ""
echo "Summary:"
echo "- Health Check: ✓"
echo "- User Signup: ✓"
echo "- User Login: ✓"
echo "- Symbol Search: ✓"
echo "- Add to Watchlist: ✓"
echo "- Get Watchlist: ✓"
echo "- KIS Token Issue: ✓"
echo "- Get Stock Detail: ✓"
echo "- Admin Quote Refresh: ✓"
echo "- Admin Get Quotes: ✓"
echo "- Admin Get Quotes by Tickers: ✓"
echo "- Token Refresh: ✓"
echo ""
echo "Total: 12/12 tests passed"
echo ""
echo "Test User: $TEST_USER"
echo "Symbol ID: $SYMBOL_ID"
echo "Symbol Ticker: $SYMBOL_TICKER"
echo "Updated Quotes: $UPDATED_COUNT"
