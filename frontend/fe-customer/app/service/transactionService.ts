import { isAxiosError } from 'axios'
import axiosTransaction, { axiosTransactionPublic } from '~/config/axiosTransactione'

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

export interface BankResponse {
  id: number
  code: string
  name: string
  shortName: string
  bin: string
  logo?: string
  swiftCode?: string
  isActive: boolean
}

export interface TransferRequest {
  sourceAccountNumber: string
  destinationAccountNumber: string
  destinationBankCode?: string
  amount: number
  description: string
  feePaymentMethod: 'SOURCE' | 'DESTINATION'
  transferType: 'INTERNAL' | 'INTERBANK'
}

export interface TransferConfirm {
  transactionId: string
  digitalOtpToken: string
  pinHashCurrent: string
}

export interface TransferResponse {
  transactionId: string
  sourceAccountNumber: string
  destinationAccountNumber: string
  destinationBankCode?: string
  destinationBankName?: string
  amount: number
  fee: number
  totalAmount: number
  description: string
  status: string
  transferType: string
  createdAt: string
  completedAt?: string
  digitalOtpRequired?: boolean
}

export interface AccountInfo {
  accountNumber: string
  accountHolderName: string
  bankCode?: string
  bankName?: string
}

export const transactionService = {
  async getAllBanks(): Promise<BankResponse[]> {
    try {
      const { data } = await axiosTransactionPublic.get<ApiResponse<BankResponse[]>>('/banks')
      
      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể lấy danh sách ngân hàng')
      }
      
      return data.data
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async searchBanks(query: string): Promise<BankResponse[]> {
    try {
      const { data } = await axiosTransactionPublic.get<ApiResponse<BankResponse[]>>(
        `/banks/search?q=${encodeURIComponent(query)}`
      )
      
      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể tìm kiếm ngân hàng')
      }
      
      return data.data
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async getAccountInfo(accountNumber: string, bankCode?: string): Promise<AccountInfo> {
    try {
      const params = bankCode ? `?bankCode=${encodeURIComponent(bankCode)}` : ''
      const url = `/accounts/info/${accountNumber}${params}`
      
      const { data } = await axiosTransactionPublic.get<ApiResponse<AccountInfo>>(url)
      
      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không tìm thấy thông tin tài khoản')
      }
      
      return data.data
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async initiateTransfer(request: TransferRequest): Promise<TransferResponse> {
    try {
      const { data } = await axiosTransaction.post<ApiResponse<TransferResponse>>(
        '/transactions/transfer/request',
        request
      )
      
      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể khởi tạo giao dịch')
      }
      
      return data.data
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async confirmTransfer(confirm: TransferConfirm): Promise<TransferResponse> {
    try {
      const { data } = await axiosTransaction.post<ApiResponse<TransferResponse>>(
        '/transactions/transfer/confirm',
        confirm
      )
      
      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể xác nhận giao dịch')
      }
      
      return data.data
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async getTransactionHistory(
    accountNumber: string, 
    page = 0, 
    size = 10
  ): Promise<TransferResponse[]> {
    try {
      const { data } = await axiosTransaction.get<ApiResponse<TransferResponse[]>>(
        `/transactions/account/${accountNumber}?page=${page}&size=${size}`
      )
      
      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể lấy lịch sử giao dịch')
      }
      
      return data.data
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async getTransactionById(transactionId: string): Promise<TransferResponse> {
    try {
      const { data } = await axiosTransaction.get<ApiResponse<TransferResponse>>(
        `/transactions/${transactionId}`
      )
      
      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không tìm thấy giao dịch')
      }
      
      return data.data
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async cancelTransaction(transactionId: string): Promise<TransferResponse> {
    try {
      const { data } = await axiosTransaction.put<ApiResponse<TransferResponse>>(
        `/transactions/${transactionId}/cancel`
      )
      
      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể hủy giao dịch')
      }
      
      return data.data
    } catch (error) {
      throw normalizeError(error)
    }
  }
}
