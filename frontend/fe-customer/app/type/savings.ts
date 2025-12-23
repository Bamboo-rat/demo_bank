export interface SavingsAccount {
  savingsAccountId: string
  savingsAccountNumber: string
  customerId: string
  sourceAccountNumber: string
  beneficiaryAccountNumber: string
  principalAmount: number
  interestRate: number
  estimatedInterest: number
  totalAmount: number
  tenor: string
  tenorMonths: number
  tenorLabel: string
  interestPaymentMethod: string
  autoRenewType: string
  startDate: string
  maturityDate: string
  status: string
  description: string
  daysUntilMaturity: number
  createdAt: string
  updatedAt: string
  // Computed for display
  accountNumber?: string
  savingsType?: string
  savingsTypeName?: string
  depositAmount?: number
  termMonths?: number
  statusLabel?: string
  maturityAmount?: number
  autoRenew?: boolean
}

export interface SavingsProduct {
  productCode: string
  productName: string
  description: string
  minAmount: number
  maxAmount: number
  termMonths: number
  interestRate: number
  earlyWithdrawalPenalty: number
}

export interface CreateSavingsRequest {
  sourceAccountNumber: string
  principalAmount: number
  tenor: string  // "SIX_MONTHS", "TWELVE_MONTHS", "TWENTY_FOUR_MONTHS", etc.
  interestPaymentMethod: string  // "END_OF_TERM", "MONTHLY", "QUARTERLY"
  autoRenewType: string  // "NONE", "PRINCIPAL_ONLY", "PRINCIPAL_AND_INTEREST"
  beneficiaryAccountNumber?: string
  description?: string
}

export interface CloseSavingsRequest {
  accountNumber: string
  destinationAccountNumber: string
  reason?: string
}

export type SavingsStatus = 'ACTIVE' | 'MATURED' | 'CLOSED' | 'PENDING'
