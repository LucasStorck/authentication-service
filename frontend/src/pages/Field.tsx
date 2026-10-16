import type { ComponentProps } from 'react'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'

interface Props extends ComponentProps<'input'> {
  id: string
  label: string
  error?: string
}

export function Field({ id, label, error, ...props }: Props) {
  return (
    <div className="flex flex-col gap-1.5">
      <Label htmlFor={id}>{label}</Label>
      <Input id={id} name={id} aria-invalid={error ? true : undefined} {...props} />
      {error && <p className="text-sm font-heading text-red-600">{error}</p>}
    </div>
  )
}
