# ProcureFlow: Product, Workflow, and Architecture Report

**Document purpose:** Explain the product's intended users, business value, end-to-end workflow, and current software architecture.  
**Product stage:** Working procurement workflow MVP; local development setup, not a publicly deployed production service.

## 1. Executive overview

ProcureFlow is a web application for bringing an organization's purchasing requests into a consistent, reviewable workflow. It captures an employee's need, extracts useful procurement details, checks the request against purchasing rules and supplier eligibility, routes required approvals, supports a request for quotation (RFQ), records supplier quotes, and presents a scored comparison. Request history and approval decisions are recorded for traceability.

The intended outcome is a clearer hand-off between employees, approvers, procurement staff, finance, and suppliers. The application is designed to make the purchasing process more visible and consistent. It is not an ERP, accounting system, purchase-order system, contract manager, or payment processor.

## 2. Product purpose and problem addressed

Organizations often handle purchasing through a mixture of email, spreadsheets, chat, and informal approvals. That can make it difficult to determine:

- What is being requested, by whom, and when it is needed.
- Whether the request is within policy and who must approve it.
- Which suppliers meet the organization's recorded eligibility requirements.
- What quotes were received and how a supplier was recommended.
- What actions were taken during review.

ProcureFlow provides a central request record and a defined path from intake to quote comparison. Its value depends on the organization maintaining accurate policies, supplier records, user roles, and quote information.

## 3. Intended customers and users

### Target customer organizations

The likely target is a small or medium-sized organization, business unit, or internal procurement team that wants to standardize routine purchasing. It may also be useful as a workflow prototype for a larger organization evaluating procurement automation. The current implementation is an MVP and should be adapted to each organization's financial controls and operating practices before production use.

### User groups

| User | Main responsibility in ProcureFlow |
| --- | --- |
| Employee / requester | Describe a business need, submit a request, and follow its status. |
| Manager | Review requests routed to the manager approval stage and approve or reject with a comment. |
| Procurement | Maintain supplier records, review/evaluate requests, run sourcing, record quotes, and view workflow activity. |
| Finance | Review requests routed to the finance approval stage and approve or reject with a comment. |
| Administrator | Manage registered users' roles and has elevated operational access. |
| Supplier | Supplies a quotation in response to an RFQ. In the current MVP, supplier responses are recorded by an internal user; there is no separate supplier-facing account or portal. |

Registration is available in the app. New accounts should be assigned an appropriate business role by an administrator before relying on role-specific workflows.

## 4. End-to-end business workflow

### Step 1 — Submit a purchase need

An authenticated employee or business user enters a plain-language description, for example, “Need 12 laptops, budget INR 900,000 by 2026-12-15.” The intake extractor attempts to identify category, item description, quantity, budget, currency, and required date. The local extractor recognizes common categories, explicit budget/currency amounts, ISO dates, and “within N days” phrasing. It reports missing details for clarification; users should check extracted values because extraction is convenience, not a guarantee of correctness.

The request is stored with the original text, structured fields, requester, status, and timestamps. Employees can view their own requests; procurement/admin roles can access the broader intake view.

### Step 2 — Review the request and evaluate policy

Procurement or an administrator evaluates a request that is ready for policy review. The policy engine selects the active category policy, falling back to the global policy when needed. It checks the request budget against the policy maximum and counts eligible suppliers based on recorded category coverage, supplier status, compliance, approval status, rating, and risk criteria.

The evaluation can:

- **Block** the request if its budget is above the policy maximum or there are no eligible suppliers.
- **Approve for sourcing** when policy passes and no approval threshold is met.
- **Require approvals** when the configured amount thresholds are exceeded.

The result includes the selected policy, decision, rationale, eligible supplier count, and approval roles. The demo installation seeds example IT hardware, software, and global policies; they are sample starting values, not universal purchasing rules.

### Step 3 — Obtain required approvals

Approvals are created from the thresholds in the selected policy. In the current implementation, a request above the manager threshold requires manager approval. A request above the procurement threshold also requires procurement and finance approvals. Approval roles are processed in their configured order, and later approvals cannot be completed until earlier required approvals are approved. The requester cannot approve or reject their own request. A rejection moves the request to a rejected state; completing all required approvals makes it available for sourcing.

Approvers use the role-based approval queue and can approve or reject with a comment. The request detail shows its approval path and recorded activity.

### Step 4 — Create and open an RFQ

Once a request is approved for sourcing, a procurement/admin user creates an RFQ with a title, supplier instructions, and quotation deadline. The user opens the RFQ for sourcing. Supplier eligibility can be viewed according to the selected policy. The current MVP records RFQ and supplier participation in the application; it does not send real supplier email invitations or integrate with an external supplier network.

### Step 5 — Record supplier quotations

As supplier responses arrive through the organization's chosen channel, an internal authorized user records each quote against the RFQ and supplier. Quote fields include amount, currency, delivery time, warranty, payment terms, and notes. Quotes can be reviewed while the RFQ is open. The current app does not provide supplier self-service login, quote document upload, or automated email collection.

### Step 6 — Close and compare quotes

Procurement closes the RFQ after the response period. Closed RFQs with at least one quote can be evaluated. The application scores each quote using these weights:

| Criterion | Weight | Scoring direction |
| --- | ---: | --- |
| Price | 40% | Lower price scores higher relative to the lowest quote. |
| Delivery time | 20% | Shorter delivery scores higher relative to the fastest quote. |
| Warranty | 15% | Longer warranty scores higher relative to the longest warranty. |
| Supplier rating | 15% | Higher recorded rating out of 5 scores higher. |
| Supplier risk | 10% | Lower recorded risk score (0–100) scores higher. |

The result ranks quotes and identifies the highest weighted score as the recommendation. This is a decision-support aid, not an automatic award. A procurement professional should review the quote details, criteria, and business context before selecting a supplier.

### Step 7 — Retain the decision trail

Request activity records key system and user actions, such as policy evaluation, approval setup, approval/rejection, and quote evaluation. Authorized procurement/admin users can inspect the audit trail. The request detail combines current status, policy outcome, approvals, and recorded activity.

## 5. Request lifecycle at a glance

```text
Drafted in intake
       |
       v
Ready for policy review
       |
       +---- policy failure / no eligible supplier ----> Blocked
       |
       +---- no approvals required ---------------------> Approved for sourcing
       |
       +---- approvals required -----------------------> Pending approval
                                                          |
                                              reject -----+----> Rejected
                                                          |
                                               all approved
                                                          v
                                                Approved for sourcing
                                                          |
                                                          v
                                                   RFQ / sourcing
                                                          |
                                                 quote responses
                                                          |
                                                          v
                                              Closed and evaluated
```

Exact statuses and allowed transitions are enforced by backend services. A user-facing status may change as the request advances through these stages.

## 6. Current product capabilities

- Separate React single-page frontend with responsive CSS styling.
- Sign-in and registration; JWT bearer-token authentication.
- Procurement request intake, extraction feedback, list/detail view, and request history.
- Role-aware approval queues and approve/reject actions.
- Policy evaluation and eligible supplier checks.
- Supplier directory maintenance, status, and compliance information.
- RFQ creation/open/close, quote recording, and weighted quote evaluation.
- Administrator view for user listing and role assignment.
- Seeded local development administrator, example supplier, and sample policies.
- Local persistent H2 database by default; MySQL can be configured.

## 7. Architecture

### Component view

```text
Employee / approver / procurement / administrator
                       |
                       v
        React frontend (Vite development server)
        frontend/src, React + CSS + API helper
                       |
                 HTTP / JSON / JWT
                       |
              /api requests (Vite proxy)
                       |
                       v
       Spring Boot REST API (Java 17, port 8080)
   Auth/JWT | Intake | Policy | Approval | Supplier
            | RFQ/Quote | Audit | User/Roles
                       |
          Spring Data JPA / Hibernate
                       |
      H2 local database or configured MySQL
```

### Frontend

The frontend is located in `frontend/` and is intentionally separate from the Spring Boot application. React renders the dashboard and role-specific views. The shared API helper attaches the JWT to authenticated calls. Vite serves the app on port 5173 in development and proxies `/api` to `http://localhost:8080`. `npm run build` creates production assets in `frontend/dist`. When hosted separately, `VITE_API_BASE_URL` must point at the backend and the backend must allow the deployed frontend origin through CORS.

### Backend

The backend is a Java 17 Spring Boot application. Controllers expose REST endpoints; services implement business rules and transactional workflow changes; repositories use Spring Data JPA; domain entities represent users, requests, policies, approvals, suppliers, RFQs, quotes, evaluations, and audit events. Authentication uses BCrypt password hashing and signed JWTs. Spring Security checks authentication and role permissions, with method-level restrictions on sensitive resources.

Principal API areas include:

| API area | Purpose |
| --- | --- |
| `/api/auth` | Register and sign in. |
| `/api/users/me` | Current authenticated user's profile. |
| `/api/procurement/intake` | Submit requests and list requests available to the caller's role. |
| `/api/policies` | Evaluate/read policy outcomes and related policy operations. |
| `/api/approvals` | Read approval paths/queues and approve or reject. |
| `/api/suppliers` | Manage and list supplier records. |
| `/api/rfqs` | Create/open/read/close RFQs and create/read quotes. |
| `/api/rfq-evaluation` | Evaluate closed RFQs and rank quotes. |
| `/api/audit` | Read a request's activity trail. |
| `/api/admin/users` | List users and assign roles (administrator only). |

All API requests other than authentication require a valid bearer token unless an endpoint's security configuration says otherwise. Role restrictions are enforced by the backend; hiding a frontend control is not the security boundary.

### Data and environments

The default configuration uses an embedded H2 database that persists locally under `./data`. MySQL can be selected with the database environment variables described in the root README. The checked-in bootstrap admin password is for local development only. A real deployment needs a private JWT secret, a changed administrator credential, production database settings, TLS, backups, monitoring, and organization-specific CORS and access policies.

## 8. Access model and responsibility boundaries

- Employees submit and track requests but cannot approve their own request.
- Manager, finance, and procurement roles process approvals assigned to their role, subject to approval order.
- Procurement and administrators manage suppliers and perform sourcing functions.
- Administrators can assign user roles. Role changes apply to newly issued authentication tokens after sign-in.
- Policy rules determine whether the workflow is blocked, immediately released for sourcing, or sent for approval.
- The weighted quote result recommends an option but does not itself purchase, contract with, or pay a supplier.

Exact permissions are ultimately defined by the backend security configuration and service checks. Organizations should review those rules against their segregation-of-duties requirements before launch.

## 9. Scope boundaries and production-readiness gaps

The current project is a functioning local MVP, not a complete enterprise procurement suite. Based on the implemented product surface, it does not currently provide:

- Public cloud deployment, domain name, or production hosting configuration for a specific provider.
- A dedicated supplier portal, supplier registration workflow, or supplier email invitations.
- Purchase order creation, contract lifecycle management, invoice matching, payment, or ERP/accounting integration.
- Document attachment and extraction workflows for invoices, specifications, or quotes.
- Configurable organization/tenant boundaries and a production-grade policy administration console.
- Complete production operations such as managed secrets, backup/restore runbooks, observability, and incident handling.

Before a real organization uses the application for purchasing, confirm policy thresholds, user authorization, supplier data quality, audit retention, data privacy requirements, and production security configuration. This report describes the repository's implemented workflow; it is not a certification of legal or financial compliance.

## 10. Local launch

Start the backend from the project root:

```powershell
.\mvnw.cmd spring-boot:run
```

Start the React frontend in a second terminal:

```powershell
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`. The local seeded administrator is `admin@procureflow.com` with password `admin123`; replace these credentials and secrets before any shared or public deployment.

## 11. Repository map

| Path | Contents |
| --- | --- |
| `frontend/src/` | React application, API client, and CSS. |
| `frontend/vite.config.js` | Frontend development server and backend API proxy. |
| `src/main/java/com/procureflow/auth/` | Registration, login, and authentication responses. |
| `src/main/java/com/procureflow/procurement/` | Request intake and extraction. |
| `src/main/java/com/procureflow/policy/` | Procurement policy and evaluation. |
| `src/main/java/com/procureflow/approval/` | Approval workflow. |
| `src/main/java/com/procureflow/supplier/` | Supplier directory and eligibility. |
| `src/main/java/com/procureflow/rfq/` | RFQ, supplier quotes, and quote scoring. |
| `src/main/java/com/procureflow/audit/` | Workflow activity log. |
| `src/main/java/com/procureflow/config/` | Security, JWT, and local bootstrap data. |
