import { isAxiosError } from 'axios'
import axiosCustomer from '~/config/axiosCustomer'

interface LoginRequest {
  username: string
  password: string
}

interface LoginResponse {
  access_token: string
  refresh_token: string
  expires_in: number
  token_type: string
}

interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
}

const API_BASE = '/auth'

const normalizeError = (error: unknown) => {
  if (isAxiosError(error)) {
    const message = (error.response?.data as { message?: string } | undefined)?.message
    return new Error(message ?? 'Có lỗi xảy ra, vui lòng thử lại')
  }
  return error instanceof Error ? error : new Error('Có lỗi xảy ra, vui lòng thử lại')
}

export const authService = {
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    try {
      const { data } = await axiosCustomer.post<ApiResponse<LoginResponse>>(
        `${API_BASE}/login`,
        credentials
      )
      
      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Đăng nhập thất bại')
      }
      
      // Store tokens
      localStorage.setItem('access_token', data.data.access_token)
      localStorage.setItem('refresh_token', data.data.refresh_token)
      
      return data.data
    } catch (error) {
      throw normalizeError(error)
    }
  },

  logout: async (): Promise<void> => {
    try {
      await axiosCustomer.post(`${API_BASE}/logout`)
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      localStorage.removeItem('access_token')
      localStorage.removeItem('refresh_token')
    }
  },

  refreshToken: async (): Promise<LoginResponse> => {
    try {
      const refreshToken = localStorage.getItem('refresh_token')
      if (!refreshToken) {
        throw new Error('No refresh token')
      }

      const { data } = await axiosCustomer.post<ApiResponse<LoginResponse>>(
        `${API_BASE}/refresh`,
        { refresh_token: refreshToken }
      )
      
      if (!data?.success || !data?.data) {
        throw new Error('Token refresh failed')
      }
      
      localStorage.setItem('access_token', data.data.access_token)
      localStorage.setItem('refresh_token', data.data.refresh_token)
      
      return data.data
    } catch (error) {
      localStorage.removeItem('access_token')
      localStorage.removeItem('refresh_token')
      throw normalizeError(error)
    }
  },

  isAuthenticated: (): boolean => {
    return !!localStorage.getItem('access_token')
  }
}
