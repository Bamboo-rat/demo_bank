import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import { customerService } from '~/service/customerService'
import type { AuthContextType, CustomerProfile } from '~/type/auth'

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [accessToken, setAccessToken] = useState<string | null>(null)
  const [refreshToken, setRefreshToken] = useState<string | null>(null)
  const [customerProfile, setCustomerProfile] = useState<CustomerProfile | null>(null)
  const [loading, setLoading] = useState(true)

  // Initialize from localStorage on mount
  useEffect(() => {
    if (typeof window === 'undefined') return

    const storedAccessToken = localStorage.getItem('accessToken')
    const storedRefreshToken = localStorage.getItem('refreshToken')

    if (storedAccessToken) {
      setAccessToken(storedAccessToken)
      setRefreshToken(storedRefreshToken)
      loadCustomerProfile()
    } else {
      setLoading(false)
    }
  }, [])

  const loadCustomerProfile = async () => {
    try {
      const profile = await customerService.getMyProfile()
      setCustomerProfile(profile)
      // Sync customerId to localStorage for backward compatibility
      if (typeof window !== 'undefined') {
        localStorage.setItem('customerId', profile.customerId)
      }
    } catch (error) {
      console.error('Failed to load customer profile:', error)
      // Token might be invalid, clear auth state
      logout()
    } finally {
      setLoading(false)
    }
  }

  const login = (newAccessToken: string, newRefreshToken: string) => {
    setAccessToken(newAccessToken)
    setRefreshToken(newRefreshToken)
    
    if (typeof window !== 'undefined') {
      localStorage.setItem('accessToken', newAccessToken)
      localStorage.setItem('refreshToken', newRefreshToken)
    }
    
    loadCustomerProfile()
  }

  const logout = () => {
    setAccessToken(null)
    setRefreshToken(null)
    setCustomerProfile(null)
    
    if (typeof window !== 'undefined') {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('customerId')
    }
  }

  const refreshProfile = async () => {
    if (!accessToken) return
    await loadCustomerProfile()
  }

  const value: AuthContextType = {
    accessToken,
    refreshToken,
    customerProfile,
    customerId: customerProfile?.customerId || null,
    loading,
    isAuthenticated: !!accessToken && !!customerProfile,
    login,
    logout,
    refreshProfile
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
