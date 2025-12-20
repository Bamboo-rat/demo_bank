import { isAxiosError } from 'axios'
import axiosTransaction from '~/config/axiosTransactione'

interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
}

export interface DigitalOtpStatus {
  enrolled: boolean
  locked: boolean
  enrolledAtTimestamp?: number | null
  lockedUntilTimestamp?: number | null
}

export interface DigitalOtpEnrollmentPayload {
  customerId: string
  digitalOtpSecret: string
  digitalPinHash: string
  salt: string
}

const API_BASE = '/digital-otp'

const normalizeError = (error: unknown) => {
  if (isAxiosError(error)) {
    const message = (error.response?.data as { message?: string } | undefined)?.message
    return new Error(message ?? 'Có lỗi xảy ra, vui lòng thử lại')
  }

  return error instanceof Error ? error : new Error('Có lỗi xảy ra, vui lòng thử lại')
}

export const digitalOtpService = {
  async getStatus(customerId: string): Promise<DigitalOtpStatus> {
    try {
      const { data } = await axiosTransaction.get<ApiResponse<DigitalOtpStatus>>(
        `${API_BASE}/status/${customerId}`
      )

      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể kiểm tra trạng thái Digital OTP')
      }

      return data.data
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async enroll(payload: DigitalOtpEnrollmentPayload): Promise<boolean> {
    try {
      const { data } = await axiosTransaction.post<ApiResponse<boolean>>(
        `${API_BASE}/enroll`,
        payload
      )

      if (!data?.success) {
        throw new Error(data?.message ?? 'Không thể đăng ký Digital OTP')
      }

      return Boolean(data.data)
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async update(payload: DigitalOtpEnrollmentPayload): Promise<boolean> {
    try {
      const { data } = await axiosTransaction.put<ApiResponse<boolean>>(
        `${API_BASE}/update`,
        payload
      )

      if (!data?.success) {
        throw new Error(data?.message ?? 'Không thể cập nhật Digital OTP')
      }

      return Boolean(data.data)
    } catch (error) {
      throw normalizeError(error)
    }
  }
}
