import type { Route } from './+types/savingsOpen'
import OpenSavingsAccount from '~/pages/savings/OpenSavingsAccount'

export function meta({}: Route.MetaArgs) {
  return [
    { title: 'Mở sổ tiết kiệm - KLB Bank' },
    { name: 'description', content: 'Gửi tiết kiệm online với lãi suất hấp dẫn' }
  ]
}

export default function SavingsOpenRoute() {
  return <OpenSavingsAccount />
}
