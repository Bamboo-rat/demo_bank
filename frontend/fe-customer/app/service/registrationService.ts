import { isAxiosError } from 'axios'
import axiosCustomer from '~/config/axiosCustomer'
import type {
  ApiResponse,
  RegistrationStartResponse,
  RegistrationSessionResponse,
  RegistrationProfilePayload,
  RegistrationIdentityPayload,
  RegistrationCompletePayload,
  CustomerResponse,
  AddressRequest
} from '../type/types'

interface EkycVerificationRequest {
  password: string
  fullName: string
  dateOfBirth: string
  gender: string
  nationality: string
  nationalId: string
  issueDateNationalId: string
  placeOfIssueNationalId: string
  occupation?: string
  position?: string
  email: string
  phoneNumber: string
  permanentAddress: AddressRequest
  temporaryAddress?: AddressRequest
}

interface EkycResponse {
  verified: boolean
  message?: string
}

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
    postRegistration<CustomerResponse>('/complete', payload),

  verifyKyc: async (payload: EkycVerificationRequest): Promise<EkycResponse> => {
    try {
      const { data } = await axiosCustomer.post<ApiResponse<EkycResponse>>(
        '/customers/kyc/verify',
        payload
      )
      
      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Xác thực eKYC thất bại')
      }
      
      return data.data
    } catch (error) {
      throw normalizeError(error)
    }
  }
}

export type {
  ApiResponse,
  RegistrationStartResponse,
  RegistrationSessionResponse,
  RegistrationProfilePayload,
  RegistrationIdentityPayload,
  RegistrationCompletePayload,
  CustomerResponse
} from '../type/types'
