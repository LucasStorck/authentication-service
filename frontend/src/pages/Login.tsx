import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { ApiError } from '@/core/api'
import { useAuth } from '@/core/auth'
import { AuthLayout } from './AuthLayout'
import { Field } from './Field'

export function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const registered = useSearchParams()[0].has('registered')

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [submitted, setSubmitted] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function submit(e: FormEvent) {
    e.preventDefault()
    setSubmitted(true)
    if (!username || !password) return
    setLoading(true)
    setError(null)
    try {
      await login(username, password)
      navigate('/home')
    } catch (err) {
      setLoading(false)
      setError(
        err instanceof ApiError && err.status === 401
          ? 'Invalid username or password.'
          : 'Could not reach the server.',
      )
    }
  }

  return (
    <AuthLayout title="Welcome!" subtitle="Sign in to your account">
      {registered && (
        <Alert>
          <AlertDescription>Account created. You can sign in now.</AlertDescription>
        </Alert>
      )}
      {error && (
        <Alert variant="destructive">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}
      <form onSubmit={submit} noValidate className="flex flex-col gap-4">
        <Field
          id="username"
          label="Username"
          autoComplete="username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          error={submitted && !username ? 'Username is required.' : undefined}
        />
        <Field
          id="password"
          label="Password"
          type="password"
          autoComplete="current-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          error={submitted && !password ? 'Password is required.' : undefined}
        />
        <Button type="submit" disabled={loading}>
          {loading ? 'Signing in...' : 'Login →'}
        </Button>
      </form>
      <p className="text-center text-sm">
        No account yet?{' '}
        <Link to="/register" className="font-heading underline">
          Create one
        </Link>
      </p>
    </AuthLayout>
  )
}
