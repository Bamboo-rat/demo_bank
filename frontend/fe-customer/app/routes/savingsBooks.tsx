import type { Route } from './+types/savingsBooks'
import SavingsAccountList from '~/pages/savings/SavingsAccountList'

export function meta({}: Route.MetaArgs) {
  return [
    { title: 'Sổ tiết kiệm - KLB Bank' },
    { name: 'description', content: 'Quản lý sổ tiết kiệm của bạn' }
  ]
}

export default function SavingsBooksRoute() {
  return <SavingsAccountList />
}
