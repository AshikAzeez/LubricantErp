# Implementation Plan: Distinctive UI Modernization

## Overview

Modernize the Kotlin/Jetpack Compose presentation layer incrementally while preserving the existing MVI contracts, Koin resolution paths, API/domain behavior, role permissions, routes and Back outcomes, calculations, validation decisions, and exact stored/submitted values. Each coding prompt builds on prior prompts, keeps legacy and modernized renderers compatible during migration, and ends by consolidating on approved shared patterns.

All correctness, accessibility, pilot approval, functional regression, and performance gates below are mandatory. The only optional task is the conditional final-brand integration task, which applies only if approved replacement assets or visual references become available.

## Tasks

- [ ] 1. Establish the migration inventory and baseline evidence
  - [x] 1.1 Implement the checked-in migration ledger schema and seed every authentication, home, report-discovery, report-detail, customer, notification, payment, order, product, cost-breakdown, form, modal, and navigation surface
    - Record route/family, owner, status, deferral reason, baseline case IDs, approval IDs, and release-evidence IDs in a machine-validatable artifact.
    - Do not change route identifiers, permission decisions, MVI types, or Koin definitions while inventorying surfaces.
    - _Requirements: 16.1, 16.2, 16.11–16.13; Design Properties: 42_
  - [-] 1.2 Create deterministic baseline case and benchmark-condition fixtures
    - Encode input data, user role, account state, expected calculations/validation/navigation outcomes, device/OS/build/data/network/power/thermal/animation conditions, and exact displayed/submitted values for each affected ERP workflow.
    - Cover Login; Home; report discovery; Sales and Tank reports; Cost Breakdown; customer, product, payment, order, notification, settings, and modal/navigation workflows.
    - _Requirements: 10.2–10.7, 16.3, 16.4; Design Properties: 42_
  - [~] 1.3 Add characterization tests around existing MVI, Koin, API/domain, permissions, calculations, validation, persistence, and navigation behavior
    - Assert existing state/intent/effect sequences, dependency paths, request fields/counts, response/error interpretation, precision/rounding, accepted/rejected values, role-authorized destinations, stale unauthorized rejection, and visible/system/top-level Back outcomes before renderer changes.
    - _Requirements: 12.3–12.12, 14.2–14.14, 16.5–16.10, 16.14; Design Properties: 9, 18, 22, 35, 36, 39, 42_
  - [~] 1.4 Add deterministic baseline semantics and screenshot capture cases for the representative surfaces
    - Capture fixed `en-IN` data, clock, font, system bars/insets, Light/Dark, default/200% font scale, reduced-motion end state, and applicable loading/empty/error/offline/content/form-error/pending states.
    - Store stable IDs that later migrated goldens and approval evidence can reference.
    - _Requirements: 1.1–1.13, 5.1–5.13, 9.3–9.20, 16.16–16.21; Design Properties: 1, 2, 4, 12, 13, 25, 43_
  - [~] 1.5 Add baseline performance trace markers and capture harnesses
    - Mark title visibility, first task-critical content, input readiness, and Back availability without logging customer, credential, form, financial, or API payload data.
    - Prepare same-condition baseline results for Login→Home, Home scroll/refresh, Reports→Sales, 100-row report scroll/filter, Customer list→detail→back, and Cost Breakdown form open/validation/scroll.
    - _Requirements: 10.2–10.11, 15.9–15.11, 16.3, 16.4; Design Properties: 26, 41, 42_

- [ ] 2. Build the required automated test infrastructure
  - [x] 2.1 Configure Kotlin/JVM unit and Kotest property-test source sets with seed replay
    - Add repository-compatible versions against the existing AGP, Kotlin, and Compose BOM; configure shrunk counterexample and seed output for CI.
    - Preserve production dependency graphs and do not move business logic into test-only adapters.
    - _Requirements: 16.4–16.10, 16.14–16.21; Design Properties: 1–44_
  - [-] 2.2 Create deterministic fake-data and state-scenario builders
    - Provide long/short Unicode text, exact `BigDecimal` values, all statuses, dates, quantities, missing assets, all component-state combinations, and loading/empty/error/offline/success/refresh/session-expiry cases.
    - Keep fake repositories behind the existing Koin/view-model boundaries and existing intents/effects.
    - _Requirements: 8.1–8.22, 11.1–11.18, 16.4–16.10; Design Properties: 4, 5, 20–24, 26–33_
  - [-] 2.3 Configure host-side Compose screenshot/golden testing
    - Fix locale to `en-IN`, clock/date, fonts, animation end state, system bars/insets, data, dimensions, themes, and font scales; support the required 320/599/600/839/840/1200 dp matrix.
    - Make diffs report clipping, overlap, theme leakage, hierarchy, alignment, and reachable-action failures without treating pixels as business assertions.
    - _Requirements: 4.1–4.16, 5.1–5.13, 16.17–16.21; Design Properties: 8, 10, 12, 43_
  - [~] 2.4 Configure Compose instrumentation, semantics, automated accessibility, resize, IME/inset, process-recreation, and navigation test harnesses
    - Support non-touch focus, modal traversal/focus restoration, live-region assertions, window resizing during active operations, visible/system Back comparison, and role-specific navigation graphs.
    - _Requirements: 4.3–4.16, 9.6–9.20, 14.1–14.14, 16.20; Design Properties: 9, 15, 25, 34, 35, 39, 43_
  - [~] 2.5 Create the Macrobenchmark module and representative 100-record fixture
    - Generate production-equivalent text, status, currency, quantity, date, and stable image-placeholder rows; implement five warm-ups, 30 measured runs, and a continuous 10-second scroll action.
    - Capture frame timing, first-interactive-content, navigation timing, environment parity, and P95 comparison outputs.
    - _Requirements: 10.2–10.11, 10.13–10.15; Design Properties: 26, 42_
  - [~] 2.6 Add test-result aggregation and acceptance-evidence validation
    - Map requirement clauses to property, unit, semantics, golden, instrumentation, accessibility, regression, benchmark, and approval result IDs; fail CI for missing mandatory evidence.
    - _Requirements: 16.14–16.26; Design Properties: 42, 43, 44_

- [ ] 3. Implement semantic tokens and foundational pure UI policies
  - [~] 3.1 Complete `GoalErpTheme` semantic token families and Light/Dark schemes
    - Add complete Material and ERP color roles, typography including financial/tabular roles, named spacing, elevation, icon rules, motion roles, and exactly four primary shape roles; retain existing `ThemeMode.SYSTEM/LIGHT/DARK` persistence and root API compatibility.
    - _Requirements: 2.1, 2.4, 2.5, 5.1–5.7, 6.1, 6.2, 6.9, 16.11; Design Properties: 12, 14_
  - [~] 3.2 Implement pure brand-cue, logo-fit, text-size, contrast, and tonal-fallback policies
    - Use only the provisional logo/violet-green direction; omit unsafe decoration, preserve a minimum 96 dp unclipped aspect-fit logo, and never return a failing meaningful color pair.
    - _Requirements: 1.2–1.8, 5.8–5.13, 9.1–9.5; Design Properties: 1, 2, 11, 12, 13_
  - [~] 3.3 Implement deterministic action, status, primary-action, and simultaneous-state resolvers
    - Centralize labels/emphasis, professional status terminology and non-color cues, one-primary-action normalization, and `Disabled > Loading > Error > Selected > Focused > Pressed > Default` visuals while retaining all semantic states.
    - _Requirements: 1.9, 1.13, 2.2, 2.3, 2.6, 2.7, 2.10, 3.5, 9.14; Design Properties: 3, 4, 5, 6_
  - [~] 3.4 Implement pure adaptive window and compact-priority planning
    - Classify current width at 600/840 dp boundaries, cap body lines at 80 characters, increase useful simultaneous content on wider windows, and return composition plans without emitting submit/cancel/retry/navigation events.
    - _Requirements: 3.12, 4.1–4.13, 16.17; Design Properties: 7, 8, 9, 10_
  - [~] 3.5 Implement token-schema, icon-action, and semantic-descriptor validators
    - Require 14 sp task body text, exactly four shape roles, standard Material icons, adjacent text for nonstandard icons, record-specific icon action labels, and complete one-node interaction semantics.
    - _Requirements: 6.1–6.9, 9.10–9.12; Design Properties: 14, 15, 25_
  - [~] 3.6 Write the Kotest property test for Design Property 1, brand cue quota and safe eligibility
    - Use at least 100 successful iterations and edge distributions for constrained geometry, contrast failures, and omitted decoration.
    - _Requirements: 1.2, 1.7, 1.8, 5.13; Design Property: 1_
  - [~] 3.7 Write the Kotest property test for Design Property 2, logo aspect preservation
    - Generate positive source dimensions and valid target bounds; assert minimum width and at most one-percent aspect deviation.
    - _Requirements: 1.4, 1.5; Design Property: 2_
  - [~] 3.8 Write the Kotest property test for Design Property 3, equivalent action consistency
    - Generate action IDs, priorities, states, and themes and compare resolved labels, emphasis, availability, and metadata.
    - _Requirements: 2.2, 2.10; Design Property: 3_
  - [~] 3.9 Write the Kotest property test for Design Property 4, stable non-color-only status meaning
    - Cover every `BusinessStatus` in both themes and require terminology, semantic role, text, and an additional cue where color is used.
    - _Requirements: 1.9, 2.3, 9.14, 13.8; Design Property: 4_
  - [~] 3.10 Write the Kotest property test for Design Property 5, deterministic complete simultaneous states
    - Generate every nonempty state subset and verify precedence plus retention of every applicable semantic meaning.
    - _Requirements: 2.6, 2.7; Design Property: 5_
  - [~] 3.11 Write the Kotest property test for Design Property 6, one primary action per region
    - Generate arbitrary labeled region actions and assert at most one primary while preserving lower-priority actions.
    - _Requirements: 3.5; Design Property: 6_
  - [~] 3.12 Write the Kotest property test for Design Property 7, compact task-priority order
    - Generate role subsets and assert the documented descending order without omissions.
    - _Requirements: 3.12; Design Property: 7_
  - [~] 3.13 Write the Kotest property test for Design Property 8, exact window classification
    - Cover every width from 320 through 1200 dp with weighted boundary values.
    - _Requirements: 4.1, 4.2, 16.17; Design Property: 8_
  - [~] 3.14 Write the Kotest property test for Design Property 9, presentation-only adaptive planning
    - Generate layout transitions, content, and pending tokens; assert no workflow event and no data mutation.
    - _Requirements: 4.8, 4.9, 4.10; Design Property: 9_
  - [~] 3.15 Write the Kotest property test for Design Property 10, bounded wide-layout visibility
    - Generate content and Medium/Large widths; compare visible task regions with Compact and enforce the 80-character body measure.
    - _Requirements: 4.13; Design Property: 10_
  - [~] 3.16 Write the Kotest property test for Design Property 11, text-size contrast classification
    - Weight generation around regular 18-point and bold 14-point boundaries.
    - _Requirements: 9.1, 9.2; Design Property: 11_
  - [~] 3.17 Write the Kotest property test for Design Property 12, semantic color contrast
    - Evaluate every light/dark semantic foreground/background pair against normal, large, boundary, focus, and non-text thresholds.
    - _Requirements: 5.8, 5.9, 5.10, 9.3, 9.4, 9.5; Design Property: 12_
  - [~] 3.18 Write the Kotest property test for Design Property 13, safe contrast fallback
    - Generate failing provisional colors and candidate palettes; require a passing replacement or omission only for optional decoration.
    - _Requirements: 1.7, 5.12, 5.13; Design Property: 13_
  - [~] 3.19 Write the Kotest property test for Design Property 14, token type/shape invariants
    - Generate accepted configurations and require task body text of at least 14 sp and exactly `control/card/container/modal` shapes.
    - _Requirements: 6.1, 6.2, 6.9; Design Property: 14_
  - [~] 3.20 Write the Kotest property test for Design Property 15, specific icon-action labels
    - Generate standard/custom and record/global descriptors; enforce adjacent custom-icon text and action-plus-record semantics.
    - _Requirements: 6.6, 6.7, 6.8; Design Property: 15_

- [ ] 4. Implement motion, feedback, state, anchoring, and asset policies
  - [~] 4.1 Implement pure motion, reduced-motion, pending-action, and operation-feedback policies
    - Read duration scale/override, bound transitions and decoration, stop repetition by five seconds, suppress spatial motion when reduced, reject duplicate pending activations, and construct named 4–10 second feedback independent of animation completion.
    - _Requirements: 7.1–7.11, 10.1; Design Properties: 16, 17, 18, 19_
  - [~] 4.2 Implement pure region-state, persistent-context, anchor, failure-isolation, next-action, and missing-asset policies
    - Preserve prior content/values/filters, distinguish empty/offline/failure, scope retries, follow same/predecessor/follower/start anchoring, isolate sibling regions, explain nonretryable failures, and keep asset geometry stable.
    - _Requirements: 8.1–8.22, 10.12, 10.14, 10.15, 13.2–13.5, 13.9–13.14; Design Properties: 20–24, 26_
  - [~] 4.3 Write the Kotest property test for Design Property 16, bounded motion durations
    - Cover every normal token and repeating-decoration stop time.
    - _Requirements: 7.2, 7.3, 7.10; Design Property: 16_
  - [~] 4.4 Write the Kotest property test for Design Property 17, reduced-motion outcome equivalence
    - Generate motion requests at zero/reduced scale; reject spatial effects and require the same final/semantic result within 150 ms.
    - _Requirements: 7.4, 7.5, 7.8; Design Property: 17_
  - [~] 4.5 Write the Kotest property test for Design Property 18, duplicate pending-action rejection
    - Generate pending action sequences and assert no second event and unchanged entered values until terminal state.
    - _Requirements: 7.6, 12.8, 12.9; Design Property: 18_
  - [~] 4.6 Write the Kotest property test for Design Property 19, identified bounded feedback
    - Generate success/failure operations and require operation identity, 4–10 second timeout, and semantic availability independent of motion.
    - _Requirements: 7.7, 7.8, 7.9; Design Property: 19_
  - [~] 4.7 Write the Kotest property test for Design Property 20, unambiguous actionable state presentation
    - Generate loading, empty, retryable failure, and offline models and verify distinct kinds, names, and applicable actions.
    - _Requirements: 8.2–8.8; Design Property: 20_
  - [~] 4.8 Write the Kotest property test for Design Property 21, nonblocking context preservation
    - Generate previous content, values, filters, refresh/offline/failure and require unchanged context plus stale identification.
    - _Requirements: 8.9, 8.10, 8.11; Design Property: 21_
  - [~] 4.9 Write the Kotest property test for Design Property 22, deterministic anchor resolution
    - Generate old/new key lists and anchors, including empty/single/removed lists, and enforce same/predecessor/follower/start order.
    - _Requirements: 8.12–8.15, 13.3–13.5; Design Property: 22_
  - [~] 4.10 Write the Kotest property test for Design Property 23, isolated region failures
    - Generate independently keyed region collections and require unaffected successful siblings plus failed-region-only retry.
    - _Requirements: 8.19, 8.20; Design Property: 23_
  - [~] 4.11 Write the Kotest property test for Design Property 24, explained nonretryable failures
    - Generate failure descriptors and require a reason and specifically labeled correction/navigation/dismiss/support action.
    - _Requirements: 8.21, 8.22, 15.12; Design Property: 24_
  - [~] 4.12 Write the Kotest property test for Design Property 25, complete interactive semantics
    - Generate descriptors and assert name/role plus every applicable value/state/action exactly once.
    - _Requirements: 9.10, 9.12; Design Property: 25_
  - [~] 4.13 Write the Kotest property test for Design Property 26, stable meaningful asset fallback
    - Generate slot dimensions and load outcomes; enforce invariant geometry and meaningful fallback descriptions.
    - _Requirements: 10.14, 10.15; Design Property: 26_

- [ ] 5. Implement ERP formatting, visualization, table, form, trust, and navigation policies
  - [~] 5.1 Implement `ErpValueFormatter`, metric/data-quality models, and exact chart normalization/selection policies
    - Format INR with Indian grouping/two decimals, quantities with units, dates as `DD MMM YYYY`, preserve exact `BigDecimal` source values, label baselines/ranges, keep equal/all-zero/mixed-sign relationships, and return exact selected data.
    - _Requirements: 6.3, 6.4, 11.1–11.9, 11.12–11.18, 15.6; Design Properties: 27, 28, 29, 30, 33_
  - [~] 5.2 Implement filter-summary, adaptive-table, and row-action policies
    - Preserve every active filter/search/date/sort/group input, expose all fields at every supported width, keep numeric/date alignment metadata, and separate one row navigation action from labeled secondary actions.
    - _Requirements: 11.10, 11.11, 13.1–13.16; Design Properties: 22, 27, 31, 32, 38_
  - [~] 5.3 Implement form field, focus/keyboard, submission-result, and consequential-confirmation policies
    - Preserve exact entered/calculated values, expose persistent/required/dependency/error/read-only semantics, map input methods, focus first invalid/next enabled fields, prevent duplicate submission, and name records/consequences.
    - _Requirements: 9.18, 12.1–12.21, 15.2–15.5; Design Properties: 18, 34, 35, 36, 37_
  - [~] 5.4 Implement navigation/modal descriptors, password semantics, and full-value redaction
    - Model labeled/non-color destination selection, mandatory modal outcomes and alert counts; expose Show/Hide password state correctly; remove complete credential values from feedback, logs, and errors.
    - _Requirements: 14.1, 14.7–14.11, 15.7–15.12; Design Properties: 39, 40, 41_
  - [~] 5.5 Write the Kotest property test for Design Property 27, explicit consistent ERP formatting
    - Generate extreme valid `BigDecimal`s, units, and dates; assert Indian currency, exact precision, units, date shape, and group alignment metadata.
    - _Requirements: 6.3, 6.4, 11.1–11.5, 13.7; Design Property: 27_
  - [~] 5.6 Write the Kotest property test for Design Property 28, chart text-equivalent distinctions
    - Generate chart models and require associated non-color cues plus category/exact value/unit/status/range semantics.
    - _Requirements: 9.13, 11.6, 11.7, 11.8; Design Property: 28_
  - [~] 5.7 Write the Kotest property test for Design Property 29, mathematically faithful chart scaling
    - Weight equal, all-zero, mixed-sign, and non-zero-baseline data and assert magnitude/baseline invariants.
    - _Requirements: 11.9, 11.13–11.16; Design Property: 29_
  - [~] 5.8 Write the Kotest property test for Design Property 30, exact chart selection
    - Generate selected IDs and assert category, exact `BigDecimal`, and unit come from source rather than normalized/abbreviated values.
    - _Requirements: 11.18; Design Property: 30_
  - [~] 5.9 Write the Kotest property test for Design Property 31, complete active-filter context
    - Generate all filter dimensions and no-match states; require every nondefault input plus reset behavior.
    - _Requirements: 11.10, 13.1, 13.13, 13.14; Design Property: 31_
  - [~] 5.10 Write the Kotest property test for Design Property 32, complete adaptive-table fields
    - Generate rows, column sets, and supported widths; compare the union of direct and expandable fields to the source set without duplicates.
    - _Requirements: 11.11, 13.6; Design Property: 32_
  - [~] 5.11 Write the Kotest property test for Design Property 33, truthful unavailable/abbreviated values
    - Generate metrics and require a nonnumeric unavailable label plus one-interaction and accessibility access to complete values.
    - _Requirements: 11.12, 11.17, 15.6; Design Property: 33_
  - [~] 5.12 Write the Kotest property test for Design Property 34, explicit accessible fields
    - Generate required, dependent, invalid, calculated, read-only, and disabled combinations and assert the complete field contract.
    - _Requirements: 9.18, 12.1, 12.2, 12.6, 12.17, 12.18, 12.21; Design Property: 34_
  - [~] 5.13 Write the Kotest property test for Design Property 35, logical focus/keyboard order
    - Generate ordered fields and types; assert first enabled invalid, next enabled, and matching input method.
    - _Requirements: 12.5, 12.16, 12.20; Design Property: 35_
  - [~] 5.14 Write the Kotest property test for Design Property 36, trustworthy form failure/success context
    - Generate forms through pending/failure/success and assert value preservation, specific recovery, and record identity.
    - _Requirements: 12.8, 12.10, 12.11, 12.12; Design Property: 36_
  - [~] 5.15 Write the Kotest property test for Design Property 37, specific consequential confirmations
    - Generate delete/irreversible/discard actions and require verb-object, visible identifier, and consequence.
    - _Requirements: 12.13, 12.14, 15.2, 15.3, 15.4; Design Property: 37_
  - [~] 5.16 Write the Kotest property test for Design Property 38, one row target and separate secondary controls
    - Generate navigation/edit/delete/collection/overflow combinations and verify labeling and separation.
    - _Requirements: 13.15, 13.16; Design Property: 38_
  - [~] 5.17 Write the Kotest property test for Design Property 39, navigation/modal state communication
    - Generate selected destinations, mandatory modals, and alert counts; require visible text/non-color cue, explicit outcomes, and count meaning.
    - _Requirements: 14.1, 14.8, 14.9; Design Property: 39_
  - [~] 5.18 Write the Kotest property test for Design Property 40, matching password visibility semantics
    - Verify hidden/Show and visible/Hide pairings for every generated visibility state.
    - _Requirements: 15.7, 15.8; Design Property: 40_
  - [~] 5.19 Write the Kotest property test for Design Property 41, complete sensitive-value redaction
    - Generate credentials embedded at arbitrary positions in feedback/log/error templates and require complete replacement.
    - _Requirements: 15.9, 15.10, 15.11; Design Property: 41_

- [ ] 6. Implement migration governance and rollout gates
  - [~] 6.1 Implement migration-ledger, baseline-manifest, verification-matrix, and approval-gate validators
    - Reject invalid/deferred-without-reason records, mismatched comparison conditions, missing matrix combinations, missing mandatory evidence, and rollout beyond pilots without all product and brand approval IDs.
    - _Requirements: 16.1–16.4, 16.14–16.26; Design Properties: 42, 43, 44_
  - [~] 6.2 Write the Kotest property test for Design Property 42, valid migration records and baseline manifests
    - Generate all surface families, statuses, deferrals, and comparison conditions; require complete inventory and exact condition parity.
    - _Requirements: 16.1, 16.2, 16.3, 16.4; Design Property: 42_
  - [~] 6.3 Write the Kotest property test for Design Property 43, complete verification Cartesian product
    - Generate applicable state/accessibility scenarios and require all six widths, two font scales, both themes, and every applicable scenario.
    - _Requirements: 16.16–16.21; Design Property: 43_
  - [~] 6.4 Write the Kotest property test for Design Property 44, pilot approval gating
    - Generate complete and incomplete approval sets and permit expansion if and only if product and brand approval exist for authentication, dashboard, report, and form pilots.
    - _Requirements: 16.22–16.26; Design Property: 44_

- [ ] 7. Build and verify the shared Compose component catalog
  - [~] 7.1 Implement a deterministic component catalog surface for every token, component state, theme, window class, font scale, and motion preference
    - Keep catalog models fake/presentation-only and avoid Koin, repositories, navigation, or business calculations.
    - _Requirements: 2.1, 2.4–2.7, 5.1, 9.8; Design Properties: 5, 8, 12, 14, 17, 43_
  - [~] 7.2 Implement `GoalActionButton`, `BusinessStatusBadge`, `ContentRegion`, metric cards, and shared feedback
    - Use Material interaction/ripple/focus behavior, 48 dp targets, one semantic node, headings/traversal groups, one primary slot, named pending/result states, and non-color status meaning.
    - _Requirements: 1.9, 2.2–2.8, 3.1–3.9, 7.1, 7.6–7.9, 9.6–9.15; Design Properties: 3–6, 18, 19, 25_
  - [~] 7.3 Implement `GoalTextField`, `GoalSelectField`, `FormScaffold`, and confirmation patterns
    - Include persistent labels, required/dependency/error/read-only/password semantics, IME visibility, logical focus, pending-submit protection, scrollable modals, and launcher-focus restoration.
    - _Requirements: 4.14, 4.16, 9.16–9.18, 12.1–12.21, 15.3–15.8; Design Properties: 18, 34–37, 40_
  - [~] 7.4 Implement `RegionStateHost` and structure-matched loading/state components
    - Delay initial loading until 300 ms, retain persistent content for refresh/offline/failure, scope actions, announce results, preserve siblings/anchors, and distinguish all state kinds.
    - _Requirements: 8.1–8.22, 9.15, 10.12; Design Properties: 19–24_
  - [~] 7.5 Implement `FilterContextBar`, adaptive record rows/tables, stable anchors, and paging footers
    - Expose complete fields at all widths, use stable keys/tabular alignment, retain loaded rows/scroll, show active filters, and keep row navigation separate from secondary actions.
    - _Requirements: 6.3, 6.4, 11.10, 11.11, 13.1–13.16; Design Properties: 22, 27, 31, 32, 38_
  - [~] 7.6 Implement `AccessibleBusinessChart` and Tank-level renderer
    - Render exact labels/units/status, accessible alternatives, equal/all-zero/mixed-sign baselines, deterministic selection, size-stable fallback, and bounded/disabled decorative wave motion.
    - _Requirements: 7.2–7.5, 9.13, 11.6–11.9, 11.13–11.18; Design Properties: 16, 17, 26, 28–30_
  - [~] 7.7 Implement `GoalTopAppBar`, adaptive `GoalNavigationSuite`, dialogs, and sheets
    - Render the existing authorized destination set as labeled bar/rail without route reordering; preserve existing callbacks/Back outcomes and enforce visible dismissal or mandatory decisions, modal containment, and focus restoration.
    - _Requirements: 4.15, 4.16, 9.16, 9.17, 14.1–14.14; Design Properties: 8, 9, 39_
  - [~] 7.8 Add Compose unit and semantics tests for the complete component catalog
    - Assert 48×48 dp nonoverlapping bounds, exact labels/roles/values/states/actions, one action node, traversal/headings, decorative exclusion, focus order, live regions, logo semantics/aspect, and field/status/chart/modal contracts.
    - _Requirements: 1.4–1.11, 3.2, 3.3, 3.9–3.11, 9.6–9.20; Design Properties: 2, 4, 15, 25, 28, 34, 39, 40_
  - [~] 7.9 Add Light/Dark component screenshot goldens at required widths and font scales
    - Cover every meaningful default/focused/pressed/selected/disabled/loading/error/combined state, normal/reduced motion end states, charts/tank, tables, navigation, dialogs, and sheets.
    - _Requirements: 2.4–2.7, 4.1–4.16, 5.1–5.13, 9.8, 9.9; Design Properties: 5, 8, 10, 12, 14, 17, 32, 43_
  - [~] 7.10 Add instrumented accessibility, keyboard/directional focus, IME/inset, and modal tests for shared components
    - Run automated accessibility checks where supported and verify traversal containment, launcher focus restoration, announcements without focus movement, 200% text, and reachable modal actions.
    - _Requirements: 4.14–4.16, 9.6–9.20; Design Properties: 15, 25, 34, 35, 39, 43_
  - [~] 7.11 Add focused unit/interaction examples for boundaries and catalog registries
    - Cover 299/300 ms loading, 599/600 and 839/840 dp, `₹12,34,567.89`, negative/zero/unavailable values, `05 Jan 2026`, fixed statuses/action verbs, icon/type/shape registries, modal precedence, and route-title mappings.
    - _Requirements: 4.1–4.2, 6.1–6.9, 8.1, 11.1–11.5, 11.12, 14.6; Design Properties: 8, 14, 15, 20, 27, 33, 39_
  - [~] 7.12 Integrate shared tokens/components under the existing root theme without migrating feature behavior
    - Verify legacy and modernized surfaces can coexist with compatible system bars, theme switching, traversal, focus, and announcements; do not create a parallel shell or route graph.
    - _Requirements: 5.4–5.7, 16.11–16.13; Design Properties: 9, 12, 25_

- [~] 8. Foundation checkpoint
  - Ensure all foundation, property, component, semantics, screenshot, instrumentation, and accessibility tests pass; ask the user if questions arise.

- [ ] 9. Migrate the Login representative authentication pilot
  - [~] 9.1 Replace Login rendering with shared theme, brand, field, action, state, and motion patterns at the existing route boundary
    - Preserve the existing login `UiState`, intents/effects, Koin view model, API behavior, password value/visibility, IME actions, validation, pending protection, errors, success navigation, and submitted credentials.
    - Use only provisional assets, at least two safe cues, `LubricantERP` logo semantics, decorative exclusion, reduced motion, and full Light/Dark/adaptive behavior.
    - _Requirements: 1.1–1.12, 4.1–4.16, 5.1–5.13, 7.1–7.11, 12.1–12.21, 15.7–15.11; Design Properties: 1, 2, 9, 12, 13, 17–20, 25, 34–36, 40, 41_
  - [~] 9.2 Add Login Compose semantics, screenshot, accessibility, resize/IME, and state tests
    - Cover all required widths/themes/font scales, password states, validation focus, pending duplicate rejection, error/recovery, operation announcement, cue omission, and reduced-motion end state.
    - _Requirements: 4.3–4.16, 9.6–9.20, 12.3–12.21, 16.17–16.21; Design Properties: 1, 2, 17, 18, 20, 25, 34, 35, 40, 43_
  - [~] 9.3 Add Login functional/API/navigation regression tests against baseline
    - Assert identical accepted/rejected credentials, request fields/counts, errors, Koin path, state/effects, sensitive redaction, success destination, and system/visible Back behavior.
    - _Requirements: 14.3–14.6, 15.9–15.11, 16.4–16.10, 16.14; Design Properties: 18, 36, 39, 41, 42_

- [ ] 10. Migrate the Home dashboard and top-level navigation pilot
  - [~] 10.1 Wire `GoalNavigationSuite` into the existing app shell
    - Preserve route constants/order, authorized destination filtering, stale-link rejection, selected state, alert count, destination reachability/action counts, saved top-level state, process recreation, and baseline top-level/system Back outcomes.
    - _Requirements: 4.4, 4.12, 14.1–14.14, 16.9, 16.10; Design Properties: 8, 9, 22, 39_
  - [~] 10.2 Replace Home dashboard rendering with adaptive shared KPI, region, chart, refresh, and state components
    - Order title/current KPIs/urgent status/primary action/trends/transactions semantically; preserve exact state, values, refresh intent/count, filters, anchors, navigation callbacks, and calculations.
    - Remove perpetual local animation and hard-coded styling in favor of tokens and reduced-motion policy.
    - _Requirements: 1.1–1.13, 3.1–3.12, 4.1–4.13, 7.1–7.11, 8.1–8.22, 10.1, 11.1–11.18; Design Properties: 1, 4, 6–10, 16–23, 27–30, 33_
  - [~] 10.3 Add Home/navigation semantics, screenshot, accessibility, adaptive, state, and reduced-motion tests
    - Cover KPI hierarchy, current/stale/unavailable values, chart alternatives, refresh with persistent content, partial region failure, bar/rail selection, 200% text, and every required matrix boundary.
    - _Requirements: 3.6, 4.1–4.13, 8.9–8.20, 9.8–9.15, 11.5–11.18, 16.17–16.21; Design Properties: 8, 10, 17, 21–23, 25, 28–30, 33, 39, 43_
  - [~] 10.4 Add Home/navigation role, workflow, API, and Back regression tests
    - Compare destination sets/action counts, stale unauthorized handling, tabs/anchors after return, state/effects, exact values, refresh request count, modal dismissal, and top-level/system Back with baseline.
    - _Requirements: 13.2–13.5, 14.2–14.14, 16.4–16.10, 16.14; Design Properties: 9, 22, 39, 42_
  - [~] 10.5 Add Home/navigation Macrobenchmark scenarios
    - Measure Login→Home first-interactive content, Home scroll/refresh frames, and navigation readiness under the prescribed warm-up/run/environment rules.
    - _Requirements: 10.1–10.13; Design Properties: 17, 26, 42_

- [ ] 11. Migrate report discovery and the Sales/Tank report pilots
  - [~] 11.1 Replace Reports discovery rendering with adaptive shared content regions and navigation cards
    - Preserve authorized report destinations, labels, callbacks, selected/expanded state, Back behavior, and number of activations while removing duplicate grids and local hard-coded treatments.
    - _Requirements: 1.1–1.13, 2.2–2.10, 3.1–3.12, 4.1–4.13, 14.1–14.14; Design Properties: 1, 3–10, 15, 39_
  - [~] 11.2 Replace Sales Summary rendering with shared filters, states, formatter, adaptive table, and anchoring
    - Preserve source values, filter/sort/group behavior, request mapping/counts, list keys, navigation, append/refresh behavior, and exact domain/API interpretation.
    - _Requirements: 8.1–8.22, 11.1–11.12, 11.17, 13.1–13.16, 16.5–16.10; Design Properties: 20–24, 27, 31–33, 38_
  - [~] 11.3 Replace Tank Stock Summary rendering with the accessible shared chart/tank renderer
    - Preserve exact quantities, units, statuses, selection, filters, and navigation; add range/baseline/category/value semantics, equal/all-zero behavior, non-color cues, and reduced-motion wave suppression.
    - _Requirements: 7.2–7.5, 9.13–9.15, 11.3, 11.5–11.18; Design Properties: 4, 16, 17, 28, 29, 30, 33_
  - [~] 11.4 Add Reports/Sales/Tank semantics, screenshot, accessibility, adaptive, state, and interaction tests
    - Cover discovery, filtered/no-match/reset, loading/offline/error/append/refresh, compact complete-field rows, wide tables, exact/abbreviated values, chart alternatives/selection, 200% text, and required boundaries/themes.
    - _Requirements: 4.1–4.13, 8.1–8.22, 9.8–9.15, 11.1–11.18, 13.1–13.16, 16.17–16.21; Design Properties: 20–25, 27–33, 38, 43_
  - [~] 11.5 Add Reports/Sales/Tank functional, API, filter, anchor, permission, and navigation regression tests
    - Compare exact values/precision, requests/errors, Koin/state/effects, authorized routes, active filters, selected rows, anchor fallback, retry scope/count, and visible/system Back outcomes with baseline.
    - _Requirements: 8.12–8.20, 13.1–13.16, 14.2–14.14, 16.4–16.10, 16.14; Design Properties: 9, 22, 23, 31, 38, 39, 42_
  - [~] 11.6 Add Reports/Sales Macrobenchmark scenarios
    - Measure discovery→Sales readiness and 100-row Sales scroll/filter under the exact baseline environment, five warm-ups, 30 runs, and 10-second scroll rules.
    - _Requirements: 10.2–10.13; Design Properties: 26, 42_

- [ ] 12. Migrate the Create/Edit Cost Breakdown form pilot
  - [~] 12.1 Replace Create/Edit Cost Breakdown rendering with `FormScaffold` and shared fields/actions
    - Preserve `CreateCostBreakdownUiState`, intents/effects, Koin path, dependent selectors, GST behavior, exact entered/stored/submitted values, derived `BigDecimal` calculations, precision/rounding, validation outcomes, pending/cancel behavior, and success navigation.
    - _Requirements: 3.1–3.5, 4.1–4.16, 12.1–12.21, 15.1–15.6, 16.5–16.8; Design Properties: 6, 9, 18, 27, 34–37_
  - [~] 12.2 Replace Cost Breakdown record editor and confirmations with shared modal patterns
    - Preserve modal launch/dismiss/back behavior, values/errors/selections/focus on canceled discard, existing delete/edit intents, visible record identifiers, consequences, and authorized actions.
    - _Requirements: 4.16, 9.16–9.18, 12.13–12.15, 14.2, 14.7, 14.8, 15.2–15.5; Design Properties: 34, 35, 37, 39_
  - [~] 12.3 Add Cost Breakdown semantics, screenshot, accessibility, adaptive, IME, modal, and state tests
    - Cover first-invalid/next focus, required/dependency/read-only semantics, keyboard types, 200% text, pending duplicate protection, nonvalidation failure recovery, window resizing without events/data loss, and modal containment/focus restoration.
    - _Requirements: 4.3–4.16, 9.6–9.20, 12.1–12.21, 16.17–16.21; Design Properties: 9, 18, 25, 34–37, 39, 43_
  - [~] 12.4 Add Cost Breakdown calculation, validation, API, storage, MVI, Koin, and navigation regression tests
    - Compare baseline accepted/rejected inputs, exact derived precision/rounding, request fields/counts, persisted values, effects, retry/cancel/delete outcomes, and visible/system Back behavior.
    - _Requirements: 12.3–12.15, 14.2–14.6, 16.4–16.10, 16.14; Design Properties: 18, 35–37, 39, 42_
  - [~] 12.5 Add Cost Breakdown Macrobenchmark scenarios
    - Measure form open/first-interactive content, validation/focus, and representative form scrolling under controlled baseline-comparison rules.
    - _Requirements: 10.1, 10.5–10.13; Design Properties: 17, 42_

- [ ] 13. Verify and package the complete representative pilot set
  - [~] 13.1 Generate pilot verification cases from the matrix generator
    - Produce every applicable combination of six boundary widths, default/200% font scales, Light/Dark, accessibility scenarios, and loading/empty/error/offline/success/refresh/form states with documented N/A reasons only where valid.
    - _Requirements: 16.16–16.21; Design Properties: 43_
  - [~] 13.2 Run and approve deterministic pilot screenshot/golden suites in CI
    - Cover Login, Home/navigation, Reports discovery, Sales, Tank, and Create/Edit Cost Breakdown; fail on clipping, overlap, theme leakage, unsafe cues, unreadable hierarchy, wrong alignment, or unreachable actions.
    - _Requirements: 1.1–1.13, 3.1–3.12, 4.1–4.16, 5.1–5.13, 16.17–16.21; Design Properties: 1–17, 27–34, 39, 43_
  - [~] 13.3 Run pilot instrumentation and accessibility verification suites
    - Execute automated checks, traversal, keyboard/directional focus, Switch Access-compatible actions, 200% font, announcements, chart alternatives, modal focus, IME/insets, resize preservation, and process recreation.
    - _Requirements: 4.3–4.16, 9.6–9.20, 14.2–14.14, 16.18–16.20; Design Properties: 9, 15, 17, 22, 25, 28, 34, 35, 39, 43_
  - [~] 13.4 Run pilot functional/API/domain/MVI/Koin/permission/navigation regression suites
    - Block the increment for any changed calculation, precision, rounding, validation, displayed/submitted value, request/response/error interpretation, dependency path, permission, destination, modal, Back outcome, or duplicate operation.
    - _Requirements: 16.4–16.15; Design Properties: 9, 18, 22, 35, 36, 39, 42_
  - [~] 13.5 Run pilot Macrobenchmarks and baseline comparisons
    - Require at least 90% of measured scroll frames within budget and no greater than 10% P95 regression for first-interactive-content or navigation under matched conditions.
    - _Requirements: 10.2–10.13, 16.14, 16.15; Design Properties: 26, 42_
  - [~] 13.6 Generate immutable pilot approval-evidence manifests from passing artifacts
    - Link exact deterministic goldens and mandatory test/benchmark results to separate product and brand approval ID slots for authentication, dashboard, report, and form families; do not let generated evidence imply approval.
    - _Requirements: 16.22–16.26; Design Properties: 42, 43, 44_

- [~] 14. Required pilot product and brand approval checkpoint
  - Stop before task 15. Confirm documented product **and** brand approval IDs exist for Login, Home dashboard/navigation, representative Reports, and Create/Edit Cost Breakdown, and that the approval-gate test passes. If any approval is missing, keep broader rollout blocked and revise only the affected pilot/foundation work. Ensure all tests pass, ask the user if questions arise.

- [ ] 15. Migrate the remaining surface families with approved shared patterns
  - [~] 15.1 Migrate Customer list/detail and payment actions
    - Reuse approved filters, rows/tables, state host, details modal/pane, confirmation, feedback, and anchor patterns while preserving customer values, payment intents/API fields, permissions, list return state, and navigation outcomes.
    - _Requirements: 8.1–8.22, 12.13–12.15, 13.1–13.16, 14.2–14.14, 16.5–16.10; Design Properties: 18–24, 31, 32, 37–39_
  - [~] 15.2 Migrate Products and raw-material/inventory surfaces
    - Reuse approved data/status/list/detail/action patterns while preserving stock calculations/units, exact values, search/filter state, record actions, permissions, API interpretation, and Back behavior.
    - _Requirements: 1.9, 8.1–8.22, 11.3, 11.5, 13.1–13.16, 15.1–15.6, 16.5–16.10; Design Properties: 4, 20–24, 27, 31–33, 37, 38_
  - [~] 15.3 Migrate report modules, consolidated stock, and all remaining report families
    - Reuse approved report discovery, filters, formatter, adaptive table/chart, state, and anchor patterns; preserve exact report calculations, requests, filter semantics, permissions, values, and navigation.
    - _Requirements: 8.1–8.22, 11.1–11.18, 13.1–13.16, 16.5–16.10; Design Properties: 20–24, 27–33, 38_
  - [~] 15.4 Migrate Payments and Orders surfaces
    - Reuse approved lists/forms/statuses/confirmations/feedback while preserving financial precision, accepted/rejected values, request counts/fields, role checks, record identifiers, and success/failure navigation.
    - _Requirements: 7.6–7.9, 8.1–8.22, 12.1–12.21, 15.1–15.6, 16.5–16.10; Design Properties: 18–24, 27, 34–38_
  - [~] 15.5 Migrate Proforma Invoice surfaces
    - Reuse approved form, table, formatter, modal, and state patterns while preserving invoice calculations, GST/precision, validation, submission/storage, API behavior, permissions, and navigation outcomes.
    - _Requirements: 8.1–8.22, 11.1–11.5, 12.1–12.21, 15.1–15.6, 16.5–16.10; Design Properties: 18–24, 27, 34–39_
  - [~] 15.6 Migrate Notifications and Settings surfaces
    - Reuse approved state/navigation/action patterns while preserving unread counts, mark-read request behavior, theme-mode persistence, authorized settings, selected values, and Back/process-restoration outcomes.
    - _Requirements: 5.4–5.7, 8.1–8.22, 14.1–14.14, 16.5–16.13; Design Properties: 19–25, 39_
  - [~] 15.7 Migrate every remaining form, dialog, bottom sheet, and transient-message variation
    - Register any justified variation with need/interaction/visual/states/themes; reuse standard fields/actions/modal/state behavior and preserve validation, values, focus, cancellation, effects, and operation outcomes.
    - _Requirements: 2.8–2.10, 4.14–4.16, 7.6–7.9, 9.15–9.18, 12.1–12.21, 14.2, 14.7, 14.8; Design Properties: 3, 5, 18, 19, 25, 34–40_
  - [~] 15.8 Add mandatory per-family unit, Compose semantics, instrumentation, and functional regression suites
    - For each migration increment, assert existing MVI/Koin/API/domain/permission/navigation behavior, exact values/calculations/validation, duplicate prevention, state preservation, and applicable loading/empty/error/offline/success/refresh behavior.
    - _Requirements: 8.1–8.22, 14.2–14.14, 16.4–16.15, 16.21; Design Properties: 9, 18, 20–25, 27, 31–42_
  - [~] 15.9 Add mandatory per-family screenshot/golden and accessibility matrix coverage
    - Cover required widths, themes, font scales, focus/traversal/announcements/modal behavior, all applicable states, and Customer list/detail pilot goldens specified by the design.
    - _Requirements: 4.1–4.16, 5.1–5.13, 9.6–9.20, 16.16–16.21; Design Properties: 8, 10–17, 25, 28, 32, 34, 39, 43_
  - [~] 15.10 Extend Macrobenchmarks to Customer list/detail and other performance-critical migrated lists
    - Use the representative 100-record fixture, controlled conditions, warm-up/run counts, 10-second scrolling, frame budget, and P95 release thresholds.
    - _Requirements: 10.2–10.13, 16.14, 16.15; Design Properties: 26, 42_

- [~] 16. Broad-rollout checkpoint
  - Ensure each remaining family has a complete ledger entry, passing implementation/regression/accessibility/golden/performance evidence, and no unresolved acceptance-criterion failures. Ensure all tests pass, ask the user if questions arise.

- [ ] 17. Consolidate the design-system path and produce release evidence
  - [~] 17.1 Remove accepted legacy renderers, obsolete feature flags, duplicated surface-local components, hard-coded colors/spacing/motion, and indication-free interaction implementations
    - Keep the existing route graph, MVI contracts, Koin modules, domain/data/API models, settings persistence, and shared root theme intact; remove a legacy renderer only after its modernized evidence passes.
    - _Requirements: 2.1–2.10, 7.1–7.11, 16.5–16.15; Design Properties: 3–5, 9, 16–19, 42_
  - [~] 17.2 Add static enforcement for semantic tokens, component variations, action/status terminology, pointer/ripple usage, and migration-ledger completeness
    - Fail new surface-local hard-coded design values or undocumented variations while allowing narrowly documented exceptions with required state/theme/accessibility tests.
    - _Requirements: 2.1–2.10, 6.5–6.9, 15.1, 15.2, 16.1, 16.2; Design Properties: 3–5, 14, 15, 42_
  - [~] 17.3 Run the full functional/API/domain/MVI/Koin/permission/navigation regression suite
    - Verify every workflow completes with baseline calculations, precision, validation, exact underlying displayed/submitted/stored values, request/response/error behavior, roles, destinations, modals, Back outcomes, and no duplicate operations.
    - _Requirements: 16.4–16.15; Design Properties: 9, 18, 22, 35–39, 42_
  - [~] 17.4 Run the full required screenshot/golden and verification matrix
    - Validate every modernized surface at six widths, two font scales, both themes, applicable state/accessibility scenarios, deterministic motion end states, and legacy-transition cases that remain.
    - _Requirements: 1.1–1.13, 4.1–4.16, 5.1–5.13, 16.16–16.21; Design Properties: 1–17, 20, 25, 27–34, 39, 43_
  - [~] 17.5 Run full accessibility and inclusive-interaction verification
    - Execute semantics/automated checks plus encoded TalkBack, Switch Access/keyboard, focus ring/order, 200% text, live announcements, chart alternatives, color-independent meaning, sensitive-output, and modal focus procedures.
    - _Requirements: 9.1–9.20, 15.7–15.11, 16.20; Design Properties: 4, 11–15, 17, 19, 25, 28, 34, 35, 39–41, 43_
  - [~] 17.6 Run the complete Macrobenchmark release suite against the matched Baseline Build
    - Enforce five warm-ups, 30 measurements, 100-record/10-second scroll runs, at least 90% in-budget frames, and no greater than 10% P95 first-content/navigation regression.
    - _Requirements: 10.2–10.13, 16.14, 16.15; Design Properties: 26, 42_
  - [~] 17.7 Finalize machine-validatable release evidence and migration statuses
    - Require every surface to be complete or deferred with a reason, every acceptance criterion to map to passing evidence, all four pilot approvals to remain present, and no workflow/accessibility/performance blocker.
    - _Requirements: 16.1–16.26; Design Properties: 42, 43, 44_
  - [ ]* 17.8 Integrate final brand assets or visual references through token/asset providers if they become approved
    - Do not change Material conventions or workflow behavior; rerun logo, cue, contrast, Light/Dark, golden, accessibility, regression, and performance suites after any approved replacement.
    - _Requirements: 1.3–1.8, 5.8–5.13, 16.27; Design Properties: 1, 2, 12, 13_

- [~] 18. Final checkpoint
  - Ensure every mandatory test and gate passes, the optional brand task is either completed or explicitly not applicable, all release evidence is linked, and no legacy renderer remains for a completed surface. Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and must not be implemented unless their condition applies. All correctness, accessibility, pilot approval, behavioral regression, and performance work is mandatory.
- Every Kotest property task must use one `checkAll` property in its own dedicated test class/file with at least 100 successful iterations (default 1,000 for inexpensive policies), the exact feature/property comment format from the design, edge-biased generators, shrinking, and replayable CI seeds. Dedicated files keep property tasks independently executable and safe to schedule in the same dependency wave.
- Golden tests verify presentation only; they never substitute for MVI, Koin, API/domain, role, navigation, calculation, validation, storage, or submission assertions.
- The task 14 approval checkpoint is a hard dependency. Broader rollout must not start until product and brand approvals exist for all four representative families and Design Property 44 passes.
- No task may add a route, reinterpret a domain/API error, change permission logic, alter a business calculation, or transform a stored/submitted source value.

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "2.1"] },
    { "id": 1, "tasks": ["1.2", "2.2", "2.3"] },
    { "id": 2, "tasks": ["1.3", "1.4", "2.4", "2.5", "2.6"] },
    { "id": 3, "tasks": ["1.5", "3.1", "3.2", "3.3", "3.4", "3.5", "4.1", "4.2", "5.1", "5.2", "5.3", "5.4", "6.1"] },
    { "id": 4, "tasks": ["3.6", "3.7", "3.8", "3.9", "3.10", "3.11", "3.12", "3.13", "3.14", "3.15", "3.16", "3.17", "3.18", "3.19", "3.20", "4.3", "4.4", "4.5", "4.6", "4.7", "4.8", "4.9", "4.10", "4.11", "4.12", "4.13", "5.5", "5.6", "5.7", "5.8", "5.9", "5.10", "5.11", "5.12", "5.13", "5.14", "5.15", "5.16", "5.17", "5.18", "5.19", "6.2", "6.3", "6.4"] },
    { "id": 5, "tasks": ["7.1", "7.2", "7.3", "7.4", "7.5", "7.6", "7.7"] },
    { "id": 6, "tasks": ["7.8", "7.9", "7.10", "7.11"] },
    { "id": 7, "tasks": ["7.12"] },
    { "id": 8, "tasks": ["9.1", "10.1"] },
    { "id": 9, "tasks": ["9.2", "9.3", "10.2", "11.1"] },
    { "id": 10, "tasks": ["10.3", "10.4", "10.5", "11.2", "11.3", "12.1"] },
    { "id": 11, "tasks": ["11.4", "11.5", "11.6", "12.2"] },
    { "id": 12, "tasks": ["12.3", "12.4", "12.5"] },
    { "id": 13, "tasks": ["13.1"] },
    { "id": 14, "tasks": ["13.2", "13.3", "13.4", "13.5"] },
    { "id": 15, "tasks": ["13.6"] },
    { "id": 16, "tasks": ["15.1", "15.2", "15.3", "15.4", "15.5", "15.6", "15.7"] },
    { "id": 17, "tasks": ["15.8", "15.9", "15.10"] },
    { "id": 18, "tasks": ["17.1", "17.2"] },
    { "id": 19, "tasks": ["17.3", "17.4", "17.5", "17.6"] },
    { "id": 20, "tasks": ["17.7", "17.8"] }
  ]
}
```
