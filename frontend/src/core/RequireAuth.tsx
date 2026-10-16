import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from './auth'

export function RequireAuth() {
  const { isLoggedIn } = useAuth()
  return isLoggedIn ? <Outlet /> : <Navigate to="/login" replace />
}
