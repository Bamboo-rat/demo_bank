export interface SavingsAccount {
  accountNumber: string
  savingsType: string
  savingsTypeName: string
  depositAmount: number
  interestRate: number
  termMonths: number
  maturityDate: string
  status: string
  statusLabel: string
  estimatedInterest: number
  maturityAmount: number
  createdAt: string
  autoRenew: boolean
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
  tenor: string  // "6M", "12M", "24M", etc.
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
