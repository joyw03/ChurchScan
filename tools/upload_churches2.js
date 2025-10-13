const admin = require("firebase-admin");
const serviceAccount = require("./serviceAccountKey.json");
admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });
const db = admin.firestore();

const churches = [
  {
    name: "한빛 교회",
    name_norm: "한빛교회",
    address: "서울특별시 강남구 테헤란로 123",
    denomination: "장로교",
    pastor: "김한빛",
    website: "http://hanbit.example.com"
  },
  {
    name: "열린 은혜 교회",
    name_norm: "열린은혜교회",
    address: "경기도 용인시 기흥구 중앙로 55",
    denomination: "감리교",
    pastor: "박은혜",
    website: "http://graceopen.example.com"
  },
  {
    name: "빛과 소금 교회",
    name_norm: "빛과소금교회",
    address: "부산광역시 해운대구 해운대로 77",
    denomination: "순복음",
    pastor: "이소금",
    website: "http://saltlight.example.com"
  }
];

(async () => {
  for (const c of churches) {
    const now = new Date();
    await db.collection("churches").add({
      ...c,
      createdAt: now,
      updatedAt: now
    });
    console.log(`✅ ${c.name} 추가 완료`);
  }
})();
