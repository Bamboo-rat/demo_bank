import type { Route } from './+types/loanMyLoans'
import MyLoans from '~/pages/loan/MyLoans'

export function meta({}: Route.MetaArgs) {
  return [
    { title: 'Khoản vay của tôi - KLB Bank' },
    { name: 'description', content: 'Quản lý các khoản vay và hồ sơ vay' }
  ]
}

export default function LoanMyLoansRoute() {
  return <MyLoans />
}
