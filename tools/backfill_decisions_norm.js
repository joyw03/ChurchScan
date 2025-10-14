const admin = require("firebase-admin");
const path = require("path");

const keyPath = path.join(__dirname, "serviceAccountKey.json");
admin.initializeApp({
  credential: admin.credential.cert(require(keyPath))
});
const db = admin.firestore();

const normalize = (text) =>
  (text || "").toString().trim().toLowerCase().replace(/\s+/g, "");

(async () => {
  const snap = await db.collection("jesus114_decisions").get();
  let updated = 0;

  for (const doc of snap.docs) {
    const data = doc.data();
    const title = data.title || "";
    const norm = normalize(title);

    if (norm && data.title_norm !== norm) {
      await doc.ref.update({
        title_norm: norm,
        updatedAt: admin.firestore.FieldValue.serverTimestamp()
      });
      updated++;
    }
  }

  console.log(`✅ jesus114_decisions 백필 완료: ${updated}개 문서 수정됨`);
  process.exit(0);
})();
