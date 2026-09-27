-- ==============================================================================
-- GrowthEngine ERP & POS — Safe Supabase PostgreSQL Schema & Storage Setup
-- Works on both fresh databases AND existing databases with existing tables.
-- ==============================================================================

-- 1. Enable UUID Extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==============================================================================
-- 1. businesses
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.businesses (
    id TEXT PRIMARY KEY DEFAULT uuid_generate_v4()::TEXT,
    name TEXT NOT NULL
);

ALTER TABLE public.businesses ADD COLUMN IF NOT EXISTS trade_name TEXT;
ALTER TABLE public.businesses ADD COLUMN IF NOT EXISTS owner_email TEXT;
ALTER TABLE public.businesses ADD COLUMN IF NOT EXISTS owner_phone TEXT;
ALTER TABLE public.businesses ADD COLUMN IF NOT EXISTS gstin TEXT;
ALTER TABLE public.businesses ADD COLUMN IF NOT EXISTS state TEXT;
ALTER TABLE public.businesses ADD COLUMN IF NOT EXISTS plan_tier TEXT DEFAULT 'GROWTH_PRO';
ALTER TABLE public.businesses ADD COLUMN IF NOT EXISTS subscription_status TEXT DEFAULT 'ACTIVE';
ALTER TABLE public.businesses ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;
ALTER TABLE public.businesses ADD COLUMN IF NOT EXISTS is_suspended BOOLEAN DEFAULT FALSE;
ALTER TABLE public.businesses ADD COLUMN IF NOT EXISTS ai_monthly_token_quota BIGINT DEFAULT 250000;
ALTER TABLE public.businesses ADD COLUMN IF NOT EXISTS ai_tokens_used BIGINT DEFAULT 0;
ALTER TABLE public.businesses ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT NOW();
ALTER TABLE public.businesses ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NOW();

-- ==============================================================================
-- 2. parties (Customers & Suppliers)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.parties (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL
);

ALTER TABLE public.parties ADD COLUMN IF NOT EXISTS business_id TEXT;
ALTER TABLE public.parties ADD COLUMN IF NOT EXISTS trade_name TEXT;
ALTER TABLE public.parties ADD COLUMN IF NOT EXISTS type TEXT DEFAULT 'CUSTOMER';
ALTER TABLE public.parties ADD COLUMN IF NOT EXISTS gstin TEXT;
ALTER TABLE public.parties ADD COLUMN IF NOT EXISTS pan_number TEXT;
ALTER TABLE public.parties ADD COLUMN IF NOT EXISTS phone TEXT;
ALTER TABLE public.parties ADD COLUMN IF NOT EXISTS email TEXT;
ALTER TABLE public.parties ADD COLUMN IF NOT EXISTS address TEXT;
ALTER TABLE public.parties ADD COLUMN IF NOT EXISTS state_name TEXT;
ALTER TABLE public.parties ADD COLUMN IF NOT EXISTS state_code TEXT;
ALTER TABLE public.parties ADD COLUMN IF NOT EXISTS credit_limit NUMERIC(15, 2) DEFAULT 0.0;
ALTER TABLE public.parties ADD COLUMN IF NOT EXISTS outstanding_balance NUMERIC(15, 2) DEFAULT 0.0;
ALTER TABLE public.parties ADD COLUMN IF NOT EXISTS payment_terms_days INTEGER DEFAULT 30;
ALTER TABLE public.parties ADD COLUMN IF NOT EXISTS overdue_days INTEGER DEFAULT 0;
ALTER TABLE public.parties ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT NOW();

-- ==============================================================================
-- 3. products (Inventory & Items Catalog)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.products (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL
);

ALTER TABLE public.products ADD COLUMN IF NOT EXISTS business_id TEXT;
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS sku TEXT;
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS hsn_code TEXT;
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS category TEXT;
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS unit TEXT DEFAULT 'Pcs';
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS purchase_price NUMERIC(15, 2) DEFAULT 0.0;
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS wholesale_price NUMERIC(15, 2) DEFAULT 0.0;
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS mrp NUMERIC(15, 2) DEFAULT 0.0;
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS gst_rate_percent NUMERIC(5, 2) DEFAULT 18.0;
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS current_stock NUMERIC(15, 2) DEFAULT 0.0;
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS min_reorder_level NUMERIC(15, 2) DEFAULT 5.0;
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS preferred_supplier TEXT;
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT NOW();

-- ==============================================================================
-- 4. invoices (GST Invoices, Quotations, POS Bills)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.invoices (
    id BIGSERIAL PRIMARY KEY,
    invoice_number TEXT NOT NULL
);

ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS business_id TEXT;
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS invoice_type TEXT DEFAULT 'TAX_INVOICE';
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS party_id BIGINT;
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS customer_name TEXT;
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS customer_gstin TEXT;
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS customer_phone TEXT;
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS customer_state TEXT;
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS is_interstate BOOLEAN DEFAULT FALSE;
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS date_epoch BIGINT;
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS due_date_epoch BIGINT;
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS items_summary TEXT;
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS items_count INTEGER DEFAULT 0;
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS subtotal NUMERIC(15, 2) DEFAULT 0.0;
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS discount NUMERIC(15, 2) DEFAULT 0.0;
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS cgst_amount NUMERIC(15, 2) DEFAULT 0.0;
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS sgst_amount NUMERIC(15, 2) DEFAULT 0.0;
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS igst_amount NUMERIC(15, 2) DEFAULT 0.0;
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS total_amount NUMERIC(15, 2) DEFAULT 0.0;
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS amount_paid NUMERIC(15, 2) DEFAULT 0.0;
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS balance_due NUMERIC(15, 2) DEFAULT 0.0;
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS payment_status TEXT DEFAULT 'PAID';
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS payment_mode TEXT DEFAULT 'UPI';
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS eway_bill_number TEXT;
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS notes TEXT;
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT NOW();

-- ==============================================================================
-- 5. expenses (Business Expenditure & GST ITC)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.expenses (
    id BIGSERIAL PRIMARY KEY,
    title TEXT NOT NULL
);

ALTER TABLE public.expenses ADD COLUMN IF NOT EXISTS business_id TEXT;
ALTER TABLE public.expenses ADD COLUMN IF NOT EXISTS category TEXT;
ALTER TABLE public.expenses ADD COLUMN IF NOT EXISTS amount NUMERIC(15, 2) DEFAULT 0.0;
ALTER TABLE public.expenses ADD COLUMN IF NOT EXISTS is_gst_claimable BOOLEAN DEFAULT FALSE;
ALTER TABLE public.expenses ADD COLUMN IF NOT EXISTS gst_amount NUMERIC(15, 2) DEFAULT 0.0;
ALTER TABLE public.expenses ADD COLUMN IF NOT EXISTS payment_mode TEXT DEFAULT 'UPI';
ALTER TABLE public.expenses ADD COLUMN IF NOT EXISTS vendor_name TEXT;
ALTER TABLE public.expenses ADD COLUMN IF NOT EXISTS date_epoch BIGINT;
ALTER TABLE public.expenses ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT NOW();

-- ==============================================================================
-- 6. payments (Ledger & Khata Transactions)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.payments (
    id BIGSERIAL PRIMARY KEY
);

ALTER TABLE public.payments ADD COLUMN IF NOT EXISTS business_id TEXT;
ALTER TABLE public.payments ADD COLUMN IF NOT EXISTS party_id BIGINT;
ALTER TABLE public.payments ADD COLUMN IF NOT EXISTS party_name TEXT;
ALTER TABLE public.payments ADD COLUMN IF NOT EXISTS invoice_number TEXT;
ALTER TABLE public.payments ADD COLUMN IF NOT EXISTS amount NUMERIC(15, 2) DEFAULT 0.0;
ALTER TABLE public.payments ADD COLUMN IF NOT EXISTS payment_mode TEXT DEFAULT 'UPI';
ALTER TABLE public.payments ADD COLUMN IF NOT EXISTS reference_number TEXT;
ALTER TABLE public.payments ADD COLUMN IF NOT EXISTS date_epoch BIGINT;
ALTER TABLE public.payments ADD COLUMN IF NOT EXISTS notes TEXT;
ALTER TABLE public.payments ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT NOW();

-- ==============================================================================
-- 7. manufacturing_orders (BOM Production)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.manufacturing_orders (
    id BIGSERIAL PRIMARY KEY,
    batch_code TEXT NOT NULL,
    finished_good_name TEXT NOT NULL
);

ALTER TABLE public.manufacturing_orders ADD COLUMN IF NOT EXISTS business_id TEXT;
ALTER TABLE public.manufacturing_orders ADD COLUMN IF NOT EXISTS target_quantity NUMERIC(15, 2) DEFAULT 1.0;
ALTER TABLE public.manufacturing_orders ADD COLUMN IF NOT EXISTS unit TEXT DEFAULT 'Pcs';
ALTER TABLE public.manufacturing_orders ADD COLUMN IF NOT EXISTS status TEXT DEFAULT 'PLANNED';
ALTER TABLE public.manufacturing_orders ADD COLUMN IF NOT EXISTS raw_materials_used_summary TEXT;
ALTER TABLE public.manufacturing_orders ADD COLUMN IF NOT EXISTS estimated_cost_per_unit NUMERIC(15, 2) DEFAULT 0.0;
ALTER TABLE public.manufacturing_orders ADD COLUMN IF NOT EXISTS start_date_epoch BIGINT;
ALTER TABLE public.manufacturing_orders ADD COLUMN IF NOT EXISTS target_date_epoch BIGINT;
ALTER TABLE public.manufacturing_orders ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT NOW();

-- ==============================================================================
-- 8. notifications (In-App Broadcasts)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.notifications (
    id TEXT PRIMARY KEY DEFAULT uuid_generate_v4()::TEXT,
    title TEXT NOT NULL,
    message TEXT NOT NULL
);

ALTER TABLE public.notifications ADD COLUMN IF NOT EXISTS notification_type TEXT DEFAULT 'broadcast';
ALTER TABLE public.notifications ADD COLUMN IF NOT EXISTS target_audience TEXT DEFAULT 'ALL_USERS';
ALTER TABLE public.notifications ADD COLUMN IF NOT EXISTS channel TEXT DEFAULT 'in_app';
ALTER TABLE public.notifications ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT NOW();

-- ==============================================================================
-- 9. activity_audit_logs (Security & Admin Activity Audit)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.activity_audit_logs (
    id TEXT PRIMARY KEY DEFAULT uuid_generate_v4()::TEXT,
    actor_email TEXT,
    action TEXT
);

ALTER TABLE public.activity_audit_logs ADD COLUMN IF NOT EXISTS actor_role TEXT;
ALTER TABLE public.activity_audit_logs ADD COLUMN IF NOT EXISTS target_entity TEXT;
ALTER TABLE public.activity_audit_logs ADD COLUMN IF NOT EXISTS details TEXT;
ALTER TABLE public.activity_audit_logs ADD COLUMN IF NOT EXISTS ip_address TEXT;
ALTER TABLE public.activity_audit_logs ADD COLUMN IF NOT EXISTS severity TEXT DEFAULT 'info';
ALTER TABLE public.activity_audit_logs ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT NOW();

-- ==============================================================================
-- PERFORMANCE INDEXES
-- ==============================================================================
CREATE INDEX IF NOT EXISTS idx_parties_business_id ON public.parties(business_id);
CREATE INDEX IF NOT EXISTS idx_products_business_id ON public.products(business_id);
CREATE INDEX IF NOT EXISTS idx_invoices_business_id ON public.invoices(business_id);
CREATE INDEX IF NOT EXISTS idx_expenses_business_id ON public.expenses(business_id);
CREATE INDEX IF NOT EXISTS idx_payments_business_id ON public.payments(business_id);

-- ==============================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ==============================================================================
ALTER TABLE public.businesses ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.parties ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.invoices ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.expenses ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.manufacturing_orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.activity_audit_logs ENABLE ROW LEVEL SECURITY;

DO $$
DECLARE
    tbl text;
BEGIN
    FOR tbl IN
        SELECT table_name
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name IN ('businesses', 'parties', 'products', 'invoices', 'expenses', 'payments', 'manufacturing_orders', 'notifications', 'activity_audit_logs')
    LOOP
        EXECUTE format('DROP POLICY IF EXISTS "allow_all_%s" ON public.%I', tbl, tbl);
        EXECUTE format('CREATE POLICY "allow_all_%s" ON public.%I FOR ALL TO anon, authenticated USING (true) WITH CHECK (true)', tbl, tbl);
    END LOOP;
END $$;

-- ==============================================================================
-- SUPABASE STORAGE BUCKETS (Invoices, Documents & Assets)
-- ==============================================================================
INSERT INTO storage.buckets (id, name, public, avif_autodetection, file_size_limit, allowed_mime_types)
VALUES 
    ('invoices', 'invoices', true, false, 20971520, ARRAY['application/pdf', 'image/png', 'image/jpeg']),
    ('company-assets', 'company-assets', true, false, 10485760, ARRAY['image/png', 'image/jpeg', 'image/svg+xml']),
    ('backups', 'backups', false, false, 52428800, NULL)
ON CONFLICT (id) DO UPDATE SET public = EXCLUDED.public;

DROP POLICY IF EXISTS "Public Invoice Access" ON storage.objects;
CREATE POLICY "Public Invoice Access"
ON storage.objects FOR ALL
TO anon, authenticated
USING (bucket_id IN ('invoices', 'company-assets'))
WITH CHECK (bucket_id IN ('invoices', 'company-assets'));

DROP POLICY IF EXISTS "Authenticated Backup Access" ON storage.objects;
CREATE POLICY "Authenticated Backup Access"
ON storage.objects FOR ALL
TO authenticated
USING (bucket_id = 'backups')
WITH CHECK (bucket_id = 'backups');

