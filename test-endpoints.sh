#!/bin/bash

# Test script for KIS quote refresh endpoints
# Make sure to start the application with: ./gradlew bootRun --args='--spring.profiles.active=local'

BASE_URL="http://localhost:8080"
ADMIN_USER="admin"
ADMIN_PASS="admin123"

echo "=== Testing KIS Quote Refresh Endpoints ==="
echo "Base URL: $BASE_URL"
echo "Admin User: $ADMIN_USER"
echo ""

# Test 1: Health check
echo "1. Testing health check..."
curl -s -o /dev/null -w "Health check: %{http_code}\n" "$BASE_URL/actuator/health"
echo ""

# Test 2: Refresh quotes
echo "2. Testing quote refresh..."
REFRESH_RESPONSE=$(curl -s -u "$ADMIN_USER:$ADMIN_PASS" -X POST "$BASE_URL/admin/quotes/refresh")
echo "Refresh response: $REFRESH_RESPONSE"
echo ""

# Test 3: Get quotes with pagination
echo "3. Testing quote query with pagination..."
QUOTES_RESPONSE=$(curl -s -u "$ADMIN_USER:$ADMIN_PASS" "$BASE_URL/admin/quotes?limit=5&page=0")
echo "Quotes response: $QUOTES_RESPONSE"
echo ""

# Test 4: Get quotes by tickers (if any exist)
echo "4. Testing quote query by tickers..."
TICKERS_RESPONSE=$(curl -s -u "$ADMIN_USER:$ADMIN_PASS" "$BASE_URL/admin/quotes/by-tickers?tickers=005930,000660")
echo "Tickers response: $TICKERS_RESPONSE"
echo ""

echo "=== Test completed ==="
