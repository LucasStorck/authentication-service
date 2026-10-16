import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { api, setUnauthorizedHandler, storage } from './api'

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

export interface CreateUserRequest {
  username: string
  email: string
  password: string
}

export interface User {
  id: string
  username: string
  email: string
  roles: string[]
}

interface AuthContextValue {
  username: string | null
  isLoggedIn: boolean
  login: (username: string, password: string) => Promise<void>
  register: (user: CreateUserRequest) => Promise<void>
  getCurrentUser: () => Promise<User>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(storage.token)
  const [username, setUsername] = useState<string | null>(storage.username)

  const logout = useCallback(() => {
    storage.clear()
    setToken(null)
    setUsername(null)
  }, [])

  useEffect(() => {
    setUnauthorizedHandler(logout)
  }, [logout])

  const value = useMemo<AuthContextValue>(
    () => ({
      username,
      isLoggedIn: token !== null,
      login: async (name, password) => {
        const res = await api<LoginResponse>('/api/login', {
          method: 'POST',
          body: JSON.stringify({ username: name, password }),
        })
        storage.save(res.accessToken, name)
        setToken(res.accessToken)
        setUsername(name)
      },
      register: (user) => api<void>('/api/user', { method: 'POST', body: JSON.stringify(user) }),
      getCurrentUser: () => api<User>(`/api/user/${encodeURIComponent(username ?? '')}`),
      logout,
    }),
    [token, username, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>')
  return ctx
}
