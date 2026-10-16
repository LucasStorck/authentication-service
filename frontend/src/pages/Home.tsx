import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { useAuth } from '@/core/auth'
import type { User } from '@/core/auth'

export function Home() {
  const { username, getCurrentUser, logout } = useAuth()
  const navigate = useNavigate()
  const [user, setUser] = useState<User | null>(null)

  useEffect(() => {
    let active = true
    getCurrentUser()
      .then((u) => active && setUser(u))
      .catch(() => {})
    return () => {
      active = false
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [username])

  function signOut() {
    logout()
    navigate('/login')
  }

  return (
    <main className="flex min-h-screen items-center justify-center p-4">
      <Card className="w-full max-w-sm">
        <CardHeader>
          <CardTitle className="text-2xl">Hello, {username}!</CardTitle>
          <CardDescription>You are signed in.</CardDescription>
        </CardHeader>
        <CardContent className="flex flex-col gap-4">
          {user && (
            <>
              <p>
                <strong>E-mail:</strong> {user.email}
              </p>
              <div className="flex flex-wrap items-center gap-2">
                <strong>Roles:</strong>
                {user.roles.map((role) => (
                  <Badge key={role}>{role}</Badge>
                ))}
              </div>
            </>
          )}
          <Button variant="neutral" onClick={signOut}>
            Logout
          </Button>
        </CardContent>
      </Card>
    </main>
  )
}
