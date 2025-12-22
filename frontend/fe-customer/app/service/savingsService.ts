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
        '/savings/my-savings'
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

  async getSavingsAccountDetail(accountNumber: string): Promise<SavingsAccount> {
    try {
      const { data } = await axiosSavings.get<ApiResponse<any>>(
        `/savings/${accountNumber}`
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

  async closeSavingsAccount(request: CloseSavingsRequest): Promise<void> {
    try {
      const { data } = await axiosSavings.post<ApiResponse<void>>(
        '/savings/close',
        request
      )

      if (!data?.success) {
        throw new Error(data?.message ?? 'Không thể tất toán sổ tiết kiệm')
      }
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async calculateInterest(
    depositAmount: number,
    termMonths: number,
    interestRate: number
  ): Promise<{ estimatedInterest: number; maturityAmount: number }> {
    try {
      const { data } = await axiosSavings.post<ApiResponse<any>>(
        '/savings/calculate-interest',
        { depositAmount, termMonths, interestRate }
      )

      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể tính lãi')
      }

      return {
        estimatedInterest: Number(data.data.estimatedInterest ?? 0),
        maturityAmount: Number(data.data.maturityAmount ?? 0)
      }
    } catch (error) {
      throw normalizeError(error)
    }
  }
}
