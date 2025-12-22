import type { Route } from './+types/savingsClose'
import CloseSavingsAccount from '~/pages/savings/CloseSavingsAccount'

export function meta({}: Route.MetaArgs) {
  return [
    { title: 'Tất toán sổ tiết kiệm - KLB Bank' },
    { name: 'description', content: 'Tất toán sổ tiết kiệm online' }
  ]
}

export default function SavingsCloseRoute() {
  return <CloseSavingsAccount />
}
