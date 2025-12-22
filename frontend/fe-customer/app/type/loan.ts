export interface LoanApplication {
  applicationId: string
  requestedAmount: number
  tenor: number
  purpose: string
  purposeLabel: string
  repaymentMethod: string
  repaymentMethodLabel: string
  status: string
  statusLabel: string
  monthlyIncome?: number
  createdAt: string
  reviewedAt?: string
}

export interface LoanAccount {
  loanNumber: string
  customerId: string
  approvedAmount: number
  outstandingPrincipal: number
  interestRate: number
  tenor: number
  purpose: string
  purposeLabel: string
  repaymentMethod: string
  repaymentMethodLabel: string
  startDate: string
  maturityDate: string
  status: string
  statusLabel: string
  disbursementDate?: string
  installmentsPaid?: number
  installmentsRemaining?: number
}

export interface RepaymentSchedule {
  scheduleId: string
  installmentNo: number
  dueDate: string
  principalAmount: number
  interestAmount: number
  totalAmount: number
  paidAmount?: number
  penaltyAmount?: number
  status: string
  statusLabel: string
  overdueDays?: number
}

export interface LoanPaymentHistory {
  paidAmount: number
  principalPaid: number
  interestPaid: number
  penaltyPaid?: number
  paymentMethod: string
  result: string
  paidDate: string
}

export interface CreateLoanApplicationRequest {
  requestedAmount: number
  tenor: number
  purpose: string
  repaymentMethod: string
  monthlyIncome?: number
  employmentStatus?: string
  collateralInfo?: string
  notes?: string
}

export interface RepayInstallmentRequest {
  loanAccountId: string
  scheduleId: string
  paymentMethod?: string
  notes?: string
}

export type LoanPurpose = 
  | 'PERSONAL' 
  | 'HOME_PURCHASE' 
  | 'CAR_PURCHASE' 
  | 'EDUCATION' 
  | 'BUSINESS' 
  | 'OTHER'

export type RepaymentMethod = 'EQUAL_PRINCIPAL' | 'EQUAL_INSTALLMENT'

export type LoanStatus = 
  | 'PENDING_APPROVAL'
  | 'APPROVED' 
  | 'ACTIVE' 
  | 'OVERDUE' 
  | 'CLOSED' 
  | 'REJECTED'
  | 'CANCELLED'

export type InstallmentStatus = 'PENDING' | 'PAID' | 'OVERDUE' | 'CANCELLED'
