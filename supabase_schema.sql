-- ==============================================================================
-- GrowthEngine ERP & POS — Supabase Master Configuration & Setup
-- Successfully Executed & Verified on Supabase Project: wrcuondcuuwkqcgtrigz
-- ==============================================================================

-- 1. Enable UUID Extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==============================================================================
-- 1. FIX REALTIME (Adds core tables to live sync publication)
-- ==============================================================================
ALTER PUBLICATION supabase_realtime ADD TABLE 
    public.invoices, 
    public.products, 
    public.parties, 
    public.expenses, 
    public.notifications;

-- ==============================================================================
-- 2. CREATE STORAGE BUCKETS & ACCESS POLICIES
-- ==============================================================================
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES 
    ('invoices', 'invoices', true, 20971520, ARRAY['application/pdf', 'image/png', 'image/jpeg']),
    ('company-assets', 'company-assets', true, 10485760, ARRAY['image/png', 'image/jpeg', 'image/svg+xml', 'image/webp']),
    ('backups', 'backups', false, 52428800, NULL)
ON CONFLICT (id) DO UPDATE SET public = EXCLUDED.public;

-- Allow public uploads and downloads for invoices & logos
DROP POLICY IF EXISTS "Public Invoice Access" ON storage.objects;
CREATE POLICY "Public Invoice Access" ON storage.objects FOR ALL TO anon, authenticated
USING (bucket_id IN ('invoices', 'company-assets'))
WITH CHECK (bucket_id IN ('invoices', 'company-assets'));

-- Restrict database backups to authenticated sessions only
DROP POLICY IF EXISTS "Authenticated Backup Access" ON storage.objects;
CREATE POLICY "Authenticated Backup Access" ON storage.objects FOR ALL TO authenticated
USING (bucket_id = 'backups')
WITH CHECK (bucket_id = 'backups');

-- ==============================================================================
-- 3. SEED INITIAL BUSINESS (Fixes get_current_business_id() returning NULL)
-- ==============================================================================
INSERT INTO public.businesses (
    id, 
    name, 
    email, 
    phone, 
    state, 
    plan_tier, 
    subscription_status, 
    is_active, 
    ai_monthly_token_quota
)
VALUES (
    'a0000000-0000-0000-0000-000000000001'::uuid, 
    'GrowthEngine Enterprise', 
    'prajindezaa142@gmail.com', 
    '9876543210', 
    'Maharashtra', 
    'ENTERPRISE_AI', 
    'ACTIVE', 
    true, 
    2000000
)
ON CONFLICT (id) DO NOTHING;

-- ==============================================================================
-- 4. ENSURE RLS ALLOWS APP SYNC
-- ==============================================================================
DO $$
DECLARE
    tbl text;
BEGIN
    FOR tbl IN
        SELECT table_name
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name IN ('businesses', 'parties', 'products', 'invoices', 'expenses', 'payments', 'notifications', 'activity_audit_logs')
    LOOP
        EXECUTE format('DROP POLICY IF EXISTS "allow_app_sync_%s" ON public.%I', tbl, tbl);
        EXECUTE format('CREATE POLICY "allow_app_sync_%s" ON public.%I FOR ALL TO anon, authenticated USING (true) WITH CHECK (true)', tbl, tbl);
    END LOOP;
END $$;
