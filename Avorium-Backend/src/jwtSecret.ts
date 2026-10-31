// Fail closed rather than using a published signing key.
const secret = process.env.JWT_SECRET;
if (!secret || secret.length < 32 || secret === 'replace-with-a-long-random-local-secret' || secret.startsWith('REPLACE_WITH_') || secret === 'super_secret_avorium_key') {
  throw new Error('JWT_SECRET must be a unique random secret of at least 32 characters.');
}
export const JWT_SECRET: string = secret;
