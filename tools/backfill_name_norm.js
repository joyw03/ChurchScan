// tools/backfill_name_norm.js
const admin = require("firebase-admin");
const path = require("path");

// 같은 폴더에 둔 서비스계정 키 사용
const keyPath = path.join(__dirname, "serviceAccountKey.json");
admin.initializeApp({ credential: admin.credential.cert(require(keyPath)) });

const db = admin.firestore();

// 공백 제거 + 소문자 변환 (한글은 소문자 영향 없음)
const normalize = (s) => (s || "").toLowerCase().replace(/\s+/g, "");

(async () => {
  const snap = await db.collection("churches").get();
  let updated = 0;

  for (const doc of snap.docs) {
    const d = doc.data();
    const want = normalize(d.name);
    if (d.name_norm !== want) {
      await doc.ref.update({
        name_norm: want,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });
      updated++;
    }
  }

  console.log(`✅ name_norm 업데이트: ${updated}개 문서`);
  process.exit(0);
})().catch((e) => {
  console.error("❌ 오류:", e);
  process.exit(1);
});
