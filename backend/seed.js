const pg = require('pg');

const client = new pg.Client({
  host: 'ep-lively-silence-a8rqu8ps.eastus2.azure.neon.tech',
  port: 5432,
  database: 'neetpg',
  user: 'neondb_owner',
  password: 'npg_kMYt9CQOgF2c',
});

async function seedData() {
  await client.connect();

  const subjects = [
    'Anatomy', 'Physiology', 'Biochemistry', 'Pathology', 'Pharmacology',
    'Microbiology', 'Forensic Medicine', 'Community Medicine', 'ENT',
    'Ophthalmology', 'Medicine', 'Surgery', 'Obstetrics and Gynecology',
    'Pediatrics', 'Orthopedics', 'Dermatology', 'Psychiatry', 'Radiology', 'Anesthesia'
  ];

  for (const subjectName of subjects) {
    const result = await client.query('INSERT INTO subject (name, created_at, updated_at) VALUES ($1, NOW(), NOW()) ON CONFLICT DO NOTHING RETURNING id', [subjectName]);
    console.log(`✅ Ensured subject: ${subjectName}`);
  }

  await client.end();
  console.log('✅ Seeding complete!');
}

seedData().catch(console.error);
