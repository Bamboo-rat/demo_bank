import type { Route } from './+types/loanApply'
import LoanApplication from '~/pages/loan/LoanApplication'

export function meta({}: Route.MetaArgs) {
  return [
    { title: 'Đăng ký vay vốn - KLB Bank' },
    { name: 'description', content: 'Đăng ký khoản vay online nhanh chóng' }
  ]
}

export default function LoanApplyRoute() {
  return <LoanApplication />
}
