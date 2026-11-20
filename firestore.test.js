const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastDate(msAgo = 2000) {
  return new Date(Date.now() - msAgo);
}

function validUserSettings(uid, ts = pastDate()) {
  return {
    userId: uid,
    displayName: "كمال",
    soundEnabled: true,
    vibrateEnabled: true,
    activeDhikrId: "istighfar",
    targetCount: 33,
    createdAt: ts,
    updatedAt: ts,
  };
}

function validCustomDhikr(uid, ts = pastDate()) {
  return {
    userId: uid,
    title: "سبحان الله العظيم وبحمده",
    target: 100,
    colorHex: "#D4AF37",
    countsAsIstighfar: false,
    createdAt: ts,
    updatedAt: ts,
  };
}

function validLeaderboardEntry(uid, ts = pastDate()) {
  return {
    userId: uid,
    displayName: "كمال",
    visibility: "public",
    todayDate: "2026-11-20",
    todayTotal: 33,
    todayIstighfar: 33,
    allTimeTotal: 150,
    allTimeIstighfar: 100,
    dhikrTotals: { istighfar: 100, subhanallah: 50 },
    dhikrToday: { istighfar: 33 },
    createdAt: ts,
    updatedAt: ts,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: denied all reads and writes across all collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set(validUserSettings(ALICE_UID))
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("custom_adhkar").get()
  );
  await assertFails(
    unauthDb.collection("leaderboard").where("visibility", "==", "public").get()
  );
  await assertFails(
    unauthDb.collection("leaderboard").doc(ALICE_UID).set(validLeaderboardEntry(ALICE_UID))
  );
});

test("UserSettings (/users/{uid}): owner create, partial update, read succeed; cross-user & ghost fields fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = pastDate(10000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validUserSettings(ALICE_UID, created))
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());

  // Partial update of allowed fields
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      targetCount: 99,
      soundEnabled: false,
      updatedAt: pastDate(1000),
    })
  );

  // Reject immutable createdAt mutation
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: pastDate(500),
      updatedAt: pastDate(500),
    })
  );

  // Reject shadow/ghost field injection
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: pastDate(500),
    })
  );

  // Cross-user isolation
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      displayName: "Hijacked",
      updatedAt: pastDate(500),
    })
  );
  // Listing /users is forbidden
  await assertFails(aliceDb.collection("users").get());
});

test("CustomDhikr (/users/{uid}/custom_adhkar/{id}): owner CRUD & list succeed; cross-user & invalid payloads fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = pastDate(10000);

  const dhikrRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("custom_adhkar")
    .doc("custom_1");

  await assertSucceeds(dhikrRef.set(validCustomDhikr(ALICE_UID, created)));
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("custom_adhkar").get()
  );

  // Partial update
  await assertSucceeds(
    dhikrRef.update({
      title: "لا حول ولا قوة إلا بالله العلي العظيم",
      target: 33,
      updatedAt: pastDate(1000),
    })
  );

  // Reject unexpected @DocumentId 'id' field in payload
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("custom_adhkar")
      .doc("custom_2")
      .set({
        ...validCustomDhikr(ALICE_UID, created),
        id: "custom_2",
      })
  );

  // Reject invalid colorHex format
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("custom_adhkar")
      .doc("custom_3")
      .set({
        ...validCustomDhikr(ALICE_UID, created),
        colorHex: "gold",
      })
  );

  // Reject cross-user list/read/delete
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("custom_adhkar").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("custom_adhkar").doc("custom_1").delete()
  );

  await assertSucceeds(dhikrRef.delete());
});

test("Leaderboard (/leaderboard/{uid}): owner write, filtered public query succeed; unfiltered query, spoofing & future timestamp fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = pastDate(10000);

  // Owner checking if own leaderboard doc exists BEFORE creation MUST succeed
  await assertSucceeds(aliceDb.collection("leaderboard").doc(ALICE_UID).get());

  // Non-owner reading a non-existent leaderboard doc MUST fail
  await assertFails(bobDb.collection("leaderboard").doc(ALICE_UID).get());

  // Owner creates own public leaderboard entry
  await assertSucceeds(
    aliceDb
      .collection("leaderboard")
      .doc(ALICE_UID)
      .set(validLeaderboardEntry(ALICE_UID, created))
  );

  // Owner updates own counters
  await assertSucceeds(
    aliceDb.collection("leaderboard").doc(ALICE_UID).update({
      todayTotal: 66,
      todayIstighfar: 66,
      allTimeTotal: 183,
      allTimeIstighfar: 133,
      dhikrTotals: { istighfar: 133, subhanallah: 50 },
      dhikrToday: { istighfar: 66 },
      updatedAt: pastDate(1000),
    })
  );

  // Authenticated user queries public leaderboard with matching filter
  await assertSucceeds(
    bobDb.collection("leaderboard").where("visibility", "==", "public").get()
  );
  await assertSucceeds(bobDb.collection("leaderboard").doc(ALICE_UID).get());

  // Unfiltered query on top-level collection MUST fail
  await assertFails(bobDb.collection("leaderboard").get());

  // Cross-user write/update/delete MUST fail
  await assertFails(
    bobDb.collection("leaderboard").doc(ALICE_UID).update({
      todayTotal: 999999,
      updatedAt: pastDate(500),
    })
  );
  await assertFails(bobDb.collection("leaderboard").doc(ALICE_UID).delete());

  // Tampered future timestamp MUST fail
  await assertFails(
    bobDb
      .collection("leaderboard")
      .doc(BOB_UID)
      .set(validLeaderboardEntry(BOB_UID, new Date(Date.now() + 60000)))
  );

  // Negative count MUST fail
  await assertFails(
    bobDb.collection("leaderboard").doc(BOB_UID).set({
      ...validLeaderboardEntry(BOB_UID, created),
      todayTotal: -5,
    })
  );

  // Identity spoofing MUST fail
  await assertFails(
    bobDb.collection("leaderboard").doc(BOB_UID).set({
      ...validLeaderboardEntry(ALICE_UID, created),
    })
  );
});
