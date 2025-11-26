import { isAxiosError } from 'axios'
import axiosCustomer from '~/config/axiosCustomer'
import type {
  ApiResponse,
  RegistrationStartResponse,
  RegistrationSessionResponse,
  RegistrationProfilePayload,
  RegistrationIdentityPayload,
  RegistrationCompletePayload,
  CustomerResponse
} from '../../type/types'

const API_BASE = '/registration'

const normalizeError = (error: unknown) => {
  if (isAxiosError(error)) {
    const message = (error.response?.data as { message?: string } | undefined)?.message
    return new Error(message ?? 'Có lỗi xảy ra, vui lòng thử lại')
  }
  return error instanceof Error ? error : new Error('Có lỗi xảy ra, vui lòng thử lại')
}

const postRegistration = async <T,>(path: string, payload: unknown): Promise<ApiResponse<T>> => {
  try {
    const { data } = await axiosCustomer.post<ApiResponse<T>>(`${API_BASE}${path}`, payload)
    if (!data?.success) {
      throw new Error(data?.message ?? 'Có lỗi xảy ra, vui lòng thử lại')
    }
    return data
  } catch (error) {
    throw normalizeError(error)
  }
}

export const registrationService = {
  startRegistration: (phoneNumber: string) =>
    postRegistration<RegistrationStartResponse>('/start', { phoneNumber }),

  verifyOtp: (phoneNumber: string, otp: string) =>
    postRegistration<RegistrationSessionResponse>('/verify', { phoneNumber, otp }),

  saveProfile: (payload: RegistrationProfilePayload) =>
    postRegistration<RegistrationSessionResponse>('/profile', payload),

  saveIdentity: (payload: RegistrationIdentityPayload) =>
    postRegistration<RegistrationSessionResponse>('/identity', payload),

  complete: (payload: RegistrationCompletePayload) =>
    postRegistration<CustomerResponse>('/complete', payload)
}

export type {
  ApiResponse,
  RegistrationStartResponse,
  RegistrationSessionResponse,
  RegistrationProfilePayload,
  RegistrationIdentityPayload,
  RegistrationCompletePayload,
  CustomerResponse
} from '../../type/types'
