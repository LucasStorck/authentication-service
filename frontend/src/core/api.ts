export class ApiError extends Error {
  readonly status: number

  constructor(status: number) {
    super(`Request failed with status ${status}`)
    this.status = status
  }
}

const ACCESS_TOKEN_KEY = 'access_token'
const USERNAME_KEY = 'username'

export const storage = {
  get token() {
    return localStorage.getItem(ACCESS_TOKEN_KEY)
  },
  get username() {
    return localStorage.getItem(USERNAME_KEY)
  },
  save(token: string, username: string) {
    localStorage.setItem(ACCESS_TOKEN_KEY, token)
    localStorage.setItem(USERNAME_KEY, username)
  },
  clear() {
    localStorage.removeItem(ACCESS_TOKEN_KEY)
    localStorage.removeItem(USERNAME_KEY)
  },
}

const PUBLIC_URLS = ['/api/login', '/api/refresh', '/api/user']

let onUnauthorized: () => void = () => {}

/** Registers the handler called when a protected request comes back 401. */
export function setUnauthorizedHandler(handler: () => void) {
  onUnauthorized = handler
}

export async function api<T>(path: string, init: RequestInit = {}): Promise<T> {
  const method = init.method ?? 'GET'
  const isPublic = method === 'POST' && PUBLIC_URLS.includes(path)
  const headers = new Headers(init.headers)
  if (init.body) headers.set('Content-Type', 'application/json')
  const token = storage.token
  if (token && !isPublic) headers.set('Authorization', `Bearer ${token}`)

  let res: Response
  try {
    res = await fetch(path, { ...init, headers })
  } catch {
    throw new ApiError(0)
  }

  if (!res.ok) {
    // Expired/invalid token on a protected call: drop the session.
    if (res.status === 401 && !isPublic) onUnauthorized()
    throw new ApiError(res.status)
  }
  const text = await res.text()
  return (text ? JSON.parse(text) : undefined) as T
}
