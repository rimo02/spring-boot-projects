-- Development fixture ONLY. Register these emails through /api/auth/register first.
-- Passwords remain BCrypt hashes created by the application; this script never sets passwords.
-- In psql: \i scripts/seed-demo.sql (or pipe it into docker compose exec -T postgres psql).
BEGIN;
DO $$
BEGIN
 IF (SELECT count(*) FROM app_users WHERE email IN ('alice@example.test','bob@example.test','admin@example.test')) <> 3 THEN
  RAISE EXCEPTION 'Register alice@example.test, bob@example.test and admin@example.test before seeding';
 END IF;
END $$;
UPDATE app_users SET role='ADMIN' WHERE email='admin@example.test';
INSERT INTO accounts(id,owner_id,display_name,currency,balance,created_at)
 SELECT '11111111-1111-1111-1111-111111111111',id,'Alice savings','INR',10000.00,now()
 FROM app_users WHERE email='alice@example.test'
 ON CONFLICT(id) DO NOTHING;
INSERT INTO accounts(id,owner_id,display_name,currency,balance,created_at)
 SELECT '22222222-2222-2222-2222-222222222222',id,'Bob savings','INR',2000.00,now()
 FROM app_users WHERE email='bob@example.test'
 ON CONFLICT(id) DO NOTHING;
COMMIT;
