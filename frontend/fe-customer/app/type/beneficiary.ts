export interface Beneficiary {
  beneficiaryId: string
  customerId: string
  beneficiaryAccountNumber: string
  beneficiaryName: string
  bankCode?: string
  bankName?: string
  nickname?: string
  note?: string
  isVerified: boolean
  transferCount: number
  lastTransferDate?: string
  createdAt: string
  updatedAt?: string
}

export interface CreateBeneficiaryRequest {
  beneficiaryAccountNumber: string
  beneficiaryName: string
  bankCode?: string
  bankName?: string
  nickname?: string
  note?: string
}

export interface UpdateBeneficiaryRequest {
  beneficiaryName?: string
  bankCode?: string
  bankName?: string
  nickname?: string
  note?: string
}

export interface BeneficiaryResponse extends Beneficiary {
  displayName: string
}
