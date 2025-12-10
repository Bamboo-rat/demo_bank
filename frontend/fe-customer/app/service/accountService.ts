import { isAxiosError } from 'axios'
import axiosAccount from '~/config/axiosAccount'

const ACCOUNT_TYPE_LABELS: Record<string, string> = {
  CHECKING: 'Tài khoản thanh toán',
  SAVINGS: 'Tài khoản tiết kiệm',
  CREDIT: 'Tài khoản tín dụng'
}

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

interface AccountResponseDto {
  accountId: string
  accountNumber: string
  accountType: string
  status: string
  currency: string
}

interface AccountListPayload {
  customerId: string
  accounts: AccountResponseDto[]
  totalCount: number
  timestamp: number
}

interface AccountDetailPayload {
  accountNumber: string
  accountType: string
  status: string
  currency: string
  balance?: string | number
  holdAmount?: string | number
}

export interface AccountSummary {
  accountId: string
  accountNumber: string
  accountType: string
  accountTypeLabel: string
  status: string
  currency: string
}

export interface AccountDetail {
  accountNumber: string
  accountType: string
  status: string
  currency: string
  balance: number
  holdAmount?: number
}

const mapAccountSummary = (account: AccountResponseDto): AccountSummary => ({
  accountId: account.accountId,
  accountNumber: account.accountNumber,
  accountType: account.accountType,
  accountTypeLabel: ACCOUNT_TYPE_LABELS[account.accountType] ?? account.accountType,
  status: account.status,
  currency: account.currency
})

const parseNumber = (value: string | number | undefined): number => {
  if (typeof value === 'number') {
    return value
  }

  if (typeof value === 'string') {
    const parsed = Number(value)
    return Number.isNaN(parsed) ? 0 : parsed
  }

  return 0
}

export const accountService = {
  async getMyAccounts(): Promise<AccountSummary[]> {
    try {
      const { data } = await axiosAccount.get<ApiResponse<AccountListPayload>>(
        '/accounts/my-accounts'
      )

      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể lấy danh sách tài khoản')
      }

      const accounts = data.data.accounts ?? []
      return accounts.map(mapAccountSummary)
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async getAccountDetail(accountNumber: string): Promise<AccountDetail> {
    try {
      const { data } = await axiosAccount.get<ApiResponse<AccountDetailPayload>>(
        `/accounts/${accountNumber}`
      )

      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể lấy thông tin tài khoản')
      }

      const payload = data.data
      return {
        accountNumber: payload.accountNumber,
        accountType: payload.accountType,
        status: payload.status,
        currency: payload.currency,
        balance: parseNumber(payload.balance),
        holdAmount: payload.holdAmount !== undefined ? parseNumber(payload.holdAmount) : undefined
      }
    } catch (error) {
      throw normalizeError(error)
    }
  }
}
