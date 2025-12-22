import { isAxiosError } from 'axios'
import axiosLoan from '~/config/axiosLoan'
import type {
  LoanApplication,
  LoanAccount,
  RepaymentSchedule,
  LoanPaymentHistory,
  CreateLoanApplicationRequest,
  RepayInstallmentRequest
} from '~/type/loan'

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

const PURPOSE_LABELS: Record<string, string> = {
  PERSONAL: 'Tiêu dùng cá nhân',
  HOME_PURCHASE: 'Mua nhà',
  CAR_PURCHASE: 'Mua xe',
  EDUCATION: 'Học tập',
  BUSINESS: 'Kinh doanh',
  OTHER: 'Khác'
}

const REPAYMENT_METHOD_LABELS: Record<string, string> = {
  EQUAL_PRINCIPAL: 'Gốc đều',
  EQUAL_INSTALLMENT: 'Gốc + lãi đều'
}

const STATUS_LABELS: Record<string, string> = {
  PENDING_APPROVAL: 'Chờ duyệt',
  APPROVED: 'Đã duyệt',
  ACTIVE: 'Đang hoạt động',
  OVERDUE: 'Quá hạn',
  CLOSED: 'Đã đóng',
  REJECTED: 'Từ chối',
  CANCELLED: 'Đã hủy'
}

const INSTALLMENT_STATUS_LABELS: Record<string, string> = {
  PENDING: 'Chưa thanh toán',
  PAID: 'Đã thanh toán',
  OVERDUE: 'Quá hạn',
  CANCELLED: 'Đã hủy'
}

const mapLoanApplication = (app: any): LoanApplication => ({
  applicationId: app.applicationId,
  requestedAmount: Number(app.requestedAmount ?? 0),
  tenor: app.tenor ?? 0,
  purpose: app.purpose,
  purposeLabel: PURPOSE_LABELS[app.purpose] ?? app.purpose,
  repaymentMethod: app.repaymentMethod,
  repaymentMethodLabel: REPAYMENT_METHOD_LABELS[app.repaymentMethod] ?? app.repaymentMethod,
  status: app.status,
  statusLabel: STATUS_LABELS[app.status] ?? app.status,
  monthlyIncome: app.monthlyIncome ? Number(app.monthlyIncome) : undefined,
  createdAt: app.createdAt,
  reviewedAt: app.reviewedAt
})

const mapLoanAccount = (loan: any): LoanAccount => ({
  loanNumber: loan.loanNumber ?? loan.loanId,
  customerId: loan.customerId,
  approvedAmount: Number(loan.approvedAmount ?? 0),
  outstandingPrincipal: Number(loan.outstandingPrincipal ?? 0),
  interestRate: Number(loan.interestRateSnapshot ?? loan.interestRate ?? 0),
  tenor: loan.tenor ?? 0,
  purpose: loan.purpose,
  purposeLabel: PURPOSE_LABELS[loan.purpose] ?? loan.purpose,
  repaymentMethod: loan.repaymentMethod,
  repaymentMethodLabel: REPAYMENT_METHOD_LABELS[loan.repaymentMethod] ?? loan.repaymentMethod,
  startDate: loan.startDate,
  maturityDate: loan.maturityDate,
  status: loan.status,
  statusLabel: STATUS_LABELS[loan.status] ?? loan.status,
  disbursementDate: loan.disbursementDate,
  installmentsPaid: loan.installmentsPaid,
  installmentsRemaining: loan.installmentsRemaining
})

const mapRepaymentSchedule = (schedule: any): RepaymentSchedule => ({
  scheduleId: schedule.scheduleId,
  installmentNo: schedule.installmentNo ?? 0,
  dueDate: schedule.dueDate,
  principalAmount: Number(schedule.principalAmount ?? 0),
  interestAmount: Number(schedule.interestAmount ?? 0),
  totalAmount: Number(schedule.totalAmount ?? 0),
  paidAmount: schedule.paidAmount ? Number(schedule.paidAmount) : undefined,
  penaltyAmount: schedule.penaltyAmount ? Number(schedule.penaltyAmount) : undefined,
  status: schedule.status,
  statusLabel: INSTALLMENT_STATUS_LABELS[schedule.status] ?? schedule.status,
  overdueDays: schedule.overdueDays
})

export const loanService = {
  async getMyLoanApplications(): Promise<LoanApplication[]> {
    try {
      const { data } = await axiosLoan.get<ApiResponse<any[]>>(
        '/loan-applications/my-applications'
      )

      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể lấy danh sách đơn vay')
      }

      return data.data.map(mapLoanApplication)
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async getMyLoans(): Promise<LoanAccount[]> {
    try {
      const { data } = await axiosLoan.get<ApiResponse<any[]>>(
        '/loan-accounts/my-loans'
      )

      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể lấy danh sách khoản vay')
      }

      return data.data.map(mapLoanAccount)
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async getLoanDetail(loanAccountId: string): Promise<LoanAccount> {
    try {
      const { data } = await axiosLoan.get<ApiResponse<any>>(
        `/loan-accounts/${loanAccountId}`
      )

      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể lấy thông tin khoản vay')
      }

      return mapLoanAccount(data.data)
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async getRepaymentSchedule(loanAccountId: string): Promise<RepaymentSchedule[]> {
    try {
      const { data } = await axiosLoan.get<ApiResponse<any[]>>(
        `/repayment-schedule/${loanAccountId}`
      )

      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể lấy lịch trả nợ')
      }

      return data.data.map(mapRepaymentSchedule)
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async getPaymentHistory(loanAccountId: string): Promise<LoanPaymentHistory[]> {
    try {
      const { data } = await axiosLoan.get<ApiResponse<any[]>>(
        `/repayment/history/${loanAccountId}`
      )

      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể lấy lịch sử thanh toán')
      }

      return data.data.map((payment: any) => ({
        paidAmount: Number(payment.paidAmount ?? 0),
        principalPaid: Number(payment.principalPaid ?? 0),
        interestPaid: Number(payment.interestPaid ?? 0),
        penaltyPaid: payment.penaltyPaid ? Number(payment.penaltyPaid) : undefined,
        paymentMethod: payment.paymentMethod ?? '',
        result: payment.result,
        paidDate: payment.paidDate
      }))
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async createLoanApplication(request: CreateLoanApplicationRequest): Promise<LoanApplication> {
    try {
      const { data } = await axiosLoan.post<ApiResponse<any>>(
        '/loan-applications/register',
        request
      )

      if (!data?.success || !data?.data) {
        throw new Error(data?.message ?? 'Không thể đăng ký vay')
      }

      return mapLoanApplication(data.data)
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async repayInstallment(request: RepayInstallmentRequest): Promise<void> {
    try {
      const { data } = await axiosLoan.post<ApiResponse<void>>(
        '/repayment/repay',
        request
      )

      if (!data?.success) {
        throw new Error(data?.message ?? 'Không thể thanh toán kỳ trả nợ')
      }
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async getCurrentInstallment(loanAccountId: string): Promise<RepaymentSchedule | null> {
    try {
      const { data } = await axiosLoan.get<ApiResponse<any>>(
        `/repayment-schedule/${loanAccountId}/current`
      )

      if (!data?.success || !data?.data) {
        return null
      }

      return mapRepaymentSchedule(data.data)
    } catch (error) {
      // Return null for not found, throw for other errors
      if (isAxiosError(error) && error.response?.status === 404) {
        return null
      }
      throw normalizeError(error)
    }
  }
}
