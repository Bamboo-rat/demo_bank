import { isAxiosError } from 'axios'
import axiosCustomer from '~/config/axiosCustomer'

interface Address {
  street: string
  ward: string
  district: string
  city: string
  country: string
}

interface CustomerProfile {
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

interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
}

const API_BASE = '/customers'

const normalizeError = (error: unknown) => {
  if (isAxiosError(error)) {
    const message = (error.response?.data as { message?: string } | undefined)?.message
    return new Error(message ?? 'Có lỗi xảy ra, vui lòng thử lại')
  }
  return error instanceof Error ? error : new Error('Có lỗi xảy ra, vui lòng thử lại')
}

export const customerService = {
  // Get current user profile
  getMyProfile: async (): Promise<CustomerProfile> => {
    try {
      const { data } = await axiosCustomer.get<ApiResponse<CustomerProfile>>(
        `${API_BASE}/me`
      )
      
      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể lấy thông tin khách hàng')
      }
      
      return data.data
    } catch (error) {
      throw normalizeError(error)
    }
  },

  // Get customer by ID
  getCustomerById: async (customerId: string): Promise<CustomerProfile> => {
    try {
      const { data } = await axiosCustomer.get<ApiResponse<CustomerProfile>>(
        `${API_BASE}/${customerId}`
      )
      
      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể lấy thông tin khách hàng')
      }
      
      return data.data
    } catch (error) {
      throw normalizeError(error)
    }
  },

  // Update customer profile
  updateProfile: async (updateData: Partial<CustomerProfile>): Promise<CustomerProfile> => {
    try {
      const { data } = await axiosCustomer.put<ApiResponse<CustomerProfile>>(
        `${API_BASE}/me`,
        updateData
      )
      
      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể cập nhật thông tin')
      }
      
      return data.data
    } catch (error) {
      throw normalizeError(error)
    }
  }
}

export type { CustomerProfile }
