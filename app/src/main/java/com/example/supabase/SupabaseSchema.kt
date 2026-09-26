package com.example.supabase

object SupabaseSchema {

    const val CLEAN_SLATE_RESET_SQL: String = """-- =============================================================================
-- STEP 1: CLEAN SLATE RESET SCRIPT (Run this first in SQL Editor)
-- =============================================================================

-- Drop trigger on auth.users
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;

-- Drop all public tables with CASCADE
DROP TABLE IF EXISTS public.user_notifications CASCADE;
DROP TABLE IF EXISTS public.notifications CASCADE;
DROP TABLE IF EXISTS public.support_tickets CASCADE;
DROP TABLE IF EXISTS public.activity_audit_logs CASCADE;
DROP TABLE IF EXISTS public.system_settings CASCADE;
DROP TABLE IF EXISTS public.manufacturing_batches CASCADE;
DROP TABLE IF EXISTS public.expenses CASCADE;
DROP TABLE IF EXISTS public.invoices CASCADE;
DROP TABLE IF EXISTS public.products CASCADE;
DROP TABLE IF EXISTS public.parties CASCADE;
DROP TABLE IF EXISTS public.ai_quota_tracking CASCADE;
DROP TABLE IF EXISTS public.ai_usage_logs CASCADE;
DROP TABLE IF EXISTS public.payments CASCADE;
DROP TABLE IF EXISTS public.subscriptions CASCADE;
DROP TABLE IF EXISTS public.subscription_plans CASCADE;
DROP TABLE IF EXISTS public.admin_users CASCADE;
DROP TABLE IF EXISTS public.profiles CASCADE;
DROP TABLE IF EXISTS public.businesses CASCADE;

-- Drop custom procedures & functions
DROP FUNCTION IF EXISTS public.handle_new_user_signup() CASCADE;
DROP FUNCTION IF EXISTS public.handle_ai_usage_increment() CASCADE;
DROP FUNCTION IF EXISTS public.is_admin() CASCADE;
DROP FUNCTION IF EXISTS public.is_super_admin() CASCADE;
DROP FUNCTION IF EXISTS public.get_current_business_id() CASCADE;

-- Drop enum types
DROP TYPE IF EXISTS user_role CASCADE;
DROP TYPE IF EXISTS subscription_tier_type CASCADE;
DROP TYPE IF EXISTS subscription_status_type CASCADE;
DROP TYPE IF EXISTS billing_cycle_type CASCADE;
DROP TYPE IF EXISTS payment_gateway_type CASCADE;
DROP TYPE IF EXISTS payment_status_type CASCADE;
DROP TYPE IF EXISTS notification_type_enum CASCADE;
DROP TYPE IF EXISTS notification_channel_enum CASCADE;
DROP TYPE IF EXISTS audit_severity_enum CASCADE;
"""

    const val SQL_SCRIPT: String = """-- =============================================================================
-- STEP 2: GROWTHENGINE & ADMIN SYSTEM — COMPLETE PRODUCTION SCHEMA
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. ENUMS
CREATE TYPE user_role AS ENUM ('super_admin', 'platform_admin', 'support_admin', 'business_owner', 'business_manager', 'staff_cashier', 'accountant');
CREATE TYPE subscription_tier_type AS ENUM ('starter_free', 'growth_pro', 'enterprise_munim', 'custom_enterprise');
CREATE TYPE subscription_status_type AS ENUM ('trial', 'active', 'pending', 'expired', 'cancelled', 'failed', 'paused');
CREATE TYPE billing_cycle_type AS ENUM ('monthly', 'quarterly', 'annual');
CREATE TYPE payment_gateway_type AS ENUM ('razorpay', 'stripe', 'upi_autopay', 'manual_neft');
CREATE TYPE payment_status_type AS ENUM ('created', 'authorized', 'captured', 'failed', 'refunded', 'pending');
CREATE TYPE notification_type_enum AS ENUM ('broadcast', 'targeted', 'system_alert', 'billing_reminder', 'ai_quota_warning');
CREATE TYPE notification_channel_enum AS ENUM ('in_app', 'push', 'email', 'whatsapp');
CREATE TYPE audit_severity_enum AS ENUM ('info', 'warn', 'critical', 'audit');

-- 2. BUSINESSES & PROFILES
CREATE TABLE public.businesses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    trade_name VARCHAR(255),
    gstin VARCHAR(15),
    pan_number VARCHAR(10),
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    state_name VARCHAR(100) DEFAULT 'Maharashtra',
    state_code VARCHAR(10) DEFAULT '27',
    address TEXT,
    city VARCHAR(100),
    pincode VARCHAR(10),
    logo_url TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    is_suspended BOOLEAN DEFAULT FALSE,
    suspension_reason TEXT,
    ai_monthly_token_quota BIGINT DEFAULT 250000,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    business_id UUID REFERENCES public.businesses(id) ON DELETE SET NULL,
    role user_role DEFAULT 'business_owner',
    full_name VARCHAR(255) DEFAULT 'User',
    email VARCHAR(255) DEFAULT '',
    phone VARCHAR(20),
    avatar_url TEXT,
    is_admin BOOLEAN DEFAULT FALSE,
    is_super_admin BOOLEAN DEFAULT FALSE,
    last_login_at TIMESTAMPTZ,
    status VARCHAR(50) DEFAULT 'active',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE public.admin_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    admin_role user_role DEFAULT 'platform_admin',
    assigned_by UUID REFERENCES public.profiles(id),
    permissions_json JSONB DEFAULT '{"manage_users": true, "manage_subscriptions": true, "manage_ai_quotas": true, "manage_broadcasts": true, "manage_system": true}'::jsonb,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT unique_admin_user UNIQUE (user_id)
);

-- 3. PLANS, SUBSCRIPTIONS & PAYMENTS
CREATE TABLE public.subscription_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tier subscription_tier_type NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    tagline TEXT,
    monthly_price_inr NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    annual_price_per_month_inr NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    max_invoices_per_month INT DEFAULT 50,
    max_skus INT DEFAULT 100,
    max_staff_seats INT DEFAULT 1,
    ai_monthly_tokens BIGINT DEFAULT 50000,
    ai_copilot_enabled BOOLEAN DEFAULT FALSE,
    manufacturing_enabled BOOLEAN DEFAULT FALSE,
    multi_warehouse_enabled BOOLEAN DEFAULT FALSE,
    razorpay_monthly_plan_id VARCHAR(100),
    razorpay_annual_plan_id VARCHAR(100),
    features_list JSONB DEFAULT '[]'::jsonb,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE public.subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID NOT NULL REFERENCES public.businesses(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    plan_tier subscription_tier_type NOT NULL DEFAULT 'growth_pro',
    status subscription_status_type NOT NULL DEFAULT 'active',
    billing_cycle billing_cycle_type NOT NULL DEFAULT 'annual',
    current_period_start TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    current_period_end TIMESTAMPTZ NOT NULL DEFAULT (NOW() + INTERVAL '365 days'),
    trial_start TIMESTAMPTZ,
    trial_end TIMESTAMPTZ,
    cancel_at_period_end BOOLEAN DEFAULT FALSE,
    cancelled_at TIMESTAMPTZ,
    paused_at TIMESTAMPTZ,
    auto_renew BOOLEAN DEFAULT TRUE,
    payment_method_summary VARCHAR(255) DEFAULT 'UPI Autopay',
    razorpay_customer_id VARCHAR(100),
    razorpay_subscription_id VARCHAR(100),
    metadata JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE public.payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID NOT NULL REFERENCES public.businesses(id) ON DELETE CASCADE,
    user_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    subscription_id UUID REFERENCES public.subscriptions(id) ON DELETE SET NULL,
    amount_inr NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    gst_amount_inr NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    total_amount_inr NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(10) DEFAULT 'INR',
    gateway payment_gateway_type DEFAULT 'razorpay',
    gateway_payment_id VARCHAR(100) UNIQUE,
    gateway_order_id VARCHAR(100),
    gateway_signature VARCHAR(255),
    status payment_status_type NOT NULL DEFAULT 'captured',
    payment_method VARCHAR(100) DEFAULT 'upi',
    failure_reason TEXT,
    receipt_number VARCHAR(100),
    invoice_url TEXT,
    captured_at TIMESTAMPTZ DEFAULT NOW(),
    metadata JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. AI USAGE & QUOTAS
CREATE TABLE public.ai_usage_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID NOT NULL REFERENCES public.businesses(id) ON DELETE CASCADE,
    user_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    feature_name VARCHAR(100) NOT NULL DEFAULT 'Copilot',
    model_name VARCHAR(100) NOT NULL DEFAULT 'Gemini 2.5 Flash',
    prompt_tokens INT NOT NULL DEFAULT 0,
    completion_tokens INT NOT NULL DEFAULT 0,
    total_tokens INT NOT NULL DEFAULT 0,
    estimated_cost_inr NUMERIC(8, 4) NOT NULL DEFAULT 0.0000,
    latency_ms INT DEFAULT 400,
    status VARCHAR(50) DEFAULT 'success',
    error_message TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE public.ai_quota_tracking (
    business_id UUID PRIMARY KEY REFERENCES public.businesses(id) ON DELETE CASCADE,
    monthly_quota_tokens BIGINT NOT NULL DEFAULT 250000,
    consumed_tokens_current_month BIGINT NOT NULL DEFAULT 0,
    bonus_tokens_allocated BIGINT NOT NULL DEFAULT 0,
    is_throttled BOOLEAN DEFAULT FALSE,
    last_reset_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. PARTIES, PRODUCTS, INVOICES, EXPENSES & MANUFACTURING
CREATE TABLE public.parties (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID NOT NULL REFERENCES public.businesses(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    trade_name VARCHAR(255),
    type VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER',
    gstin VARCHAR(15),
    pan_number VARCHAR(10),
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(255),
    address TEXT,
    state_name VARCHAR(100) DEFAULT 'Maharashtra',
    state_code VARCHAR(10) DEFAULT '27',
    credit_limit NUMERIC(12, 2) DEFAULT 100000.00,
    outstanding_balance NUMERIC(12, 2) DEFAULT 0.00,
    payment_terms_days INT DEFAULT 30,
    overdue_days INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE public.products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID NOT NULL REFERENCES public.businesses(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    sku VARCHAR(100) NOT NULL,
    hsn_code VARCHAR(50),
    category VARCHAR(100) DEFAULT 'General',
    unit VARCHAR(50) DEFAULT 'Pcs',
    purchase_price NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    wholesale_price NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    mrp NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    gst_rate_percent NUMERIC(5, 2) NOT NULL DEFAULT 18.00,
    current_stock NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    min_reorder_level NUMERIC(12, 2) DEFAULT 10.00,
    preferred_supplier VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE public.invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID NOT NULL REFERENCES public.businesses(id) ON DELETE CASCADE,
    party_id UUID REFERENCES public.parties(id) ON DELETE SET NULL,
    invoice_number VARCHAR(100) NOT NULL,
    invoice_type VARCHAR(50) DEFAULT 'TAX_INVOICE',
    customer_name VARCHAR(255) NOT NULL,
    customer_gstin VARCHAR(15),
    customer_phone VARCHAR(20),
    customer_state VARCHAR(100),
    is_interstate BOOLEAN DEFAULT FALSE,
    invoice_date TIMESTAMPTZ DEFAULT NOW(),
    due_date TIMESTAMPTZ DEFAULT NOW(),
    items_summary TEXT,
    items_count INT DEFAULT 1,
    subtotal NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    discount NUMERIC(12, 2) DEFAULT 0.00,
    cgst_amount NUMERIC(12, 2) DEFAULT 0.00,
    sgst_amount NUMERIC(12, 2) DEFAULT 0.00,
    igst_amount NUMERIC(12, 2) DEFAULT 0.00,
    total_amount NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    amount_paid NUMERIC(12, 2) DEFAULT 0.00,
    balance_due NUMERIC(12, 2) DEFAULT 0.00,
    payment_status VARCHAR(50) DEFAULT 'PAID',
    payment_mode VARCHAR(50) DEFAULT 'UPI',
    eway_bill_number VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE public.expenses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID NOT NULL REFERENCES public.businesses(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    is_gst_claimable BOOLEAN DEFAULT FALSE,
    gst_amount NUMERIC(12, 2) DEFAULT 0.00,
    payment_mode VARCHAR(50) DEFAULT 'UPI',
    vendor_name VARCHAR(255),
    expense_date TIMESTAMPTZ DEFAULT NOW(),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE public.manufacturing_batches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID NOT NULL REFERENCES public.businesses(id) ON DELETE CASCADE,
    batch_code VARCHAR(100) NOT NULL,
    finished_good_name VARCHAR(255) NOT NULL,
    target_quantity NUMERIC(12, 2) NOT NULL DEFAULT 1.00,
    unit VARCHAR(50) DEFAULT 'Pcs',
    status VARCHAR(50) DEFAULT 'IN_PRODUCTION',
    raw_materials_summary TEXT,
    estimated_cost_per_unit NUMERIC(12, 2) DEFAULT 0.00,
    start_date TIMESTAMPTZ DEFAULT NOW(),
    target_date TIMESTAMPTZ DEFAULT NOW(),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 6. NOTIFICATIONS, AUDIT & SUPPORT
CREATE TABLE public.notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    notification_type notification_type_enum DEFAULT 'broadcast',
    target_audience VARCHAR(100) DEFAULT 'ALL_USERS',
    target_business_id UUID REFERENCES public.businesses(id) ON DELETE CASCADE,
    channel notification_channel_enum DEFAULT 'in_app',
    scheduled_at TIMESTAMPTZ DEFAULT NOW(),
    sent_at TIMESTAMPTZ DEFAULT NOW(),
    is_sent BOOLEAN DEFAULT TRUE,
    created_by UUID REFERENCES public.profiles(id),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE public.user_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id UUID NOT NULL REFERENCES public.notifications(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT unique_user_notification UNIQUE (notification_id, user_id)
);

CREATE TABLE public.activity_audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID REFERENCES public.businesses(id) ON DELETE SET NULL,
    user_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    actor_email VARCHAR(255) NOT NULL,
    actor_role VARCHAR(50) DEFAULT 'user',
    action VARCHAR(100) NOT NULL,
    target_entity VARCHAR(100),
    details TEXT,
    ip_address VARCHAR(50),
    severity audit_severity_enum DEFAULT 'info',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE public.support_tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_number VARCHAR(50) NOT NULL UNIQUE,
    business_id UUID REFERENCES public.businesses(id) ON DELETE CASCADE,
    user_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    business_name VARCHAR(255),
    user_email VARCHAR(255),
    subject VARCHAR(255) NOT NULL,
    category VARCHAR(100) DEFAULT 'Billing & Payments',
    priority VARCHAR(50) DEFAULT 'MEDIUM',
    status VARCHAR(50) DEFAULT 'OPEN',
    assigned_admin_id UUID REFERENCES public.profiles(id),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE public.system_settings (
    key VARCHAR(100) PRIMARY KEY,
    value JSONB NOT NULL,
    description TEXT,
    is_public BOOLEAN DEFAULT FALSE,
    updated_by UUID REFERENCES public.profiles(id),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 7. INDEXES
CREATE INDEX idx_businesses_active ON public.businesses(is_active);
CREATE INDEX idx_profiles_business_id ON public.profiles(business_id);
CREATE INDEX idx_profiles_role ON public.profiles(role);
CREATE INDEX idx_subscriptions_business ON public.subscriptions(business_id);
CREATE INDEX idx_payments_business ON public.payments(business_id);
CREATE INDEX idx_ai_usage_business ON public.ai_usage_logs(business_id, created_at DESC);
CREATE INDEX idx_invoices_business ON public.invoices(business_id, invoice_date DESC);
CREATE INDEX idx_parties_business ON public.parties(business_id);
CREATE INDEX idx_products_business ON public.products(business_id);
CREATE INDEX idx_audit_logs_business ON public.activity_audit_logs(business_id, created_at DESC);

-- 8. HELPER SECURITY FUNCTIONS
CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.profiles p
        WHERE p.id = auth.uid() AND (p.is_admin = TRUE OR p.is_super_admin = TRUE OR p.role IN ('super_admin', 'platform_admin', 'support_admin'))
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION public.is_super_admin()
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.profiles p
        WHERE p.id = auth.uid() AND (p.is_super_admin = TRUE OR p.role = 'super_admin')
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION public.get_current_business_id()
RETURNS UUID AS $$
    SELECT business_id FROM public.profiles WHERE id = auth.uid() LIMIT 1;
$$ LANGUAGE sql SECURITY DEFINER STABLE;

-- 9. ROW LEVEL SECURITY (RLS) POLICIES
ALTER TABLE public.businesses ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.admin_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.subscription_plans ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.subscriptions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ai_usage_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ai_quota_tracking ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.parties ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.invoices ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.expenses ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.manufacturing_batches ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.activity_audit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.support_tickets ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.system_settings ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can read own profile or admins read all" ON public.profiles FOR SELECT USING (auth.uid() = id OR public.is_admin());
CREATE POLICY "Users can update own profile or admins update all" ON public.profiles FOR UPDATE USING (auth.uid() = id OR public.is_admin());
CREATE POLICY "Tenant read business or admin read all" ON public.businesses FOR SELECT USING (id = public.get_current_business_id() OR public.is_admin());
CREATE POLICY "Tenant update own business or admin update all" ON public.businesses FOR UPDATE USING (id = public.get_current_business_id() OR public.is_admin());
CREATE POLICY "Admins can view admin list" ON public.admin_users FOR SELECT USING (public.is_admin());
CREATE POLICY "Super Admins can modify admin list" ON public.admin_users FOR ALL USING (public.is_super_admin());
CREATE POLICY "Anyone can read active plans" ON public.subscription_plans FOR SELECT USING (is_active = TRUE OR public.is_admin());
CREATE POLICY "Admins can manage plans" ON public.subscription_plans FOR ALL USING (public.is_admin());
CREATE POLICY "Tenant view own subscription or admin view all" ON public.subscriptions FOR SELECT USING (business_id = public.get_current_business_id() OR public.is_admin());
CREATE POLICY "Admins or webhooks can manage subscriptions" ON public.subscriptions FOR ALL USING (public.is_admin());
CREATE POLICY "Tenant view own payments or admin view all" ON public.payments FOR SELECT USING (business_id = public.get_current_business_id() OR public.is_admin());
CREATE POLICY "Admins can manage payments" ON public.payments FOR ALL USING (public.is_admin());
CREATE POLICY "Tenant view own AI usage or admin view all" ON public.ai_usage_logs FOR SELECT USING (business_id = public.get_current_business_id() OR public.is_admin());
CREATE POLICY "Tenant insert AI usage logs" ON public.ai_usage_logs FOR INSERT WITH CHECK (business_id = public.get_current_business_id() OR public.is_admin());
CREATE POLICY "Tenant view own AI quota or admin view all" ON public.ai_quota_tracking FOR SELECT USING (business_id = public.get_current_business_id() OR public.is_admin());
CREATE POLICY "Admins can update AI quotas" ON public.ai_quota_tracking FOR ALL USING (public.is_admin());
CREATE POLICY "Tenant isolation for invoices" ON public.invoices FOR ALL USING (business_id = public.get_current_business_id() OR public.is_admin());
CREATE POLICY "Tenant isolation for products" ON public.products FOR ALL USING (business_id = public.get_current_business_id() OR public.is_admin());
CREATE POLICY "Tenant isolation for parties" ON public.parties FOR ALL USING (business_id = public.get_current_business_id() OR public.is_admin());
CREATE POLICY "Tenant isolation for expenses" ON public.expenses FOR ALL USING (business_id = public.get_current_business_id() OR public.is_admin());
CREATE POLICY "Tenant isolation for manufacturing" ON public.manufacturing_batches FOR ALL USING (business_id = public.get_current_business_id() OR public.is_admin());
CREATE POLICY "Users read broadcast or targeted notifications" ON public.notifications FOR SELECT USING (target_audience = 'ALL_USERS' OR target_business_id = public.get_current_business_id() OR public.is_admin());
CREATE POLICY "Admins can create notifications" ON public.notifications FOR INSERT WITH CHECK (public.is_admin());
CREATE POLICY "Users manage own notification read states" ON public.user_notifications FOR ALL USING (user_id = auth.uid() OR public.is_admin());
CREATE POLICY "Admins view all audit logs" ON public.activity_audit_logs FOR SELECT USING (public.is_admin());
CREATE POLICY "System and users insert audit logs" ON public.activity_audit_logs FOR INSERT WITH CHECK (TRUE);
CREATE POLICY "Users read own tickets or admins read all" ON public.support_tickets FOR SELECT USING (business_id = public.get_current_business_id() OR public.is_admin());
CREATE POLICY "Users insert tickets" ON public.support_tickets FOR INSERT WITH CHECK (business_id = public.get_current_business_id() OR public.is_admin());
CREATE POLICY "Admins update support tickets" ON public.support_tickets FOR UPDATE USING (public.is_admin());
CREATE POLICY "Public settings read" ON public.system_settings FOR SELECT USING (is_public = TRUE OR public.is_admin());
CREATE POLICY "Admins manage settings" ON public.system_settings FOR ALL USING (public.is_admin());

-- 10. TRIGGERS
CREATE OR REPLACE FUNCTION public.handle_ai_usage_increment()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.ai_quota_tracking (business_id, consumed_tokens_current_month)
    VALUES (NEW.business_id, NEW.total_tokens)
    ON CONFLICT (business_id) DO UPDATE
    SET consumed_tokens_current_month = public.ai_quota_tracking.consumed_tokens_current_month + NEW.total_tokens,
        updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER trg_ai_usage_increment
    AFTER INSERT ON public.ai_usage_logs
    FOR EACH ROW EXECUTE FUNCTION public.handle_ai_usage_increment();

CREATE OR REPLACE FUNCTION public.handle_new_user_signup()
RETURNS TRIGGER AS $$
DECLARE
    new_biz_id UUID;
    user_full_name TEXT;
BEGIN
    user_full_name := COALESCE(NEW.raw_user_meta_data->>'full_name', 'Business Owner');
    
    INSERT INTO public.businesses (name, trade_name, email, phone)
    VALUES (
        COALESCE(NEW.raw_user_meta_data->>'business_name', 'My Enterprise'),
        COALESCE(NEW.raw_user_meta_data->>'trade_name', 'My Enterprise'),
        NEW.email,
        COALESCE(NEW.phone, '+91 9800000000')
    ) RETURNING id INTO new_biz_id;

    INSERT INTO public.profiles (id, business_id, role, full_name, email, phone)
    VALUES (
        NEW.id,
        new_biz_id,
        'business_owner',
        user_full_name,
        NEW.email,
        NEW.phone
    );

    INSERT INTO public.ai_quota_tracking (business_id, monthly_quota_tokens)
    VALUES (new_biz_id, 250000);

    INSERT INTO public.subscriptions (
        business_id,
        user_id,
        plan_tier,
        status,
        billing_cycle,
        current_period_start,
        current_period_end,
        trial_start,
        trial_end
    ) VALUES (
        new_biz_id,
        NEW.id,
        'growth_pro',
        'trial',
        'annual',
        NOW(),
        NOW() + INTERVAL '14 days',
        NOW(),
        NOW() + INTERVAL '14 days'
    );

    INSERT INTO public.activity_audit_logs (business_id, user_id, actor_email, action, target_entity, details)
    VALUES (new_biz_id, NEW.id, NEW.email, 'USER_SIGNUP', 'USER', 'New business registered with 14-day Pro trial');

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user_signup();

-- 11. SEED DEFAULT DATA
INSERT INTO public.subscription_plans (tier, name, tagline, monthly_price_inr, annual_price_per_month_inr, max_invoices_per_month, max_skus, max_staff_seats, ai_monthly_tokens, ai_copilot_enabled, manufacturing_enabled, features_list)
VALUES 
    ('starter_free', 'Starter Free', 'Essential GST billing & counter sales for small retail', 0.00, 0.00, 50, 100, 1, 10000, FALSE, FALSE, '["Up to 50 GST invoices/mo", "Counter POS", "100 SKUs", "Basic Khata Ledger"]'::jsonb),
    ('growth_pro', 'Growth Pro', 'Complete ledger, stock & automated WhatsApp collections', 799.00, 599.00, 1000000, 10000, 3, 250000, TRUE, FALSE, '["Unlimited GST Invoices", "Fast POS with Barcode", "10,000 SKUs", "Automated WhatsApp Reminders", "Live Supabase Cloud Sync", "Input Tax Credit Reports", "3 Staff Logins"]'::jsonb),
    ('enterprise_munim', 'Enterprise Munim', 'AI Copilot Munim, Manufacturing BOM & multi-user teams', 1999.00, 1499.00, 1000000, 100000, 999, 2000000, TRUE, TRUE, '["All Growth Pro Features", "AI Business Copilot (Hindi/English)", "Manufacturing & Raw Materials (BOM)", "Multi-Godown / Warehouses", "MSMED 45-day Interest Tracker", "Unlimited Staff Seats", "24/7 Priority Support"]'::jsonb)
ON CONFLICT (tier) DO NOTHING;

INSERT INTO public.system_settings (key, value, description, is_public)
VALUES 
    ('maintenance_mode', '{"enabled": false, "message": "GrowthEngine is running normally."}'::jsonb, 'Global maintenance toggle', true),
    ('global_announcement', '{"banner": "⚡ GrowthEngine v2.4 Live: High-speed GST E-Invoice & AI Copilot released.", "active": true}'::jsonb, 'System announcement banner', true),
    ('razorpay_gateway_config', '{"currency": "INR", "webhook_enabled": true, "autopay_supported": true}'::jsonb, 'Razorpay configuration details', false)
ON CONFLICT (key) DO NOTHING;
"""

    const val AUTH_CONFIG_DOC: String = """
# Supabase Authentication Setup
1. Enable Email/Password auth and Phone SMS OTP (for Indian +91 numbers).
2. Set JWT Expiry to 3600 seconds (1 hour) with Refresh Token rotation.
3. Configure Redirect URL to: `https://growthengine.in/auth/callback` or Android Deep Link `growthengine://auth/callback`.
4. Role claims automatically populated in JWT `app_metadata` or queried from `public.profiles`.

# Storage Buckets Setup
Create the following Supabase Storage buckets with appropriate policies:
1. `invoices-pdf` (Private): Stores generated PDF invoices and receipts. Read allowed only by tenant (`business_id`) and admin.
2. `business-logos` (Public): Stores business brand logos and company seals for GST invoice headers.
3. `receipt-attachments` (Private): Stores expense receipts, purchase bills, and vendor challans.
4. `exports-backup` (Private): Stores JSON and Excel ledger backup snapshots.

# Supabase Edge Functions
Deploy these 3 TypeScript Edge Functions in Supabase CLI:
1. `razorpay-webhook`:
   - Endpoint: `https://<PROJECT_ID>.supabase.co/functions/v1/razorpay-webhook`
   - Handles `payment.captured`, `subscription.charged`, `subscription.cancelled`, `payment.failed`.
   - Verifies `X-Razorpay-Signature` with webhook secret.
   - Inserts into `public.payments` and updates `public.subscriptions`.
2. `ai-token-meter`:
   - Validates business token balance before querying Gemini 2.5 Flash.
   - Logs token usage into `public.ai_usage_logs`.
3. `send-broadcast-notification`:
   - Dispatches FCM push notifications to targeted user segments.

# Environment Variables (.env)
- `SUPABASE_URL`: `https://wrcuondcuuwkqcgtrigz.supabase.co`
- `SUPABASE_ANON_KEY`: `<SUPABASE_ANON_KEY>`
- `SUPABASE_SERVICE_ROLE_KEY`: `<SUPABASE_SERVICE_ROLE_KEY>` (Admin/Backend Only)
- `RAZORPAY_KEY_ID`: `rzp_live_xxxxxxxx`
- `RAZORPAY_KEY_SECRET`: `xxxxxxxxxxxxxxxx`
- `RAZORPAY_WEBHOOK_SECRET`: `whsec_xxxxxxxx`
"""
}
