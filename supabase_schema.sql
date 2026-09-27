-- ==============================================================================
-- GrowthEngine ERP & POS — Complete Supabase PostgreSQL Schema & Storage Setup
-- Run this entire script in Supabase Dashboard -> SQL Editor -> Click 'Run'
-- ==============================================================================

-- 1. Enable UUID Extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==============================================================================
-- TABLE 1: businesses (Core MSME Business Profiles & Quotas)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.businesses (
    id TEXT PRIMARY KEY DEFAULT uuid_generate_v4()::TEXT,
    name TEXT NOT NULL,
    trade_name TEXT,
    owner_email TEXT,
    owner_phone TEXT,
    gstin TEXT,
    state TEXT,
    plan_tier TEXT DEFAULT 'GROWTH_PRO',
    subscription_status TEXT DEFAULT 'ACTIVE',
    is_active BOOLEAN DEFAULT TRUE,
    is_suspended BOOLEAN DEFAULT FALSE,
    ai_monthly_token_quota BIGINT DEFAULT 250000,
    ai_tokens_used BIGINT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ==============================================================================
-- TABLE 2: parties (Customers & Suppliers)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.parties (
    id BIGSERIAL PRIMARY KEY,
    business_id TEXT,
    name TEXT NOT NULL,
    trade_name TEXT,
    type TEXT DEFAULT 'CUSTOMER', -- 'CUSTOMER', 'SUPPLIER'
    gstin TEXT,
    pan_number TEXT,
    phone TEXT,
    email TEXT,
    address TEXT,
    state_name TEXT,
    state_code TEXT,
    credit_limit NUMERIC(15, 2) DEFAULT 0.0,
    outstanding_balance NUMERIC(15, 2) DEFAULT 0.0,
    payment_terms_days INTEGER DEFAULT 30,
    overdue_days INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ==============================================================================
-- TABLE 3: products (Inventory & Items Catalog)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.products (
    id BIGSERIAL PRIMARY KEY,
    business_id TEXT,
    name TEXT NOT NULL,
    sku TEXT,
    hsn_code TEXT,
    category TEXT,
    unit TEXT DEFAULT 'Pcs',
    purchase_price NUMERIC(15, 2) DEFAULT 0.0,
    wholesale_price NUMERIC(15, 2) DEFAULT 0.0,
    mrp NUMERIC(15, 2) DEFAULT 0.0,
    gst_rate_percent NUMERIC(5, 2) DEFAULT 18.0,
    current_stock NUMERIC(15, 2) DEFAULT 0.0,
    min_reorder_level NUMERIC(15, 2) DEFAULT 5.0,
    preferred_supplier TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ==============================================================================
-- TABLE 4: invoices (GST Invoices, Proformas, Quotations, POS Bills)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.invoices (
    id BIGSERIAL PRIMARY KEY,
    business_id TEXT,
    invoice_number TEXT NOT NULL,
    invoice_type TEXT DEFAULT 'TAX_INVOICE',
    party_id BIGINT,
    customer_name TEXT,
    customer_gstin TEXT,
    customer_phone TEXT,
    customer_state TEXT,
    is_interstate BOOLEAN DEFAULT FALSE,
    date_epoch BIGINT,
    due_date_epoch BIGINT,
    items_summary TEXT,
    items_count INTEGER DEFAULT 0,
    subtotal NUMERIC(15, 2) DEFAULT 0.0,
    discount NUMERIC(15, 2) DEFAULT 0.0,
    cgst_amount NUMERIC(15, 2) DEFAULT 0.0,
    sgst_amount NUMERIC(15, 2) DEFAULT 0.0,
    igst_amount NUMERIC(15, 2) DEFAULT 0.0,
    total_amount NUMERIC(15, 2) DEFAULT 0.0,
    amount_paid NUMERIC(15, 2) DEFAULT 0.0,
    balance_due NUMERIC(15, 2) DEFAULT 0.0,
    payment_status TEXT DEFAULT 'PAID', -- 'PAID', 'PARTIAL', 'UNPAID', 'OVERDUE'
    payment_mode TEXT DEFAULT 'UPI',
    eway_bill_number TEXT,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ==============================================================================
-- TABLE 5: expenses (Business Expenditure & GST ITC Tracking)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.expenses (
    id BIGSERIAL PRIMARY KEY,
    business_id TEXT,
    title TEXT NOT NULL,
    category TEXT,
    amount NUMERIC(15, 2) DEFAULT 0.0,
    is_gst_claimable BOOLEAN DEFAULT FALSE,
    gst_amount NUMERIC(15, 2) DEFAULT 0.0,
    payment_mode TEXT DEFAULT 'UPI',
    vendor_name TEXT,
    date_epoch BIGINT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ==============================================================================
-- TABLE 6: payments (Ledger & Khata Payment Transactions)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.payments (
    id BIGSERIAL PRIMARY KEY,
    business_id TEXT,
    party_id BIGINT,
    party_name TEXT,
    invoice_number TEXT,
    amount NUMERIC(15, 2) DEFAULT 0.0,
    payment_mode TEXT DEFAULT 'UPI',
    reference_number TEXT,
    date_epoch BIGINT,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ==============================================================================
-- TABLE 7: manufacturing_orders (BOM Production & Batch Tracking)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.manufacturing_orders (
    id BIGSERIAL PRIMARY KEY,
    business_id TEXT,
    batch_code TEXT NOT NULL,
    finished_good_name TEXT NOT NULL,
    target_quantity NUMERIC(15, 2) DEFAULT 1.0,
    unit TEXT DEFAULT 'Pcs',
    status TEXT DEFAULT 'PLANNED',
    raw_materials_used_summary TEXT,
    estimated_cost_per_unit NUMERIC(15, 2) DEFAULT 0.0,
    start_date_epoch BIGINT,
    target_date_epoch BIGINT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ==============================================================================
-- TABLE 8: notifications (Broadcast & Targeted In-App Notifications)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.notifications (
    id TEXT PRIMARY KEY DEFAULT uuid_generate_v4()::TEXT,
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    notification_type TEXT DEFAULT 'broadcast',
    target_audience TEXT DEFAULT 'ALL_USERS',
    channel TEXT DEFAULT 'in_app',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ==============================================================================
-- TABLE 9: activity_audit_logs (Security & Admin Activity Audit)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.activity_audit_logs (
    id TEXT PRIMARY KEY DEFAULT uuid_generate_v4()::TEXT,
    actor_email TEXT,
    actor_role TEXT,
    action TEXT,
    target_entity TEXT,
    details TEXT,
    ip_address TEXT,
    severity TEXT DEFAULT 'info',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ==============================================================================
-- PERFORMANCE INDEXES
-- ==============================================================================
CREATE INDEX IF NOT EXISTS idx_parties_business_id ON public.parties(business_id);
CREATE INDEX IF NOT EXISTS idx_products_business_id ON public.products(business_id);
CREATE INDEX IF NOT EXISTS idx_invoices_business_id ON public.invoices(business_id);
CREATE INDEX IF NOT EXISTS idx_invoices_invoice_number ON public.invoices(invoice_number);
CREATE INDEX IF NOT EXISTS idx_expenses_business_id ON public.expenses(business_id);
CREATE INDEX IF NOT EXISTS idx_payments_business_id ON public.payments(business_id);

-- ==============================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- Ensures anon and authenticated clients can sync records seamlessly
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

-- Allow full access to anon and authenticated roles
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
-- 1. Create storage buckets
INSERT INTO storage.buckets (id, name, public, avif_autodetection, file_size_limit, allowed_mime_types)
VALUES 
    ('invoices', 'invoices', true, false, 20971520, ARRAY['application/pdf', 'image/png', 'image/jpeg']),
    ('company-assets', 'company-assets', true, false, 10485760, ARRAY['image/png', 'image/jpeg', 'image/svg+xml']),
    ('backups', 'backups', false, false, 52428800, NULL)
ON CONFLICT (id) DO UPDATE SET public = EXCLUDED.public;

-- 2. Storage RLS Policies (Allow read/write access for public/app usage)
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

-- ==============================================================================
-- SEED INITIAL RECORD (So Admin Analytics and Sync test query returns 200 OK)
-- ==============================================================================
INSERT INTO public.businesses (id, name, trade_name, owner_email, gstin, state, plan_tier, subscription_status, is_active, ai_monthly_token_quota)
VALUES (
    'biz_initial_default',
    'GrowthEngine Enterprise',
    'GrowthEngine MSME',
    'prajindezaa142@gmail.com',
    '27AAAAA0000A1Z5',
    'Maharashtra',
    'ENTERPRISE_AI',
    'ACTIVE',
    true,
    2000000
)
ON CONFLICT (id) DO NOTHING;
