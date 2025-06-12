# import requests
# from bs4 import BeautifulSoup
# import csv
# import os
#
# # 🔗 수집 대상 페이지
# url = "http://www.jesus114.net/main/sub.html?Mode=view&boardID=www69&num=8681&page=1&keyfield=&key=&bCate="
#
# # 🧲 HTML 요청
# response = requests.get(url)
# response.encoding = "utf-8"
# soup = BeautifulSoup(response.text, "html.parser")
#
# # 🧱 첫 번째 <table> 가져오기
# table = soup.find("table")
# if not table:
#     print("❌ 표를 찾을 수 없습니다.")
#     exit()
#
# rows = table.find_all("tr")[1:]  # 헤더 제외
# data = []
#
# # 🧐 행 반복 수집 + 누락 로그
# for idx, row in enumerate(rows, 1):
#     cols = row.find_all("td")
#     if len(cols) < 5:
#         print(f"⚠️ 건너뜀 (행 {idx}): td 개수 = {len(cols)}")
#         continue
#
#     item = {
#         "group": cols[0].get_text(strip=True),
#         "title": cols[1].get_text(strip=True),
#         "year": cols[2].get_text(strip=True),
#         "action": cols[3].get_text(strip=True),
#         "reason": cols[4].get_text(strip=True)
#     }
#     data.append(item)
#
# # 📝 CSV로 저장
# if data:
#     csv_filename = "jesus114_교단결의.csv"
#     with open(csv_filename, "w", newline="", encoding="utf-8-sig") as f:
#         writer = csv.DictWriter(f, fieldnames=["group", "title", "year", "action", "reason"])
#         writer.writeheader()
#         writer.writerows(data)
#     print(f"\n✅ {len(data)}개의 항목을 '{csv_filename}'에 저장했습니다.")
# else:
#     print("❌ 수집된 데이터가 없습니다.")
