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
  savingsType: string
  depositAmount: number
  termMonths: number
  autoRenew: boolean
}

export interface CloseSavingsRequest {
  accountNumber: string
  destinationAccountNumber: string
  reason?: string
}

export type SavingsStatus = 'ACTIVE' | 'MATURED' | 'CLOSED' | 'PENDING'
