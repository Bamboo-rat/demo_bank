import { isAxiosError } from 'axios'
import axiosSavings from '~/config/axiosSavings'
import type {
  SavingsAccount,
  SavingsProduct,
  CreateSavingsRequest,
  CloseSavingsRequest
} from '~/type/savings'

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

const SAVINGS_TYPE_LABELS: Record<string, string> = {
  FIXED_TERM: 'Có kỳ hạn',
  FLEXIBLE: 'Linh hoạt',
  ACCUMULATION: 'Tích lũy'
}

const STATUS_LABELS: Record<string, string> = {
  ACTIVE: 'Đang hoạt động',
  MATURED: 'Đã đáo hạn',
  CLOSED: 'Đã tất toán',
  PENDING: 'Chờ xử lý'
}

const mapSavingsAccount = (account: any): SavingsAccount => ({
  accountNumber: account.accountNumber,
  savingsType: account.savingsType,
  savingsTypeName: SAVINGS_TYPE_LABELS[account.savingsType] ?? account.savingsType,
  depositAmount: Number(account.depositAmount ?? 0),
  interestRate: Number(account.interestRate ?? 0),
  termMonths: account.termMonths ?? 0,
  maturityDate: account.maturityDate,
  status: account.status,
  statusLabel: STATUS_LABELS[account.status] ?? account.status,
  estimatedInterest: Number(account.estimatedInterest ?? 0),
  maturityAmount: Number(account.maturityAmount ?? 0),
  createdAt: account.createdAt,
  autoRenew: account.autoRenew ?? false
})

export const savingsService = {
  async getMySavingsAccounts(): Promise<SavingsAccount[]> {
    try {
      const { data } = await axiosSavings.get<ApiResponse<any[]>>(
        '/savings/customer'
      )

      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể lấy danh sách sổ tiết kiệm')
      }

      return data.data.map(mapSavingsAccount)
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async getSavingsProducts(): Promise<SavingsProduct[]> {
    try {
      const { data } = await axiosSavings.get<ApiResponse<any[]>>(
        '/savings/products'
      )

      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể lấy danh sách sản phẩm')
      }

      return data.data.map((product: any) => ({
        productCode: product.productCode,
        productName: product.productName,
        description: product.description ?? '',
        minAmount: Number(product.minAmount ?? 0),
        maxAmount: Number(product.maxAmount ?? 0),
        termMonths: product.termMonths ?? 0,
        interestRate: Number(product.interestRate ?? 0),
        earlyWithdrawalPenalty: Number(product.earlyWithdrawalPenalty ?? 0)
      }))
    } catch (error) {
      throw normalizeError(error)
    }
  },


  async getSavingsAccountDetail(savingsAccountId: string): Promise<SavingsAccount> {
    try {
      const { data } = await axiosSavings.get<ApiResponse<any>>(
        `/savings/${savingsAccountId}`
      )

      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể lấy thông tin sổ tiết kiệm')
      }

      return mapSavingsAccount(data.data)
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async createSavingsAccount(request: CreateSavingsRequest): Promise<SavingsAccount> {
    try {
      const { data } = await axiosSavings.post<ApiResponse<any>>(
        '/savings/open',
        request
      )

      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể mở sổ tiết kiệm')
      }

      return mapSavingsAccount(data.data)
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async closeSavingsAccount(savingsAccountId: string): Promise<any> {
    try {
      const { data } = await axiosSavings.post<ApiResponse<any>>(
        `/savings/${savingsAccountId}/premature-withdraw`
      )

      if (!data?.success) {
        throw new Error(data?.message ?? 'Không thể tất toán sổ tiết kiệm')
      }

      return data.data
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async calculateInterest(
    principalAmount: number,
    tenor: string,
    interestPaymentMethod = 'END_OF_TERM'
  ): Promise<{ 
    interestRate: number
    projectedInterest: number
    maturityAmount: number 
    maturityDate: string
  }> {
    try {
      const { data } = await axiosSavings.post<ApiResponse<any>>(
        '/savings/calculate-preview',
        { principalAmount, tenor, interestPaymentMethod }
      )

      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể tính lãi')
      }

      return {
        interestRate: Number(data.data.interestRate ?? 0),
        projectedInterest: Number(
          data.data.projectedInterest ?? data.data.estimatedInterest ?? 0
        ),
        maturityAmount: Number(data.data.maturityAmount ?? data.data.totalAmount ?? 0),
        maturityDate: data.data.maturityDate
      }
    } catch (error) {
      throw normalizeError(error)
    }
  }
}
