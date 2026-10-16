import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { ApiError } from '@/core/api'
import { useAuth } from '@/core/auth'
import { AuthLayout } from './AuthLayout'
import { Field } from './Field'

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export function Register() {
  const { register } = useAuth()
  const navigate = useNavigate()

  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [submitted, setSubmitted] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const errors = {
    username: !username ? 'Username is required.' : undefined,
    email: !EMAIL_PATTERN.test(email) ? 'Enter a valid e-mail.' : undefined,
    password: password.length < 6 ? 'Password must have at least 6 characters.' : undefined,
  }

  async function submit(e: FormEvent) {
    e.preventDefault()
    setSubmitted(true)
    if (errors.username || errors.email || errors.password) return
    setLoading(true)
    setError(null)
    try {
      await register({ username, email, password })
      navigate('/login?registered=1')
    } catch (err) {
      setLoading(false)
      const status = err instanceof ApiError ? err.status : 0
      setError(
        status === 409
          ? 'That username is already taken.'
          : status === 400
            ? 'Please check the fields and try again.'
            : 'Could not reach the server.',
      )
    }
  }

  return (
    <AuthLayout title="Create account!" subtitle="It only takes a moment">
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
          error={submitted ? errors.username : undefined}
        />
        <Field
          id="email"
          label="E-mail"
          type="email"
          autoComplete="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          error={submitted ? errors.email : undefined}
        />
        <Field
          id="password"
          label="Password"
          type="password"
          autoComplete="new-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          error={submitted ? errors.password : undefined}
        />
        <Button type="submit" disabled={loading}>
          {loading ? 'Creating...' : 'Create →'}
        </Button>
      </form>
      <p className="text-center text-sm">
        Already have an account?{' '}
        <Link to="/login" className="font-heading underline">
          Sign in
        </Link>
      </p>
    </AuthLayout>
  )
}
