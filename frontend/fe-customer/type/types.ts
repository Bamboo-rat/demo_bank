export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
}

export interface RegistrationStartResponse {
  phoneNumber: string
  otpTtlSeconds: number
}

export interface RegistrationSessionResponse {
  sessionId: string
  phoneNumber: string
  status: string
  expiresAt: string
  kycStatus?: string
}

export interface RegistrationProfilePayload {
  sessionId: string
  phoneNumber: string
  password: string
  fullName: string
  dateOfBirth: string
  gender: string
  nationality: string
  email: string
  occupation?: string
  position?: string
}

export interface RegistrationIdentityPayload {
  sessionId: string
  phoneNumber: string
  nationalId: string
  issueDateNationalId: string
  placeOfIssueNationalId: string
  permanentAddress: AddressRequest
  temporaryAddress?: AddressRequest
  documentFrontImage?: string | null
  documentBackImage?: string | null
  selfieImage?: string | null
}

export interface RegistrationCompletePayload {
  sessionId: string
  phoneNumber: string
}

export interface CustomerResponse {
  customerId: string
  fullName: string
  phoneNumber: string
  email: string
  dateOfBirth?: string
  gender?: string
  nationality?: string
  nationalId?: string
  cifNumber?: string
  status: string
  kycStatus?: string
  riskLevel?: string
  createdAt?: string
  permanentAddress?: AddressRequest
  temporaryAddress?: AddressRequest
}

export interface AddressRequest {
  street: string
  ward: string
  district: string
  city: string
  country: string
}
