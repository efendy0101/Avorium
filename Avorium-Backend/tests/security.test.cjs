const { test } = require('node:test');
const assert = require('node:assert/strict');
const { spawnSync } = require('node:child_process');
const path = require('node:path');
const modulePath = path.resolve(__dirname, '../dist/src/jwtSecret.js');
for (const [label, value, success] of [['missing', '', false], ['example', 'replace-with-a-long-random-local-secret', false], ['random-shaped', 'test-only-unique-value-12345678901234567890', true]]) {
  test(`JWT secret: ${label}`, () => {
    const result = spawnSync(process.execPath, [modulePath], { env: { ...process.env, JWT_SECRET: value } });
    assert.equal(result.status === 0, success);
  });
}
test('unverified purchase is rejected', async () => {
  const { verifyPurchase } = require('../dist/src/controllers/iapController');
  let status;
  const res = { status(value) { status = value; return this; }, json() { return this; } };
  await verifyPurchase({ user: { userId: 'demo', role: 'USER' }, body: { purchaseToken: 'fake', productId: 'demo' } }, res);
  assert.equal(status, 503);
});
