import type { Route } from './+types/loanPay'
import LoanPayment from '~/pages/loan/LoanPayment'

export function meta({}: Route.MetaArgs) {
  return [
    { title: 'Thanh toán khoản vay - KLB Bank' },
    { name: 'description', content: 'Thanh toán các kỳ nợ vay' }
  ]
}

export default function LoanPayRoute() {
  return <LoanPayment />
}
