import firebase_admin
from firebase_admin import credentials, firestore
import pandas as pd

# Firebase 인증
cred = credentials.Certificate("serviceAccountKey.json")
firebase_admin.initialize_app(cred)

# Firestore 클라이언트
db = firestore.client()

# CSV 읽기 (헤더는 첫 줄)
csv_file = "jesus114_교단결의_정제본.csv"
df = pd.read_csv(csv_file, encoding="utf-8-sig", header=0)

# 업로드할 컬렉션 이름
collection_name = "jesus114_decisions"

# Firestore 업로드
for index, row in df.iterrows():
    data = {
        "title": row["title"],
        "denomination": row["denomination"],
        "year": row["year"],
        "action": row["action"],
        "reason": row["reason"]
    }
    db.collection(collection_name).add(data)

print(f"✅ Firestore에 총 {len(df)}개 문서를 업로드했습니다.")
