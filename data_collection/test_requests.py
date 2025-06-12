import requests

response = requests.get("https://google.com")
print("✅ 상태 코드:", response.status_code)
