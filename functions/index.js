const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

/** 교회 이름 정규화: 공백 제거 + 소문자 변환 */
function normalizeName(s) {
  if (!s) return "";
  return s.toLowerCase().replace(/\s+/g, "");
}

/**
 * churches 문서 생성/수정 시 name_norm 자동 업데이트 (Firebase Functions v1)
 * - Spark 플랜에서 동작
 */
exports.onChurchWrite = functions.firestore
    .document("churches/{id}")
    .onWrite(async (change) => {
      const after = change.after.exists ? change.after : null;
      if (!after) return null; // 삭제 이벤트면 스킵

      const data = after.data() || {};
      const name = data.name || "";
      const computed = normalizeName(name);

      if (data.name_norm === computed) return null;

      return after.ref.update({
        name_norm: computed,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });
    });

/** 간단 헬스체크 */
exports.health = functions.https.onRequest((_req, res) => {
  res.status(200).send("OK");
});