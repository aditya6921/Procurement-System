import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Activity, ArrowDownRight, ArrowRight, ArrowUpRight, Bell, Check, CheckCircle2,
  ChevronDown, CircleHelp, ClipboardCheck, Clock3, FileCheck2, FilePlus2,
  FileText, LayoutDashboard, LogOut, Menu, PackageCheck, Plus, RefreshCw,
  Search, Settings2, ShieldCheck, ShoppingBag, Truck, Users, X,
} from 'lucide-react';
import { api, clearSession, getSavedUser, getToken, json, saveSession } from './api.js';

const CATEGORIES = ['IT_HARDWARE', 'SOFTWARE', 'OFFICE_SUPPLIES', 'PROFESSIONAL_SERVICES', 'MARKETING', 'FACILITIES', 'TRAVEL', 'LOGISTICS', 'RAW_MATERIALS', 'OTHER'];
const ROLES = ['EMPLOYEE', 'PROCUREMENT', 'MANAGER', 'FINANCE', 'ADMIN'];
const roleNames = { EMPLOYEE: 'Employee', PROCUREMENT: 'Procurement', MANAGER: 'Manager', FINANCE: 'Finance', ADMIN: 'Administrator' };
const privileged = (role) => ['ADMIN', 'PROCUREMENT'].includes(role);
const titleCase = (value = '') => value.toLowerCase().replaceAll('_', ' ').replace(/\b\w/g, (letter) => letter.toUpperCase());
const dateText = (value) => value ? new Date(value).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' }) : '—';
const money = (amount, currency = '') => amount == null ? '—' : `${currency} ${Number(amount).toLocaleString(undefined, { maximumFractionDigits: 2 })}`.trim();
const statusTone = (status = '') => ['APPROVED', 'APPROVED_FOR_SOURCING', 'COMPLETED', 'COMPLIANT'].includes(status) ? 'success' : ['REJECTED', 'BLOCKED', 'AI_FAILED', 'NON_COMPLIANT'].includes(status) ? 'danger' : ['PENDING_APPROVAL', 'PENDING', 'READY_FOR_POLICY', 'AI_PROCESSING', 'SOURCING', 'OPEN'].includes(status) ? 'warning' : 'neutral';

function Pill({ children }) { return <span className={`pill ${statusTone(children)}`}>{titleCase(children)}</span>; }
function Empty({ icon: Icon = FileText, title, text, action }) {
  return <div className="empty-state"><span className="empty-icon"><Icon size={21} /></span><strong>{title}</strong><p>{text}</p>{action}</div>;
}
function SectionHeading({ eyebrow, title, text, action }) {
  return <div className="section-heading"><div><span className="eyebrow">{eyebrow}</span><h1>{title}</h1>{text && <p>{text}</p>}</div>{action}</div>;
}
function Modal({ title, description, onClose, children, wide = false }) {
  useEffect(() => {
    const close = (event) => event.key === 'Escape' && onClose();
    window.addEventListener('keydown', close);
    return () => window.removeEventListener('keydown', close);
  }, [onClose]);
  return <div className="modal-shade" onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
    <section className={`modal ${wide ? 'modal-wide' : ''}`} role="dialog" aria-modal="true" aria-labelledby="modal-title">
      <header className="modal-heading"><div><h2 id="modal-title">{title}</h2>{description && <p>{description}</p>}</div><button className="icon-button" aria-label="Close" onClick={onClose}><X size={18} /></button></header>
      {children}
    </section>
  </div>;
}
function Field({ label, children, className = '' }) { return <label className={`field ${className}`}><span>{label}</span>{children}</label>; }
function Button({ children, variant = 'primary', icon: Icon, type = 'button', ...props }) {
  return <button type={type} className={`button button-${variant}`} {...props}>{Icon && <Icon size={15} />}{children}</button>;
}

export default function App() {
  const [user, setUser] = useState(getSavedUser);
  const [page, setPage] = useState('overview');
  const [requests, setRequests] = useState([]);
  const [approvals, setApprovals] = useState([]);
  const [suppliers, setSuppliers] = useState([]);
  const [people, setPeople] = useState([]);
  const [rfqs, setRfqs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState('');
  const [modal, setModal] = useState(null);
  const [query, setQuery] = useState('');
  const [authMode, setAuthMode] = useState('login');
  const [mobileNav, setMobileNav] = useState(false);

  const notify = useCallback((message) => {
    setToast(message);
    window.setTimeout(() => setToast(''), 3600);
  }, []);

  const refresh = useCallback(async (quiet = false) => {
    if (!getToken()) return;
    if (!quiet) setLoading(true);
    const role = getSavedUser()?.role;
    const results = await Promise.allSettled([
      api(privileged(role) ? '/api/procurement/intake' : '/api/procurement/intake/mine'),
      api('/api/approvals/pending'),
      privileged(role) ? api('/api/suppliers') : Promise.resolve([]),
      role === 'ADMIN' ? api('/api/admin/users') : Promise.resolve([]),
    ]);
    if (results[0].status === 'fulfilled') setRequests(results[0].value || []);
    else if (results[0].reason?.status === 401) signOut();
    if (results[1].status === 'fulfilled') setApprovals(results[1].value || []);
    if (results[2].status === 'fulfilled') setSuppliers(results[2].value || []);
    if (results[3].status === 'fulfilled') setPeople(results[3].value || []);
    const requestRows = results[0].status === 'fulfilled' ? results[0].value || [] : [];
    const rfqResults = await Promise.allSettled(requestRows.map((request) => api(`/api/rfqs/request/${request.id}`)));
    setRfqs(rfqResults.flatMap((result) => result.status === 'fulfilled' ? result.value || [] : []));
    if (!quiet) setLoading(false);
  }, []);

  useEffect(() => { if (user && getToken()) refresh(); }, [user, refresh]);

  function signOut() {
    clearSession(); setUser(null); setRequests([]); setApprovals([]); setSuppliers([]); setPeople([]); setRfqs([]); setPage('overview');
  }

  async function authenticate(event) {
    event.preventDefault();
    const values = Object.fromEntries(new FormData(event.currentTarget));
    try {
      const endpoint = authMode === 'login' ? '/api/auth/login' : '/api/auth/register';
      const result = await api(endpoint, json('POST', values));
      saveSession(result); setUser(result.user); notify(authMode === 'login' ? `Welcome back, ${result.user.fullName.split(' ')[0]}` : 'Your account is ready');
    } catch (error) { notify(error.message); }
  }

  async function runAction(action, successMessage, quiet = false) {
    try { const result = await action(); await refresh(quiet); if (successMessage) notify(successMessage); return result; }
    catch (error) { notify(error.message); throw error; }
  }

  const filteredRequests = useMemo(() => requests.filter((item) => {
    const extraction = item.extraction || {};
    const text = `${item.id} ${item.rawRequest} ${extraction.itemDescription || ''} ${item.status}`.toLowerCase();
    return text.includes(query.toLowerCase());
  }), [requests, query]);

  const navItems = [
    { id: 'overview', label: 'Overview', icon: LayoutDashboard },
    { id: 'requests', label: 'Requests', icon: FileText, badge: requests.length },
    { id: 'approvals', label: 'Approvals', icon: ClipboardCheck, badge: approvals.length },
    ...(privileged(user?.role) ? [{ id: 'suppliers', label: 'Suppliers', icon: Truck }] : []),
    ...(user?.role === 'ADMIN' ? [{ id: 'people', label: 'People & roles', icon: Users }] : []),
    ...(privileged(user?.role) ? [{ id: 'rfqs', label: 'RFQs & quotes', icon: ShoppingBag }] : []),
  ];

  function openRequestForm() { setModal({ kind: 'request' }); }

  async function createRequest(event) {
    event.preventDefault();
    const request = new FormData(event.currentTarget).get('request');
    try {
      const created = await api('/api/procurement/intake', json('POST', { request }));
      setModal(null); await refresh(); setPage('requests');
      notify(created.message || 'Procurement request submitted');
    } catch (error) { notify(error.message); }
  }

  async function viewRequest(request) {
    const data = { request, approvals: [], evaluation: null, audit: [] };
    const detailCalls = [api(`/api/approvals/request/${request.id}`).then((value) => { data.approvals = value; })];
    if (privileged(user.role)) {
      detailCalls.push(api(`/api/policies/evaluation/${request.id}`).then((value) => { data.evaluation = value; }).catch(() => {}));
      detailCalls.push(api(`/api/audit/request/${request.id}`).then((value) => { data.audit = value; }).catch(() => {}));
    }
    await Promise.allSettled(detailCalls);
    setModal({ kind: 'request-detail', data });
  }

  async function evaluatePolicy(request) {
    try {
      const result = await api(`/api/policies/evaluate/${request.id}`, { method: 'POST' });
      await refresh(); notify(`Policy decision: ${titleCase(result.decision)}`);
    } catch (error) { notify(error.message); }
  }

  async function createRfq(request) {
    setModal({ kind: 'rfq-create', request });
  }

  async function submitRfq(event) {
    event.preventDefault();
    const values = Object.fromEntries(new FormData(event.currentTarget));
    const request = modal.request;
    try {
      const rfq = await api(`/api/rfqs/request/${request.id}`, json('POST', {
        title: values.title || '', description: values.description || '', quotationDeadline: values.quotationDeadline,
      }));
      await api(`/api/rfqs/${rfq.id}/open`, { method: 'POST' });
      setModal(null); await refresh(); setPage('rfqs'); notify('RFQ opened and suppliers invited');
    } catch (error) { notify(error.message); }
  }

  async function actOnApproval(approval, action) {
    const comment = window.prompt(`${action === 'approve' ? 'Approval' : 'Rejection'} note (optional):`) ?? '';
    try {
      await api(`/api/approvals/${approval.id}/${action}`, json('POST', { comment }));
      await refresh(); notify(`Approval ${action === 'approve' ? 'recorded' : 'rejected'}`);
    } catch (error) { notify(error.message); }
  }

  async function saveSupplier(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const raw = Object.fromEntries(new FormData(form));
    const categories = [...form.querySelector('[name="categories"]').selectedOptions].map((option) => option.value);
    const body = {
      companyName: raw.companyName, contactEmail: raw.contactEmail, contactPerson: raw.contactPerson || null,
      categories, rating: Number(raw.rating), riskScore: Number(raw.riskScore), averageDeliveryDays: Number(raw.averageDeliveryDays),
      deliveryPerformance: Number(raw.deliveryPerformance), complianceStatus: raw.complianceStatus,
      supplierStatus: raw.supplierStatus, approvedSupplier: form.elements.approvedSupplier.checked,
      active: form.elements.active.checked,
    };
    try {
      const existing = modal.supplier;
      if (existing) await api(`/api/suppliers/${existing.id}`, json('PUT', body));
      else await api('/api/suppliers', json('POST', body));
      setModal(null); await refresh(); notify(existing ? 'Supplier updated' : 'Supplier added');
    } catch (error) { notify(error.message); }
  }

  async function updateRole(person, role) {
    try { await api(`/api/admin/users/${person.id}/role`, json('PUT', { role })); await refresh(true); notify(`${person.fullName} is now ${roleNames[role]}`); }
    catch (error) { notify(error.message); }
  }

  async function manageRfq(rfq) {
    try {
      const [request, quotes, eligibleSuppliers] = await Promise.all([
        api(privileged(user.role) ? '/api/procurement/intake' : '/api/procurement/intake/mine'),
        api(`/api/rfqs/${rfq.id}/quotes`),
        rfq.status === 'OPEN' ? api(`/api/policies/eligible-suppliers/${rfq.requestId}`).catch(() => []) : Promise.resolve([]),
      ]);
      const requestItem = request.find((item) => item.id === rfq.requestId);
      setModal({ kind: 'rfq-detail', rfq, quotes, request: requestItem, eligibleSuppliers });
    } catch (error) { notify(error.message); }
  }

  function manageRequestRfq(requestId) {
    const rfq = rfqs.find((item) => item.requestId === Number(requestId));
    if (rfq) manageRfq(rfq);
    else notify('No RFQ is linked to this request yet');
  }

  async function submitQuote(event) {
    event.preventDefault();
    const values = Object.fromEntries(new FormData(event.currentTarget));
    const rfq = modal.rfq;
    const body = {
      supplierId: Number(values.supplierId), quotedAmount: Number(values.quotedAmount), currency: values.currency.toUpperCase(),
      deliveryDays: Number(values.deliveryDays), warrantyMonths: Number(values.warrantyMonths),
      paymentTerms: values.paymentTerms || '', notes: values.notes || '',
    };
    try {
      await api(`/api/rfqs/${rfq.id}/quotes`, json('POST', body));
      const quotes = await api(`/api/rfqs/${rfq.id}/quotes`);
      setModal({ ...modal, quotes }); await refresh(true); notify('Quote submitted');
    } catch (error) { notify(error.message); }
  }

  async function closeRfq(rfq) {
    try {
      const updated = await api(`/api/rfqs/${rfq.id}/close`, { method: 'POST' });
      const quotes = await api(`/api/rfqs/${rfq.id}/quotes`);
      setModal({ ...modal, rfq: updated, quotes }); await refresh(true); notify('RFQ closed');
    } catch (error) { notify(error.message); }
  }

  async function scoreQuotes(rfq) {
    try {
      const result = await api(`/api/rfq-evaluation/${rfq.id}`, { method: 'POST' });
      const quotes = result.quotes || [];
      const winner = quotes.find((quote) => quote.id === result.recommendedQuoteId);
      setModal({ ...modal, rfq, quotes, winner }); await refresh(true);
      notify(winner ? `Recommended supplier: ${winner.supplierName}` : 'Quotes evaluated');
    } catch (error) { notify(error.message); }
  }

  if (!user || !getToken()) return <AuthScreen mode={authMode} setMode={setAuthMode} onSubmit={authenticate} />;

  const approvedCount = requests.filter((request) => ['APPROVED_FOR_SOURCING', 'SOURCING', 'COMPLETED'].includes(request.status)).length;
  const inProgressCount = requests.filter((request) => ['AI_PROCESSING', 'READY_FOR_POLICY', 'PENDING_APPROVAL', 'SOURCING'].includes(request.status)).length;
  const firstName = user.fullName.split(' ')[0];

  return <div className="app-shell">
    <aside className={`sidebar ${mobileNav ? 'sidebar-open' : ''}`}>
      <a className="brand" href="#overview" onClick={() => setPage('overview')}><span className="brand-icon">p</span><span className="brand-name">procure<span>flow</span></span></a>
      <div className="workspace-switch"><span className="workspace-dot">P</span><span><b>ProcureFlow Inc.</b><small>Workspace</small></span><ChevronDown size={15} /></div>
      <div className="nav-caption">WORKSPACE</div>
      <nav className="main-nav">{navItems.map(({ id, label, icon: Icon, badge }) => <button key={id} className={`nav-link ${page === id ? 'nav-link-active' : ''}`} onClick={() => { setPage(id); setMobileNav(false); }}><Icon size={17} strokeWidth={1.8} /><span>{label}</span>{badge > 0 && <em>{badge}</em>}</button>)}</nav>
      <div className="sidebar-bottom"><div className="help-card"><span><CircleHelp size={17} /></span><div><b>Need a hand?</b><small>Visit the quick-start guide</small></div><ArrowRight size={15} /></div><div className="profile-card"><div className="avatar">{user.fullName.split(' ').map((word) => word[0]).slice(0, 2).join('').toUpperCase()}</div><div className="profile-copy"><b>{user.fullName}</b><small>{roleNames[user.role] || titleCase(user.role)}</small></div><button className="icon-button profile-menu" title="Sign out" onClick={signOut}><LogOut size={16} /></button></div></div>
    </aside>

    <main className="main-area">
      <header className="topbar"><div className="topbar-left"><button className="mobile-menu icon-button" onClick={() => setMobileNav(!mobileNav)}><Menu size={20} /></button><div className="breadcrumb">Workspace <span>/</span> <b>{navItems.find((item) => item.id === page)?.label || 'Overview'}</b></div></div><div className="topbar-right"><div className="api-indicator"><i /> API connected</div><button className="icon-button notification-button" aria-label="Notifications"><Bell size={18} /><i /></button><div className="topbar-divider" /><span className="topbar-date">{new Date().toLocaleDateString(undefined, { weekday: 'short', month: 'short', day: 'numeric' })}</span></div></header>

      <div className="page-content">
        {page === 'overview' && <>
          <SectionHeading eyebrow="MONDAY, SEPTEMBER 28, 2026" title={`Good ${new Date().getHours() < 12 ? 'morning' : 'afternoon'}, ${firstName}`} text="Here’s what’s happening across your procurement workspace." action={<Button icon={Plus} onClick={openRequestForm}>New request</Button>} />
          <section className="welcome-banner"><div><span className="banner-kicker">PROCUREMENT, IN GOOD ORDER</span><h2>Every great purchase starts with a clear brief.</h2><p>Bring requests, approvals, and supplier quotes together in one place.</p><Button variant="light" icon={FilePlus2} onClick={openRequestForm}>Create a request <ArrowRight size={15} /></Button></div><div className="banner-art"><div className="art-orbit orbit-one"/><div className="art-orbit orbit-two"/><div className="art-paper"><div/><div/><div/><span><Check size={15}/></span></div><div className="art-star star-one">✳</div><div className="art-star star-two">✦</div><div className="art-dot"/></div></section>
          <section className="stats-grid"><StatCard icon={FileText} label="Total requests" value={requests.length} note="Across your workspace" tone="blue" /><StatCard icon={Clock3} label="In progress" value={inProgressCount} note="Moving through workflow" tone="amber" /><StatCard icon={ShieldCheck} label="Ready to source" value={approvedCount} note="Policy and approvals passed" tone="green" /><StatCard icon={ClipboardCheck} label="Your approvals" value={approvals.length} note="Waiting for your review" tone="violet" /></section>
          <div className="overview-grid"><section className="card recent-card"><div className="card-header"><div><h2>Recent requests</h2><p>Your latest procurement activity</p></div><button className="link-button" onClick={() => setPage('requests')}>View all <ArrowRight size={14}/></button></div><RequestTable requests={requests.slice(0, 5)} role={user.role} onView={viewRequest} onEvaluate={evaluatePolicy} onRfq={createRfq} onManageRfq={manageRequestRfq} compact /></section><section className="card workflow-card"><div className="card-header"><div><h2>Workflow snapshot</h2><p>Requests by stage</p></div><Activity size={17} className="muted-icon"/></div><WorkflowSnapshot requests={requests} /></section></div>
          <section className="bottom-grid"><div className="card mini-panel"><div className="mini-icon mint"><Truck size={17}/></div><div><b>{suppliers.length || (privileged(user.role) ? 0 : '—')}</b><strong>Active suppliers</strong><small>{privileged(user.role) ? 'Available for sourcing' : 'Procurement directory'}</small></div>{privileged(user.role) && <button className="circle-arrow" onClick={() => setPage('suppliers')}><ArrowUpRight size={16}/></button>}</div><div className="card mini-panel"><div className="mini-icon lavender"><ShoppingBag size={17}/></div><div><b>{rfqs.length}</b><strong>Active RFQs</strong><small>{rfqs.filter((rfq) => rfq.status === 'OPEN').length} open for quotations</small></div>{privileged(user.role) && <button className="circle-arrow" onClick={() => setPage('rfqs')}><ArrowUpRight size={16}/></button>}</div><div className="card mini-panel"><div className="mini-icon peach"><CheckCircle2 size={17}/></div><div><b>{approvals.filter((item) => item.status === 'PENDING').length}</b><strong>Awaiting your review</strong><small>Keep work moving forward</small></div><button className="circle-arrow" onClick={() => setPage('approvals')}><ArrowUpRight size={16}/></button></div></section>
        </>}

        {page === 'requests' && <><SectionHeading eyebrow="PROCUREMENT INTAKE" title="Requests" text="Submit a need and follow it from intake through sourcing." action={<Button icon={Plus} onClick={openRequestForm}>New request</Button>} /><div className="toolbar"><div className="search-box"><Search size={16}/><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search requests" /></div><Button variant="outline" icon={RefreshCw} onClick={() => refresh()}>Refresh</Button></div><section className="card table-card"><RequestTable requests={filteredRequests} role={user.role} onView={viewRequest} onEvaluate={evaluatePolicy} onRfq={createRfq} onManageRfq={manageRequestRfq} /></section></>}

        {page === 'approvals' && <><SectionHeading eyebrow="DECISION QUEUE" title="Approvals" text="Review requests assigned to your role and keep decisions moving." action={<Button variant="outline" icon={RefreshCw} onClick={() => refresh()}>Refresh</Button>} />{approvals.length ? <div className="approval-list">{approvals.map((approval) => { const request = requests.find((item) => item.id === approval.requestId); const extraction = request?.extraction || {}; return <article className="approval-card card" key={approval.id}><div className="approval-symbol"><ClipboardCheck size={19}/></div><div className="approval-main"><div className="approval-topline"><b>Request #{approval.requestId}</b><Pill>{approval.status}</Pill></div><h3>{extraction.itemDescription || 'Procurement approval'}</h3><p>{request?.rawRequest || `Approval required for ${titleCase(approval.approvalRole)}.`}</p><div className="approval-meta"><span><Users size={13}/> {titleCase(approval.approvalRole)} review</span><span><Clock3 size={13}/> Submitted {dateText(request?.createdAt || approval.createdAt)}</span>{extraction.budgetAmount != null && <span><ShoppingBag size={13}/> {money(extraction.budgetAmount, extraction.currency)}</span>}</div></div><div className="approval-actions">{approval.status === 'PENDING' && <><Button variant="outline" onClick={() => actOnApproval(approval, 'reject')}>Reject</Button><Button icon={Check} onClick={() => actOnApproval(approval, 'approve')}>Approve</Button></>}</div></article>; })}</div> : <section className="card"><Empty icon={CheckCircle2} title="Your queue is clear" text="No approvals are waiting for your role right now." /></section>}</>}

        {page === 'suppliers' && <><SectionHeading eyebrow="SUPPLIER DIRECTORY" title="Suppliers" text="Manage supplier profiles, categories, and compliance." action={<Button icon={Plus} onClick={() => setModal({ kind: 'supplier' })}>Add supplier</Button>} /><div className="toolbar"><div className="search-box"><Search size={16}/><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search suppliers" /></div><Button variant="outline" icon={RefreshCw} onClick={() => refresh()}>Refresh</Button></div><SupplierTable suppliers={suppliers.filter((supplier) => `${supplier.companyName} ${supplier.contactEmail}`.toLowerCase().includes(query.toLowerCase()))} onEdit={(supplier) => setModal({ kind: 'supplier', supplier })} onDeactivate={async (supplier) => { if (window.confirm(`Deactivate ${supplier.companyName}?`)) await runAction(() => api(`/api/suppliers/${supplier.id}`, { method: 'DELETE' }), 'Supplier deactivated'); }} /></>}

        {page === 'people' && user.role === 'ADMIN' && <><SectionHeading eyebrow="TEAM ACCESS" title="People & roles" text="Assign roles to employees. Role changes take effect when they sign in again." action={<Button variant="outline" icon={RefreshCw} onClick={() => refresh()}>Refresh</Button>} /><PeopleTable people={people} currentUser={user} onUpdateRole={updateRole} /></>}

        {page === 'rfqs' && privileged(user.role) && <><SectionHeading eyebrow="SUPPLIER SOURCING" title="RFQs & quotes" text="Track invitations, collect supplier quotes, and compare responses." action={<Button variant="outline" icon={RefreshCw} onClick={() => refresh()}>Refresh</Button>} /><RfqTable rfqs={rfqs} requests={requests} onManage={manageRfq} /></>}

        <footer className="page-footer"><span>ProcureFlow <span className="footer-dot">·</span> Procurement workspace</span><span><i/> Connected to backend</span></footer>
      </div>
    </main>

    {loading && <div className="refresh-indicator"><RefreshCw size={14} className="spin"/> Syncing workspace</div>}
    {toast && <div className="toast"><CheckCircle2 size={17}/>{toast}</div>}

    {modal?.kind === 'request' && <Modal title="New procurement request" description="Describe what you need. Add quantity, total budget, currency, and date to speed up review." onClose={() => setModal(null)}><form className="modal-form" onSubmit={createRequest}><Field label="Request details"><textarea name="request" required minLength="5" maxLength="4000" rows="5" placeholder="For example: Need 12 laptops, budget INR 900,000 by 2026-12-15" /></Field><div className="form-hint"><CircleHelp size={14}/> The local extractor will fill in details it recognizes and ask for anything missing.</div><div className="modal-actions"><Button variant="ghost" onClick={() => setModal(null)}>Cancel</Button><Button type="submit" icon={FilePlus2}>Submit request</Button></div></form></Modal>}
    {modal?.kind === 'request-detail' && <RequestDetail data={modal.data} onClose={() => setModal(null)} />}
    {modal?.kind === 'rfq-create' && <Modal title="Create a request for quotation" description={`Invite eligible suppliers to quote on request #${modal.request.id}.`} onClose={() => setModal(null)}><form className="modal-form" onSubmit={submitRfq}><Field label="RFQ title"><input name="title" placeholder={`RFQ — ${modal.request.extraction?.itemDescription || `Request ${modal.request.id}`}`} maxLength="250" /></Field><Field label="Instructions for suppliers"><textarea name="description" rows="3" maxLength="2000" placeholder="Add delivery, warranty, or service requirements" /></Field><Field label="Quotation deadline"><input name="quotationDeadline" type="datetime-local" required min={new Date(Date.now() + 60000).toISOString().slice(0, 16)} defaultValue={new Date(Date.now() + 14 * 86400000).toISOString().slice(0, 16)} /></Field><div className="modal-actions"><Button variant="ghost" onClick={() => setModal(null)}>Cancel</Button><Button type="submit" icon={ShoppingBag}>Create and open RFQ</Button></div></form></Modal>}
    {modal?.kind === 'supplier' && <SupplierModal supplier={modal.supplier} onClose={() => setModal(null)} onSubmit={saveSupplier} />}
    {modal?.kind === 'rfq-detail' && <RfqDetail modal={modal} suppliers={suppliers} onClose={() => setModal(null)} onSubmitQuote={submitQuote} onCloseRfq={closeRfq} onEvaluate={scoreQuotes} />}
  </div>;
}

function AuthScreen({ mode, setMode, onSubmit }) {
  const registering = mode === 'register';
  return <main className="auth-shell">
    <div className="auth-decoration">
      <div className="auth-glow" />
      <div className="auth-copy">
        <a className="brand auth-brand"><span className="brand-icon">p</span><span className="brand-name">procure<span>flow</span></span></a>
        <div className="auth-message">
          <span className="eyebrow">PURCHASING, MADE CLEAR</span>
          <h1>Good decisions start with a better process.</h1>
          <p>Bring every request, approval, and supplier quote into one calm, connected workspace.</p>
          <div className="auth-points">
            <span><CheckCircle2 size={16} /> Clear purchasing workflows</span>
            <span><ShieldCheck size={16} /> Controlled reviews and approvals</span>
            <span><PackageCheck size={16} /> Supplier quotes side by side</span>
          </div>
        </div>
        <span className="auth-copyright">© {new Date().getFullYear()} ProcureFlow</span>
      </div>
    </div>
    <section className="auth-panel">
      <div className="auth-form-wrap">
        <span className="eyebrow">YOUR PROCUREMENT WORKSPACE</span>
        <h2>{registering ? 'Create your account' : 'Welcome back'}</h2>
        <p>{registering ? 'Start a request or join your organization.' : 'Sign in to continue to your workspace.'}</p>
        <form className="auth-form" onSubmit={onSubmit}>
          {registering && <Field label="Full name"><input name="fullName" autoComplete="name" required minLength="2" maxLength="100" placeholder="Your name" /></Field>}
          <Field label="Work email"><input name="email" type="email" autoComplete="username" required placeholder="you@company.com" /></Field>
          <Field label="Password"><input name="password" type="password" autoComplete={registering ? 'new-password' : 'current-password'} required minLength={registering ? 8 : undefined} placeholder="Enter your password" /></Field>
          <button className="forgot-link" type="button" title="Ask your administrator to reset access">Need help signing in?</button>
          <Button type="submit" className="auth-submit">{registering ? 'Create account' : 'Sign in'} <ArrowRight size={16} /></Button>
        </form>
        <div className="auth-switch">{registering ? 'Already have an account?' : 'New to ProcureFlow?'} <button onClick={() => setMode(registering ? 'login' : 'register')}>{registering ? 'Sign in' : 'Create an account'}</button></div>
        <div className="auth-note"><ShieldCheck size={14} /> Secure access with your organization account</div>
      </div>
    </section>
  </main>;
}

function StatCard({ icon: Icon, label, value, note, tone }) {
  return <article className="stat-card"><span className={`stat-icon ${tone}`}><Icon size={18}/></span><div className="stat-details"><span>{label}</span><strong>{value}</strong><small>{note}</small></div><span className={`stat-trend trend-${tone}`}>{tone === 'green' ? <ArrowUpRight size={15}/> : tone === 'amber' ? <ArrowDownRight size={15}/> : <Activity size={14}/>}</span></article>;
}

function WorkflowSnapshot({ requests }) {
  const stages = [
    ['Needs clarification', 'NEEDS_CLARIFICATION', 'stage-gold'], ['Policy review', 'READY_FOR_POLICY', 'stage-blue'],
    ['Pending approval', 'PENDING_APPROVAL', 'stage-purple'], ['Sourcing', 'SOURCING', 'stage-teal'],
  ];
  if (!requests.length) return <p className="workflow-empty">Your request pipeline will appear here.</p>;
  return <div className="workflow-list">{stages.map(([label, status, color]) => { const count = requests.filter((item) => item.status === status).length; const percent = requests.length ? Math.round(count / requests.length * 100) : 0; return <div className="workflow-row" key={status}><div className="workflow-label"><span className={`stage-dot ${color}`}/><span>{label}</span><b>{count}</b></div><div className="progress-track"><span className={color} style={{ width: `${percent}%` }}/></div></div>; })}</div>;
}

function RequestTable({ requests, role, onView, onEvaluate, onRfq, onManageRfq, compact = false }) {
  if (!requests.length) return <Empty icon={FileText} title="No requests yet" text="Start with a short brief. You can add detail as it moves through review." />;
  return <div className="table-scroll"><table className="data-table"><thead><tr><th>REQUEST</th><th>CATEGORY</th><th>BUDGET</th><th>NEEDED BY</th><th>STATUS</th><th /></tr></thead><tbody>{requests.map((request) => { const extraction = request.extraction || {}; return <tr key={request.id}><td><button className="request-title" onClick={() => onView(request)}><span className="request-id">REQ-{String(request.id).padStart(4, '0')}</span><b>{extraction.itemDescription || request.rawRequest}</b></button></td><td>{extraction.category ? titleCase(extraction.category) : <span className="muted-text">Not set</span>}</td><td className="money-cell">{money(extraction.budgetAmount, extraction.currency)}</td><td>{dateText(extraction.deadline)}</td><td><Pill>{request.status}</Pill></td><td><div className="table-actions"><button className="more-button" onClick={() => onView(request)} title="View details"><ArrowRight size={15}/></button>{privileged(role) && request.status === 'READY_FOR_POLICY' && <button className="action-text" onClick={() => onEvaluate(request)}>Evaluate</button>}{privileged(role) && request.status === 'APPROVED_FOR_SOURCING' && <button className="action-text" onClick={() => onRfq(request)}>Create RFQ</button>}{privileged(role) && request.status === 'SOURCING' && <button className="action-text" onClick={() => onManageRfq(request.id)}>Manage RFQ</button>}</div></td></tr>; })}</tbody></table>{compact && requests.length > 0 && <div className="table-bottom"><span>Showing {requests.length} recent {requests.length === 1 ? 'request' : 'requests'}</span><span>Updated just now <i /></span></div>}</div>;
}

function SupplierTable({ suppliers, onEdit, onDeactivate }) {
  if (!suppliers.length) return <section className="card"><Empty icon={Truck} title="No suppliers found" text="Add supplier profiles to make them available for policy checks and RFQs." /></section>;
  return <section className="card table-card"><div className="table-scroll"><table className="data-table"><thead><tr><th>SUPPLIER</th><th>CATEGORIES</th><th>RATING</th><th>RISK</th><th>COMPLIANCE</th><th>STATUS</th><th /></tr></thead><tbody>{suppliers.map((supplier) => <tr key={supplier.id}><td><div className="supplier-name"><span className="supplier-avatar">{supplier.companyName.slice(0, 1).toUpperCase()}</span><span><b>{supplier.companyName}</b><small>{supplier.contactEmail}</small></span></div></td><td><div className="category-list">{(supplier.categories || []).slice(0, 2).map((item) => <span key={item}>{titleCase(item)}</span>)}{supplier.categories?.length > 2 && <span>+{supplier.categories.length - 2}</span>}</div></td><td><span className="rating">★ {Number(supplier.rating).toFixed(1)}</span></td><td>{supplier.riskScore}<small className="subtle-cell"> / 100</small></td><td><Pill>{supplier.complianceStatus}</Pill></td><td><Pill>{supplier.supplierStatus}</Pill></td><td><div className="row-buttons"><button className="small-link" onClick={() => onEdit(supplier)}>Edit</button>{supplier.active && <button className="small-link danger-link" onClick={() => onDeactivate(supplier)}>Deactivate</button>}</div></td></tr>)}</tbody></table></div></section>;
}

function PeopleTable({ people, currentUser, onUpdateRole }) {
  return <section className="card table-card"><div className="table-scroll"><table className="data-table"><thead><tr><th>TEAM MEMBER</th><th>EMAIL</th><th>ROLE</th><th>ACCESS</th></tr></thead><tbody>{people.map((person) => <tr key={person.id}><td><div className="supplier-name"><span className="supplier-avatar avatar-blue">{person.fullName.split(' ').map((part) => part[0]).slice(0, 2).join('').toUpperCase()}</span><span><b>{person.fullName}</b>{person.id === currentUser.id && <small>You</small>}</span></div></td><td>{person.email}</td><td><select className="role-select" value={person.role} disabled={person.id === currentUser.id} onChange={(event) => onUpdateRole(person, event.target.value)}>{ROLES.map((role) => <option value={role} key={role}>{roleNames[role]}</option>)}</select></td><td><span className="access-state"><i /> Active</span></td></tr>)}</tbody></table></div><div className="table-bottom"><span>{people.length} team members</span><span>Role changes apply on next sign in</span></div></section>;
}

function RfqTable({ rfqs, requests, onManage }) {
  if (!rfqs.length) return <section className="card"><Empty icon={ShoppingBag} title="No RFQs yet" text="Once a request passes policy and approvals, create an RFQ to invite eligible suppliers." /></section>;
  return <section className="card table-card"><div className="table-scroll"><table className="data-table"><thead><tr><th>RFQ</th><th>REQUEST</th><th>DEADLINE</th><th>QUOTES</th><th>STATUS</th><th /></tr></thead><tbody>{rfqs.map((rfq) => { const request = requests.find((item) => item.id === rfq.requestId); return <tr key={rfq.id}><td><b>{rfq.title || `RFQ-${String(rfq.id).padStart(4, '0')}`}</b><small className="block-subtle">Created {dateText(rfq.createdAt)}</small></td><td>{request?.extraction?.itemDescription || `Request #${rfq.requestId}`}</td><td>{dateText(rfq.quotationDeadline)}</td><td><span className="quote-count"><FileCheck2 size={14}/> Quote comparison</span></td><td><Pill>{rfq.status}</Pill></td><td><Button variant="outline" onClick={() => onManage(rfq)}>Manage RFQ <ArrowRight size={14}/></Button></td></tr>; })}</tbody></table></div></section>;
}

function RequestDetail({ data, onClose }) {
  const { request, approvals, evaluation, audit } = data;
  const extraction = request.extraction || {};
  return <Modal title={`Request REQ-${String(request.id).padStart(4, '0')}`} description="Request details, review progress, and audit activity." onClose={onClose} wide><div className="detail-layout"><div className="detail-main"><div className="detail-status"><Pill>{request.status}</Pill><span>Submitted {dateText(request.createdAt)}</span></div><h3>{extraction.itemDescription || 'Request details need clarification'}</h3><p className="detail-raw">{request.rawRequest}</p><div className="detail-fields"><DetailItem label="Quantity" value={extraction.quantity ?? '—'} /><DetailItem label="Estimated budget" value={money(extraction.budgetAmount, extraction.currency)} /><DetailItem label="Category" value={titleCase(extraction.category || 'Not set')} /><DetailItem label="Needed by" value={dateText(extraction.deadline)} /></div>{request.missingFields?.length > 0 && <div className="clarification-box"><CircleHelp size={16}/><div><b>Details needed</b><p>{request.missingFields.join(' · ')}</p></div></div>}{evaluation && <div className="evaluation-box"><ShieldCheck size={17}/><div><b>{titleCase(evaluation.decision)}</b><p>{evaluation.reason}</p><small>{evaluation.eligibleSupplierCount} eligible suppliers · {evaluation.policyName}</small></div></div>}<h4 className="subsection-title">Approval path</h4>{approvals.length ? <div className="timeline">{approvals.map((approval) => <div className="timeline-item" key={approval.id}><span className={`timeline-dot ${approval.status.toLowerCase()}`}/><div><b>{titleCase(approval.approvalRole)}</b><Pill>{approval.status}</Pill><small>{approval.approverEmail || 'Waiting for approver'}</small>{approval.comment && <p>{approval.comment}</p>}</div></div>)}</div> : <p className="muted-text">No approval steps assigned.</p>}</div><aside className="detail-side"><h4>Activity</h4>{audit.length ? <div className="audit-list">{audit.map((entry) => <div className="audit-entry" key={entry.id}><span className="audit-dot"/><div><b>{titleCase(entry.action)}</b><p>{entry.details}</p><small>{dateText(entry.createdAt)} · {entry.actorEmail}</small></div></div>)}</div> : <p className="muted-text">Activity appears here as this request moves through the workflow.</p>}</aside></div></Modal>;
}
function DetailItem({ label, value }) { return <div className="detail-item"><small>{label}</small><b>{value}</b></div>; }

function SupplierModal({ supplier, onClose, onSubmit }) {
  return <Modal title={supplier ? 'Edit supplier' : 'Add supplier'} description="Supplier details help policy checks rank compliant vendors." onClose={onClose}><form className="modal-form supplier-form" onSubmit={onSubmit}><Field label="Company name"><input name="companyName" required maxLength="200" defaultValue={supplier?.companyName} /></Field><div className="two-fields"><Field label="Contact email"><input name="contactEmail" type="email" required defaultValue={supplier?.contactEmail} /></Field><Field label="Contact person"><input name="contactPerson" defaultValue={supplier?.contactPerson} /></Field></div><Field label="Procurement categories"><select name="categories" multiple required defaultValue={supplier?.categories || []}>{CATEGORIES.map((category) => <option value={category} key={category}>{titleCase(category)}</option>)}</select><small className="field-help">Hold Ctrl (Windows) or Command (Mac) to select multiple.</small></Field><div className="three-fields"><Field label="Rating (0–5)"><input name="rating" type="number" min="0" max="5" step="0.01" required defaultValue={supplier?.rating ?? '4.2'} /></Field><Field label="Risk (0–100)"><input name="riskScore" type="number" min="0" max="100" required defaultValue={supplier?.riskScore ?? '20'} /></Field><Field label="Delivery days"><input name="averageDeliveryDays" type="number" min="1" required defaultValue={supplier?.averageDeliveryDays ?? '14'} /></Field></div><div className="two-fields"><Field label="Delivery performance %"><input name="deliveryPerformance" type="number" min="0" max="100" step="0.01" required defaultValue={supplier?.deliveryPerformance ?? '95'} /></Field><Field label="Compliance"><select name="complianceStatus" defaultValue={supplier?.complianceStatus || 'COMPLIANT'}><option value="COMPLIANT">Compliant</option><option value="PENDING_REVIEW">Pending review</option><option value="NON_COMPLIANT">Non-compliant</option></select></Field></div><Field label="Supplier status"><select name="supplierStatus" defaultValue={supplier?.supplierStatus || 'ACTIVE'}><option value="ACTIVE">Active</option><option value="SUSPENDED">Suspended</option><option value="BLOCKED">Blocked</option></select></Field><div className="check-row"><label><input type="checkbox" name="approvedSupplier" defaultChecked={supplier?.approvedSupplier ?? true}/> Approved supplier</label><label><input type="checkbox" name="active" defaultChecked={supplier?.active ?? true}/> Active</label></div><div className="modal-actions"><Button variant="ghost" onClick={onClose}>Cancel</Button><Button type="submit" icon={Check}>{supplier ? 'Save changes' : 'Add supplier'}</Button></div></form></Modal>;
}

function RfqDetail({ modal, suppliers: _suppliers, onClose, onSubmitQuote, onCloseRfq, onEvaluate }) {
  const { rfq, quotes, request, winner } = modal;
  const suppliers = modal.eligibleSuppliers || [];
  const extraction = request?.extraction || {};
  const canQuote = rfq.status === 'OPEN';
  return <Modal title={rfq.title || `RFQ-${String(rfq.id).padStart(4, '0')}`} description={`Request #${rfq.requestId} · quotation deadline ${dateText(rfq.quotationDeadline)}`} onClose={onClose} wide><div className="rfq-detail"><div className="rfq-summary"><div><span>STATUS</span><Pill>{rfq.status}</Pill></div><div><span>REQUEST BUDGET</span><b>{money(extraction.budgetAmount, extraction.currency)}</b></div><div><span>QUOTES RECEIVED</span><b>{quotes.length}</b></div></div>{winner && <div className="winner-banner"><span className="winner-crown">★</span><div><b>Recommended supplier: {winner.supplierName}</b><p>Weighted score {winner.totalScore} · {money(winner.quotedAmount, winner.currency)}</p></div></div>}<div className="quote-section-heading"><div><h3>Supplier responses</h3><p>Quotes are ranked after closing the RFQ.</p></div>{rfq.status === 'CLOSED' && <Button variant="outline" onClick={() => onEvaluate(rfq)}>Evaluate quotes</Button>}</div>{quotes.length ? <div className="table-scroll quote-table"><table className="data-table"><thead><tr><th>SUPPLIER</th><th>PRICE</th><th>DELIVERY</th><th>WARRANTY</th><th>SCORE</th></tr></thead><tbody>{quotes.map((quote) => <tr key={quote.id}><td><b>{quote.supplierName}</b></td><td>{money(quote.quotedAmount, quote.currency)}</td><td>{quote.deliveryDays} days</td><td>{quote.warrantyMonths} months</td><td>{quote.totalScore != null ? <strong>{quote.totalScore}</strong> : <span className="muted-text">Awaiting evaluation</span>}</td></tr>)}</tbody></table></div> : <Empty icon={FileCheck2} title="No supplier responses yet" text="Record a quote below when a supplier responds." />}{canQuote && <form className="quote-form" onSubmit={onSubmitQuote}><h3>Record a supplier quote</h3><div className="three-fields"><Field label="Invited supplier"><select name="supplierId" required defaultValue=""><option value="" disabled>Select supplier</option>{suppliers.map((supplier) => <option value={supplier.id} key={supplier.id}>{supplier.companyName}</option>)}</select></Field><Field label="Quote amount"><input name="quotedAmount" required type="number" min="0.01" step="0.01" defaultValue={extraction.budgetAmount || ''} /></Field><Field label="Currency"><input name="currency" required minLength="3" maxLength="3" defaultValue={extraction.currency || 'INR'} /></Field></div><div className="three-fields"><Field label="Delivery (days)"><input name="deliveryDays" type="number" min="1" required defaultValue="14" /></Field><Field label="Warranty (months)"><input name="warrantyMonths" type="number" min="0" required defaultValue="12" /></Field><Field label="Payment terms"><input name="paymentTerms" maxLength="100" placeholder="Net 30" /></Field></div><Field label="Notes"><input name="notes" maxLength="2000" placeholder="Optional supplier notes" /></Field><div className="rfq-form-actions"><Button type="submit" icon={FileCheck2}>Record quote</Button><Button variant="outline" onClick={() => onCloseRfq(rfq)}>Close RFQ</Button></div></form>}</div></Modal>;
}
