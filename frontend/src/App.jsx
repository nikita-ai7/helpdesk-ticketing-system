import { useCallback, useEffect, useMemo, useState } from 'react'
import './App.css'

const API = '/api'

const emptyTicket = {
  subject: '',
  description: '',
  priority: 'MEDIUM',
}

function App() {
  const [credentials, setCredentials] = useState(null)
  const [loginForm, setLoginForm] = useState({
    organization: 'acme',
    email: 'admin@acme.test',
    password: 'Password@123',
  })
  const [role, setRole] = useState('')
  const [userLabel, setUserLabel] = useState('')
  const [tickets, setTickets] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [priorityFilter, setPriorityFilter] = useState('')
  const [selectedTicket, setSelectedTicket] = useState(null)
  const [comments, setComments] = useState([])
  const [history, setHistory] = useState([])
  const [agents, setAgents] = useState([])
  const [commentText, setCommentText] = useState('')
  const [internalNote, setInternalNote] = useState(false)
  const [newTicket, setNewTicket] = useState(emptyTicket)
  const [showCreate, setShowCreate] = useState(false)
  const [assigneeId, setAssigneeId] = useState('')
  const [newStatus, setNewStatus] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  const isCustomer = role === 'CUSTOMER'
  const canManage = role === 'ORG_ADMIN' || role === 'AGENT'

  const request = useCallback(async (path, options = {}) => {
    if (!credentials) throw new Error('Please sign in again.')

    const response = await fetch(`${API}${path}`, {
      ...options,
      headers: {
        Authorization: `Basic ${btoa(`${credentials.username}:${credentials.password}`)}`,
        ...(options.body ? { 'Content-Type': 'application/json' } : {}),
        ...options.headers,
      },
    })

    if (response.status === 401) {
      setCredentials(null)
      throw new Error('Your login is no longer valid. Please sign in again.')
    }

    if (!response.ok) {
      let message = `Request failed (${response.status})`
      try {
        const data = await response.json()
        message = data.message || data.detail || message
      } catch {
        // Some security responses have no JSON body.
      }
      throw new Error(message)
    }

    if (response.status === 204) return null
    return response.json()
  }, [credentials])

  const loadTickets = useCallback(async () => {
    if (!credentials) return
    setLoading(true)
    setError('')
    try {
      const params = new URLSearchParams({ page: String(page), size: '10' })
      if (search.trim()) params.set('search', search.trim())
      if (statusFilter) params.set('status', statusFilter)
      if (priorityFilter) params.set('priority', priorityFilter)

      const data = await request(`/tickets?${params.toString()}`)
      setTickets(data.content || [])
      setTotalPages(data.totalPages || 0)
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }, [credentials, page, search, statusFilter, priorityFilter, request])

  useEffect(() => {
    if (credentials) loadTickets()
  }, [credentials, loadTickets])

async function signIn(event) {
  event.preventDefault()
  setError('')
  setNotice('')
  setLoading(true)

  const nextCredentials = {
    username: `${loginForm.organization.trim()}|${loginForm.email.trim()}`,
    password: loginForm.password,
  }

  try {
    const response = await fetch(`${API}/users/me`, {
      headers: {
        Authorization: `Basic ${btoa(
          `${nextCredentials.username}:${nextCredentials.password}`,
        )}`,
      },
    })

    if (!response.ok) {
      throw new Error(
        response.status === 401
          ? 'Invalid organization, email, or password.'
          : `Could not sign in (${response.status}).`,
      )
    }

    const user = await response.json()

    setCredentials(nextCredentials)
    setRole(user.role)
    setUserLabel(user.name)
    setPage(0)
    setTickets([])
    setNotice(`Welcome, ${user.name}!`)
  } catch (e) {
    setError(e.message)
  } finally {
    setLoading(false)
  }
}

  function signOut() {
    setCredentials(null)
    setTickets([])
    setSelectedTicket(null)
    setComments([])
    setHistory([])
    setAgents([])
    setRole('')
    setUserLabel('')
    setNotice('')
    setError('')
  }

  async function createTicket(event) {
    event.preventDefault()
    try {
      const created = await request('/tickets', {
        method: 'POST',
        body: JSON.stringify(newTicket),
      })
      setNewTicket(emptyTicket)
      setShowCreate(false)
      setPage(0)
      setNotice(`Ticket #${created.id} created successfully.`)
      await loadTickets()
      await openTicket(created)
    } catch (e) {
      setError(e.message)
    }
  }

async function openTicket(ticket) {
  setError('')
  setNotice('')
  setSelectedTicket(ticket)
  setAssigneeId(ticket.assigneeId ? String(ticket.assigneeId) : '')
  setNewStatus(ticket.status)

  try {
    const [commentData, historyData, agentData] = await Promise.all([
      request(`/tickets/${ticket.id}/comments`),
      canManage
        ? request(`/tickets/${ticket.id}/history`).catch(() => [])
        : Promise.resolve([]),
      canManage
        ? request('/users/agents').catch(() => [])
        : Promise.resolve([]),
    ])

    setComments(commentData || [])
    setHistory(historyData || [])
    setAgents(agentData || [])
  } catch (e) {
    setComments([])
    setHistory([])
    setAgents([])
    setError(e.message)
  }
}

  async function addComment(event) {
    event.preventDefault()
    if (!selectedTicket || !commentText.trim()) return
    try {
      await request(`/tickets/${selectedTicket.id}/comments`, {
        method: 'POST',
        body: JSON.stringify({
          body: commentText.trim(),
          internalNote: canManage && internalNote,
        }),
      })
      setCommentText('')
      setInternalNote(false)
      const data = await request(`/tickets/${selectedTicket.id}/comments`)
      setComments(data || [])
      setNotice('Comment added.')
    } catch (e) {
      setError(e.message)
    }
  }

  async function updateStatus() {
    if (!selectedTicket || !newStatus) return
    try {
      const updated = await request(`/tickets/${selectedTicket.id}/status`, {
        method: 'PATCH',
        body: JSON.stringify({ status: newStatus }),
      })
      setSelectedTicket(updated)
      setNotice('Ticket status updated.')
      await loadTickets()
      await openTicket(updated)
    } catch (e) {
      setError(e.message)
    }
  }

  async function assignTicket() {
    if (!selectedTicket || !assigneeId) {
      setError('Enter an agent ID to assign this ticket.')
      return
    }
    try {
      const updated = await request(`/tickets/${selectedTicket.id}/assignee`, {
        method: 'PATCH',
        body: JSON.stringify({ assigneeId: Number(assigneeId) }),
      })
      setSelectedTicket(updated)
      setNotice('Ticket assigned successfully.')
      await loadTickets()
      await openTicket(updated)
    } catch (e) {
      setError(e.message)
    }
  }

  const openCount = useMemo(
    () => tickets.filter((ticket) => ticket.status === 'OPEN').length,
    [tickets],
  )

  if (!credentials) {
    return (
      <main className="login-page">
        <section className="login-card">
          <div className="brand-mark">H</div>
          <p className="eyebrow">SUPPORT WORKSPACE</p>
          <h1>Welcome back</h1>
          <p className="muted">Sign in to manage your support tickets.</p>

          {error && <div className="alert error">{error}</div>}

          <form onSubmit={signIn} className="stack-form">
            <label>
              Organization slug
              <input
                value={loginForm.organization}
                onChange={(e) => setLoginForm({ ...loginForm, organization: e.target.value })}
                placeholder="acme"
                required
              />
            </label>
            <label>
              Email
              <input
                type="email"
                value={loginForm.email}
                onChange={(e) => setLoginForm({ ...loginForm, email: e.target.value })}
                placeholder="admin@acme.test"
                required
              />
            </label>
            <label>
              Password
              <input
                type="password"
                value={loginForm.password}
                onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })}
                required
              />
            </label>
            <button className="primary full-width" disabled={loading}>
              {loading ? 'Signing in…' : 'Sign in'}
            </button>
          </form>

          <div className="demo-box">
            <strong>Demo accounts</strong>
            <p>Organization: <code>acme</code> or <code>globex</code></p>
            <p>Admin: <code>admin@acme.test</code></p>
            <p>Agent: <code>agent@acme.test</code></p>
            <p>Customer: <code>customer@acme.test</code></p>
            <p>Password: <code>Password@123</code></p>
            <small>Demo credentials are for local development only.</small>
          </div>
        </section>
      </main>
    )
  }

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <div className="brand-mark">H</div>
          <div><strong>Helpdesk</strong><small>Support workspace</small></div>
        </div>
        <div className="nav-label">WORKSPACE</div>
        <button className="nav-item active" onClick={() => setSelectedTicket(null)}>
          <span>▦</span> All tickets
        </button>
        <button className="nav-item" onClick={() => { setStatusFilter('OPEN'); setPage(0); setSelectedTicket(null) }}>
          <span>◷</span> Open tickets
        </button>
        <div className="sidebar-bottom">
          <div className="avatar">{userLabel.slice(0, 1).toUpperCase()}</div>
          <div className="profile-copy"><strong>{userLabel}</strong><small>{role.replace('_', ' ')}</small></div>
          <button className="icon-button" title="Sign out" onClick={signOut}>↪</button>
        </div>
      </aside>

      <section className="main-panel">
        <header className="topbar">
          <div><span className="breadcrumb">Workspace</span><span className="breadcrumb-separator">/</span><strong>{selectedTicket ? `Ticket #${selectedTicket.id}` : 'Tickets'}</strong></div>
          <div className="topbar-actions">
            <span className="user-chip">{loginForm.organization}</span>
            <button className="secondary" onClick={signOut}>Sign out</button>
          </div>
        </header>

        <div className="content">
          {error && <div className="alert error">{error}<button onClick={() => setError('')}>Dismiss</button></div>}
          {notice && <div className="alert success">{notice}<button onClick={() => setNotice('')}>Dismiss</button></div>}

          {!selectedTicket ? (
            <>
              <div className="page-heading">
                <div>
                  <p className="eyebrow">SUPPORT OVERVIEW</p>
                  <h1>Tickets</h1>
                  <p className="muted">Track requests, resolve issues, and keep everyone in the loop.</p>
                </div>
                <button className="primary" onClick={() => setShowCreate(!showCreate)}>＋ New ticket</button>
              </div>

              <div className="stats-grid">
                <article className="stat-card"><span>Total on this page</span><strong>{tickets.length}</strong><small>Current results</small></article>
                <article className="stat-card"><span>Open on this page</span><strong>{openCount}</strong><small>Needs attention</small></article>
                <article className="stat-card"><span>Total pages</span><strong>{totalPages}</strong><small>Based on filters</small></article>
              </div>

              {showCreate && (
                <form className="panel create-form" onSubmit={createTicket}>
                  <div className="panel-heading"><h2>Create a ticket</h2><button type="button" className="icon-button" onClick={() => setShowCreate(false)}>×</button></div>
                  <label>Subject<input maxLength="200" required value={newTicket.subject} onChange={(e) => setNewTicket({ ...newTicket, subject: e.target.value })} placeholder="Briefly describe the issue" /></label>
                  <label>Description<textarea maxLength="10000" required rows="4" value={newTicket.description} onChange={(e) => setNewTicket({ ...newTicket, description: e.target.value })} placeholder="Add details that will help us investigate" /></label>
                  <label>Priority<select value={newTicket.priority} onChange={(e) => setNewTicket({ ...newTicket, priority: e.target.value })}><option>LOW</option><option>MEDIUM</option><option>HIGH</option><option>URGENT</option></select></label>
                  <button className="primary" disabled={loading}>Create ticket</button>
                </form>
              )}

              <section className="panel">
                <div className="panel-heading">
                  <div><h2>All tickets</h2><p className="muted">Search and filter requests</p></div>
                  <button className="secondary" onClick={loadTickets}>↻ Refresh</button>
                </div>
                <div className="filters">
                  <input aria-label="Search tickets" placeholder="Search subject or description…" value={search} onChange={(e) => { setSearch(e.target.value); setPage(0) }} onKeyDown={(e) => e.key === 'Enter' && loadTickets()} />
                  <select aria-label="Filter by status" value={statusFilter} onChange={(e) => { setStatusFilter(e.target.value); setPage(0) }}><option value="">All statuses</option><option>OPEN</option><option>IN_PROGRESS</option><option>RESOLVED</option><option>CLOSED</option></select>
                  <select aria-label="Filter by priority" value={priorityFilter} onChange={(e) => { setPriorityFilter(e.target.value); setPage(0) }}><option value="">All priorities</option><option>LOW</option><option>MEDIUM</option><option>HIGH</option><option>URGENT</option></select>
                  <button className="secondary" onClick={loadTickets}>Search</button>
                </div>

                {loading ? <div className="empty-state">Loading tickets…</div> : tickets.length === 0 ? <div className="empty-state"><span>▤</span><strong>No tickets found</strong><p>Try changing your filters or create a new ticket.</p></div> : (
                  <div className="table-wrap">
                    <table>
                      <thead><tr><th>Ticket</th><th>Requester</th><th>Priority</th><th>Status</th><th>Assignee</th><th>Created</th></tr></thead>
                      <tbody>
                        {tickets.map((ticket) => (
                          <tr key={ticket.id} className="ticket-row" onClick={() => openTicket(ticket)}>
                            <td><strong>#{ticket.id} {ticket.subject}</strong><small>{ticket.description}</small></td>
                            <td>{ticket.requesterName}</td>
                            <td><span className={`priority priority-${ticket.priority.toLowerCase()}`}>{ticket.priority}</span></td>
                            <td><span className={`status status-${ticket.status.toLowerCase()}`}>{ticket.status.replace('_', ' ')}</span></td>
                            <td>{ticket.assigneeName || 'Unassigned'}</td>
                            <td>{ticket.createdAt ? new Date(ticket.createdAt).toLocaleDateString() : '—'}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}

                <div className="pagination">
                  <span>Page {page + 1} of {Math.max(totalPages, 1)}</span>
                  <div><button className="secondary" disabled={page <= 0} onClick={() => setPage(page - 1)}>Previous</button><button className="secondary" disabled={totalPages === 0 || page + 1 >= totalPages} onClick={() => setPage(page + 1)}>Next</button></div>
                </div>
              </section>
            </>
          ) : (
            <section className="ticket-detail">
              <button className="back-link" onClick={() => { setSelectedTicket(null); setError('') }}>← Back to tickets</button>
              <div className="page-heading">
                <div><p className="eyebrow">TICKET #{selectedTicket.id}</p><h1>{selectedTicket.subject}</h1><p className="muted">Created {selectedTicket.createdAt ? new Date(selectedTicket.createdAt).toLocaleString() : 'recently'}</p></div>
                <span className={`status status-${selectedTicket.status.toLowerCase()}`}>{selectedTicket.status.replace('_', ' ')}</span>
              </div>
              <div className="detail-grid">
                <div className="detail-main">
                  <section className="panel">
                    <div className="panel-heading"><h2>Description</h2><span className={`priority priority-${selectedTicket.priority.toLowerCase()}`}>{selectedTicket.priority}</span></div>
                    <p className="description-text">{selectedTicket.description}</p>
                    <div className="ticket-meta"><span>Requester<strong>{selectedTicket.requesterName}</strong></span><span>Assignee<strong>{selectedTicket.assigneeName || 'Unassigned'}</strong></span></div>
                  </section>

                  <section className="panel">
                    <div className="panel-heading"><h2>Conversation</h2><span className="count">{comments.length}</span></div>
                    <div className="comment-list">
                      {comments.length === 0 ? <p className="muted">No comments yet.</p> : comments.map((comment) => (
                        <article className={`comment ${comment.internalNote ? 'internal-comment' : ''}`} key={comment.id}>
                          <div className="comment-avatar">{comment.authorName?.slice(0, 1).toUpperCase()}</div>
                          <div className="comment-body"><div className="comment-heading"><strong>{comment.authorName}</strong><span>{new Date(comment.createdAt).toLocaleString()}</span></div>{comment.internalNote && <span className="internal-label">Internal note</span>}<p>{comment.body}</p></div>
                        </article>
                      ))}
                    </div>
                    <form className="comment-form" onSubmit={addComment}>
                      <label htmlFor="commentBody">Add a reply</label>
                      <textarea id="commentBody" rows="3" value={commentText} onChange={(e) => setCommentText(e.target.value)} placeholder="Write a reply…" required />
                      {canManage && <label className="checkbox-label"><input type="checkbox" checked={internalNote} onChange={(e) => setInternalNote(e.target.checked)} /> Add as internal note</label>}
                      <button className="primary" disabled={!commentText.trim()}>Send reply</button>
                    </form>
                  </section>

                  {canManage && <section className="panel">
                    <div className="panel-heading"><h2>Ticket history</h2></div>
                    {history.length === 0 ? <p className="muted">No history available.</p> : <div className="history-list">{history.map((item) => <div className="history-item" key={item.id}><span className="history-dot" /><div><strong>{item.action.replace('_', ' ')}</strong><p>{item.oldValue ? `${item.oldValue} → ` : ''}{item.newValue || '—'}</p><small>{item.changedByName} · {new Date(item.createdAt).toLocaleString()}</small></div></div>)}</div>}
                  </section>}
                </div>

                <aside className="detail-side">
                  <section className="panel">
                    <h2>Ticket properties</h2>
                    <label>Status<select value={newStatus} disabled={!canManage} onChange={(e) => setNewStatus(e.target.value)}><option>OPEN</option><option>IN_PROGRESS</option><option>RESOLVED</option><option>CLOSED</option></select></label>
                    {canManage && <button className="primary full-width" onClick={updateStatus} disabled={newStatus === selectedTicket.status}>Update status</button>}
                    <div className="side-divider" />
                  
{canManage && (
  <>
    <label>
      Assign to agent
      <select
        value={assigneeId}
        onChange={(e) => setAssigneeId(e.target.value)}
      >
        <option value="">
  {agents.length ? 'Select an agent' : 'No agents available'}
</option>
        {agents.map((agent) => (
          <option key={agent.id} value={String(agent.id)}>
            {agent.name} ({agent.email})
          </option>
        ))}
      </select>
    </label>

    <button
      className="secondary full-width"
      onClick={assignTicket}
      disabled={!assigneeId || String(selectedTicket.assigneeId || '') === assigneeId}
    >
      Assign ticket
    </button>

    <p className="hint">
      Only agents from your organization are available.
    </p>
  </>
)}
                     </section>
                </aside>
              </div>
            </section>
          )}
          <footer className="footer">Helpdesk · Multi-tenant support workspace</footer>
        </div>
      </section>
    </main>
  )
}

export default App