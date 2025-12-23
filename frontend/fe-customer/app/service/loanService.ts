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
  CONSUMER_LOAN: 'Tiêu dùng',
  HOME_LOAN: 'Mua nhà',
  AUTO_LOAN: 'Mua xe',
  PERSONAL_LOAN: 'Tín chấp',
  BUSINESS_LOAN: 'Kinh doanh',
  EDUCATION_LOAN: 'Học tập'
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
      const { data } = await axiosLoan.get<any[]>(
        '/loan/applications/customer/me'
      )

      return data.map(mapLoanApplication)
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async getMyLoans(): Promise<LoanAccount[]> {
    try {
      const { data } = await axiosLoan.get<any[]>(
        '/loan/accounts/customer/me'
      )

      return data.map(mapLoanAccount)
    } catch (error) {
      throw normalizeError(error)
    }
  },

  // Backend: GET /api/loan/accounts/{loanAccountId}
  async getLoanDetail(loanAccountId: string): Promise<LoanAccount> {
    try {
      const { data } = await axiosLoan.get<any>(
        `/loan/accounts/${loanAccountId}`
      )

      return mapLoanAccount(data)
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async getRepaymentSchedule(loanAccountId: string): Promise<RepaymentSchedule[]> {
    try {
      const { data } = await axiosLoan.get<any[]>(
        `/loan/schedule/${loanAccountId}`
      )

      return data.map(mapRepaymentSchedule)
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async getPaymentHistory(loanAccountId: string): Promise<LoanPaymentHistory[]> {
    try {
      const { data } = await axiosLoan.get<any[]>(
        `/loan/repayment/history/${loanAccountId}`
      )

      return data.map((payment: any) => ({
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
      const { data } = await axiosLoan.post<any>(
        '/loan/applications',
        request
      )

      return mapLoanApplication(data)
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async repayInstallment(request: RepayInstallmentRequest): Promise<any> {
    try {
      const { data } = await axiosLoan.post<any>(
        '/loan/repayment/pay',
        request
      )

      return data
    } catch (error) {
      throw normalizeError(error)
    }
  },

  async getCurrentInstallment(loanAccountId: string): Promise<RepaymentSchedule | null> {
    try {
      const schedules = await this.getRepaymentSchedule(loanAccountId)
      // Tìm kỳ đầu tiên chưa thanh toán
      const current = schedules.find(s => s.status === 'PENDING' || s.status === 'OVERDUE')
      return current || null
    } catch (error) {
      if (isAxiosError(error) && error.response?.status === 404) {
        return null
      }
      throw normalizeError(error)
    }
  }
}
