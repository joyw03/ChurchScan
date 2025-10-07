// tools/upload_churches.js
const fs = require("fs");
const path = require("path");
const {parse} = require("csv-parse");
const admin = require("firebase-admin");

// 서비스 계정 키 로드
const keyPath = path.join(__dirname, "serviceAccountKey.json");
admin.initializeApp({ credential: admin.credential.cert(require(keyPath)) });
const db = admin.firestore();

// 공백 제거 + 소문자
const normalizeName = (s) => (s || "").toLowerCase().replace(/\s+/g, "");

async function run(csvPath, limit = 50) {
  const stream = fs.createReadStream(csvPath).pipe(parse({columns: true, trim: true}));
  let count = 0;

  for await (const row of stream) {
    if (count >= limit) break;

    const name = row.name || row.교회명 || "";
    const doc = {
      name,
      name_norm: normalizeName(name),
      address: row.address || row.주소 || "",
      denomination: row.denomination || row.교단 || "",
      pastor: row.pastor || row.담임목사 || "",
      website: row.website || "",
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    };

    await db.collection("churches").add(doc);
    count++;
  }

  console.log(`✅ churches 업로드 완료: ${count}건`);
}

const csvPath = process.argv[2] || path.join(__dirname, "churches_sample.csv");
const limit = parseInt(process.argv[3] || "50", 10);
run(csvPath, limit).catch((e) => { console.error("❌ 오류:", e); process.exit(1); });
