@echo off
echo === Test 1: OpenIM Server API ===
curl -s --connect-timeout 5 http://localhost:10002/auth/user_token -H "Content-Type: application/json" -d "{\"secret\":\"openIMAdmin@2026\",\"userID\":\"openIMAdmin\",\"platformID\":1}"

echo.
echo === Test 2: OpenIM WS Port ===
curl -s --connect-timeout 5 -i http://localhost:10001/

echo.
echo === Test 3: Spring Boot STOMP endpoint ===
curl -s --connect-timeout 5 -i http://localhost:8080/stomp

echo.
echo === Test 4: Spring Boot API ===
curl -s --connect-timeout 5 http://localhost:8080/api/consultants

echo.
echo === Test 5: OpenIM /openim/config ===
curl -s --connect-timeout 5 http://localhost:8080/api/openim/config
