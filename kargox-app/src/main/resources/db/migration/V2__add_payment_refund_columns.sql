-- Adds refund tracking to payments (supports PaymentService.refund()).
--
-- Written as its own migration rather than editing V1 - once a migration has
-- been applied to a real database, Flyway records a checksum of its exact
-- content and refuses to proceed if that file changes later (by design,
-- since silently reinterpreting an already-applied migration is exactly how
-- schemas and data drift out of sync). Further schema changes always belong
-- in a new Vn file from here on, never a retroactive edit to an old one.
--
-- IF NOT EXISTS guards make this safe to run regardless of whether an
-- earlier, since-reverted copy of V1 already added these columns directly.

ALTER TABLE payments ADD COLUMN IF NOT EXISTS refunded_at TIMESTAMPTZ;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS refund_reason VARCHAR(500);
