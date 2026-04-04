import urllib.request
import json

# Get admin token
url = "http://127.0.0.1:10002/auth/get_admin_token"
data = json.dumps({"secret": "openIM123", "userID": "imAdmin", "platformID": 1}).encode()
req = urllib.request.Request(url, data=data, headers={"Content-Type": "application/json", "operationID": "test123"})
resp = json.loads(urllib.request.urlopen(req).read())
print("Admin token response:", resp)
token = resp["data"]["token"]
print("Token:", token[:50], "...")

# Register a user
url2 = "http://127.0.0.1:10002/user_register"
body = json.dumps({"secret": "openIM123", "users": [{"userID": "testuser1", "nickname": "测试用户", "faceURL": ""}]}).encode()
req2 = urllib.request.Request(url2, data=body, headers={"Content-Type": "application/json", "operationID": "test456", "token": token})
resp2 = json.loads(urllib.request.urlopen(req2).read())
print("User register response:", resp2)
