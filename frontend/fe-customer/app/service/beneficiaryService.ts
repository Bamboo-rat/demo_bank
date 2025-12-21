import { isAxiosError } from 'axios'
import axiosAccount from '~/config/axiosAccount'
import type { Beneficiary, CreateBeneficiaryRequest, UpdateBeneficiaryRequest } from '~/type/beneficiary'

const normalizeError = (error: unknown) => {
  if (isAxiosError(error)) {
    const message = (error.response?.data as { message?: string } | undefined)?.message
    return new Error(message ?? 'Có lỗi xảy ra, vui lòng thử lại')
  }
  return error instanceof Error ? error : new Error('Có lỗi xảy ra, vui lòng thử lại')
}

interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
}

export const beneficiaryService = {
  async getAllBeneficiaries(customerId: string): Promise<Beneficiary[]> {
    try {
      const { data } = await axiosAccount.get<ApiResponse<Beneficiary[]>>(
        `/customers/${customerId}/beneficiaries`
      )
      
      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể lấy danh sách người thụ hưởng')
      }
      
      return data.data
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async getBeneficiary(customerId: string, beneficiaryId: string): Promise<Beneficiary> {
    try {
      const { data } = await axiosAccount.get<ApiResponse<Beneficiary>>(
        `/customers/${customerId}/beneficiaries/${beneficiaryId}`
      )
      
      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không tìm thấy người thụ hưởng')
      }
      
      return data.data
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async createBeneficiary(customerId: string, request: CreateBeneficiaryRequest): Promise<Beneficiary> {
    try {
      const { data } = await axiosAccount.post<ApiResponse<Beneficiary>>(
        `/customers/${customerId}/beneficiaries`,
        request
      )
      
      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể tạo người thụ hưởng')
      }
      
      return data.data
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async updateBeneficiary(
    customerId: string, 
    beneficiaryId: string, 
    request: UpdateBeneficiaryRequest
  ): Promise<Beneficiary> {
    try {
      const { data } = await axiosAccount.put<ApiResponse<Beneficiary>>(
        `/customers/${customerId}/beneficiaries/${beneficiaryId}`,
        request
      )
      
      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể cập nhật người thụ hưởng')
      }
      
      return data.data
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async deleteBeneficiary(customerId: string, beneficiaryId: string): Promise<void> {
    try {
      const { data } = await axiosAccount.delete<ApiResponse<void>>(
        `/customers/${customerId}/beneficiaries/${beneficiaryId}`
      )
      
      if (!data?.success) {
        throw new Error(data?.message ?? 'Không thể xóa người thụ hưởng')
      }
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async searchBeneficiaries(customerId: string, searchTerm: string): Promise<Beneficiary[]> {
    try {
      const { data } = await axiosAccount.get<ApiResponse<Beneficiary[]>>(
        `/customers/${customerId}/beneficiaries/search?q=${encodeURIComponent(searchTerm)}`
      )
      
      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể tìm kiếm người thụ hưởng')
      }
      
      return data.data
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async getMostUsedBeneficiaries(customerId: string, limit: number = 5): Promise<Beneficiary[]> {
    try {
      const { data } = await axiosAccount.get<ApiResponse<Beneficiary[]>>(
        `/customers/${customerId}/beneficiaries/most-used?limit=${limit}`
      )
      
      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể lấy danh sách thường dùng')
      }
      
      return data.data
    } catch (error) {
      throw normalizeError(error)
    }
  }
}
