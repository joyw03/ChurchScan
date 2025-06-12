import requests
from bs4 import BeautifulSoup
import csv

url = "http://www.jesus114.net/main/sub.html?Mode=view&boardID=www69&num=8681&page=1&keyfield=&key=&bCate="

response = requests.get(url)
response.encoding = "utf-8"
soup = BeautifulSoup(response.text, "html.parser")

table = soup.find("table")
rows = table.find_all("tr")[1:]

data = []

for idx, row in enumerate(rows, 1):
    cols = row.find_all("td")
    if len(cols) < 2:
        print(f"⚠️ 완전 무시 (행 {idx}): td 개수 = {len(cols)}")
        continue

    item = {
        "group": cols[0].get_text(strip=True) if len(cols) > 0 else "",
        "title": cols[1].get_text(strip=True) if len(cols) > 1 else "",
        "year": cols[2].get_text(strip=True) if len(cols) > 2 else "",
        "action": cols[3].get_text(strip=True) if len(cols) > 3 else "",
        "reason": cols[4].get_text(strip=True) if len(cols) > 4 else ""
    }

    if item["title"]:
        data.append(item)
    else:
        print(f"⛔ 단체명 없음, 건너뜀 (행 {idx})")

with open("jesus114_교단결의.csv", "w", newline="", encoding="utf-8-sig") as f:
    writer = csv.DictWriter(f, fieldnames=["group", "title", "year", "action", "reason"])
    writer.writeheader()
    writer.writerows(data)

print(f"\n✅ 최종 저장 항목 수: {len(data)}개 (결측 허용)")