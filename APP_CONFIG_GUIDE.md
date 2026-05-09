# `app_config.json` Configuration Guide

**File location:** `app/src/main/assets/config/app_config.json`

---

## Overview

`app_config.json` is read at startup by `AppConfigDataSource` and resolved by `NetworkConfigResolver`. It controls which backend environment the app targets and whether the Ktor mock engine is used instead of real network calls.

> **Important:** `useMockEngine` and `environment` are **only respected in debug builds**. In a release (non-debuggable) build, the app always uses `PRODUCTION` environment with the real network — regardless of what this file says.

---

## Schema

```json
{
  "environment": "TEST" | "STAGE" | "PRODUCTION",
  "useMockEngine": true | false,
  "baseUrls": {
    "test":       "<url>",
    "stage":      "<url>",
    "production": "<url>"
  }
}
```

### Fields

| Field | Type | Default | Description |
|---|---|---|---|
| `environment` | `string` | `"TEST"` | Which environment to target. Case-insensitive. Falls back to `TEST` if unrecognised. |
| `useMockEngine` | `boolean` | `true` | When `true`, Ktor uses the in-memory `MockEngine` reading JSON from `assets/mock/`. When `false`, uses the real OkHttp engine. |
| `baseUrls` | `object` | see below | Map of environment keys to base URLs. The key matching the active `environment` is used. |

### `baseUrls` keys

| Key | Used when `environment` is |
|---|---|
| `"test"` | `"TEST"` |
| `"stage"` | `"STAGE"` |
| `"production"` | `"PRODUCTION"` |

If a key is missing, the code falls back to built-in defaults:

| Environment | Built-in default |
|---|---|
| TEST | `https://test.goal-erp.local/` |
| STAGE | `https://stage.goal-erp.local/` |
| PRODUCTION | `https://api.goal-erp.com/` |

---

## Preset Configurations

### 1. Mock Engine (offline / UI development)

All API calls are served from local JSON files in `assets/mock/`. No network required.

```json
{
  "environment": "TEST",
  "useMockEngine": true,
  "baseUrls": {
    "test":       "http://havostech-001-site2.atempurl.com/",
    "stage":      "http://havostech-001-site2.atempurl.com/",
    "production": "https://api.goal-erp.com/"
  }
}
```

Log tag: `GoalERP-Mock(TEST)` — verbose body logging enabled.

---

### 2. Live Test Server (current default)

Calls hit the real test server. Token auth is fully active.

```json
{
  "environment": "TEST",
  "useMockEngine": false,
  "baseUrls": {
    "test":       "http://havostech-001-site2.atempurl.com/",
    "stage":      "http://havostech-001-site2.atempurl.com/",
    "production": "https://api.goal-erp.com/"
  }
}
```

Log tag: `GoalERP-Network(TEST)` — verbose body logging enabled.

---

### 3. Stage Server

```json
{
  "environment": "STAGE",
  "useMockEngine": false,
  "baseUrls": {
    "test":       "http://havostech-001-site2.atempurl.com/",
    "stage":      "https://stage.goal-erp.com/",
    "production": "https://api.goal-erp.com/"
  }
}
```

Log tag: `GoalERP-Network(STAGE)` — verbose body logging enabled.

---

### 4. Production (debug build pointing at prod)

```json
{
  "environment": "PRODUCTION",
  "useMockEngine": false,
  "baseUrls": {
    "test":       "http://havostech-001-site2.atempurl.com/",
    "stage":      "https://stage.goal-erp.com/",
    "production": "https://api.goal-erp.com/"
  }
}
```

Log tag: `GoalERP-Network(PRODUCTION)` — **logs suppressed** (NONE level).

---

## Mock Asset File Map

When `useMockEngine: true`, every API call is matched in `MockAssetResponseProvider` and served from these files:

| API Endpoint | Method | Asset file |
|---|---|---|
| `api/auth/login` | POST | `mock/auth/login_success.json` |
| `api/auth/logout` | POST | `mock/auth/logout_success.json` |
| `api/auth/refresh` | POST | `mock/auth/refresh_success.json` |
| `api/auth/profile` | GET | `mock/auth/profile_success.json` |
| `api/dashboard` | GET | `mock/dashboard/dashboard.json` |
| `reports/tank-stock-summary` | GET | `mock/reports/tank_stock_summary.json` |
| `reports/raw-material-stock` | GET | `mock/reports/raw_material_stock.json` |
| `reports/packaging-loss-gain` | GET | `mock/reports/packaging_loss_gain.json` |
| `api/reports/tank-stock` | GET | `mock/reports/stock_overview_tanks.json` |
| `api/reports/sales-summary` | GET | `mock/reports/report_sales_summary.json` |
| `api/reports/product-sales` | GET | `mock/reports/product_sales.json` |
| `api/reports/net-profit` | GET | `mock/reports/net_profit.json` |
| `api/reports/expense-summary` | GET | `mock/reports/expense_summary.json` |
| `api/payments/received` | GET | `mock/payments/received.json` |
| `api/customers/{id}/mobile-summary` | GET | `mock/customers/customer_mobile_summary.json` |
| `api/ledger/customer/{id}` | GET | `mock/customers/customer_ledger.json` |
| `api/customers` | GET | `mock/customers/customers.json` |
| `api/notifications/unread-count` | GET | `mock/notifications/unread_count.json` |
| `api/notifications/read-all` | POST | `mock/notifications/mark_all_read.json` |
| `api/notifications/{id}/read` | POST | `mock/notifications/mark_read.json` |
| `api/notifications` | GET | `mock/notifications/notifications.json` |

Any unmatched route returns `404` with `{"error":"Mock route not found", ...}`.

---

## How to switch quickly

Just change the two top-level fields:

```json
"useMockEngine": true   ← offline mock
"useMockEngine": false  ← real server

"environment": "TEST"        ← test server
"environment": "STAGE"       ← stage server
"environment": "PRODUCTION"  ← prod server
```

No code changes needed. Rebuild and run.

---

## Notes

- The file is parsed with `ignoreUnknownKeys = true` and `isLenient = true` — extra fields are safe.
- If the file is missing or malformed, all fields fall back to their defaults (`environment=TEST`, `useMockEngine=true`).
- The `baseUrl` for the active environment must end with a trailing `/` for Ktor's `defaultRequest` to resolve paths correctly.
