export interface Address {
  street: string
  ward: string
  district: string
  city: string
  country: string
}

export interface CustomerProfile {
  customerId: string
  fullName: string
  phoneNumber: string
  email: string
  dateOfBirth?: string
  gender?: string
  nationality?: string
  nationalId?: string
  cifNumber?: string
  status?: string
  kycStatus?: string
  riskLevel?: string
  createdAt?: string
  permanentAddress?: Address
  temporaryAddress?: Address
}

export interface AuthContextType {
  accessToken: string | null
  refreshToken: string | null
  customerProfile: CustomerProfile | null
  customerId: string | null
  loading: boolean
  isAuthenticated: boolean
  login: (accessToken: string, refreshToken: string) => void
  logout: () => void
  refreshProfile: () => Promise<void>
}
