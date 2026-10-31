require('dotenv/config');

const { PrismaClient } = require('@prisma/client');
const { PrismaMariaDb } = require('@prisma/adapter-mariadb');

const prisma = new PrismaClient({ adapter: new PrismaMariaDb(process.env.DATABASE_URL) });

async function main() {
  const email = process.argv[2]?.trim().toLowerCase();

  if (!email) {
    throw new Error('Usage: npm run make:admin -- user@example.com');
  }

  const user = await prisma.user.update({
    where: { email },
    data: { role: 'ADMIN' },
    select: {
      id: true,
      email: true,
      name: true,
      role: true
    }
  });

  console.log(`Admin role granted: ${user.email} (${user.name})`);
}

main()
  .catch((error) => {
    console.error(error);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
