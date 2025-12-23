import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { loanService } from '~/service/loanService'
import type { LoanApplication, LoanAccount } from '~/type/loan'
import Layout from '~/component/layout/Layout'
import { useAuth } from '~/context/AuthContext'

export default function MyLoans() {
  const { customerProfile } = useAuth()
  const [activeTab, setActiveTab] = useState<'accounts' | 'applications'>('accounts')
  const [loanAccounts, setLoanAccounts] = useState<LoanAccount[]>([])
  const [loanApplications, setLoanApplications] = useState<LoanApplication[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    loadData()
  }, [activeTab])

  const loadData = async () => {
    try {
      setLoading(true)
      setError(null)

      if (!customerProfile?.customerId) {
        setError('Không tìm thấy thông tin khách hàng')
        return
      }

      if (activeTab === 'accounts') {
        const data = await loanService.getMyLoans()
        setLoanAccounts(data)
      } else {
        const data = await loanService.getMyLoanApplications()
        setLoanApplications(data)
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Có lỗi xảy ra')
    } finally {
      setLoading(false)
    }
  }

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount)
  }

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('vi-VN')
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE':
      case 'APPROVED':
        return 'bg-green-100 text-green-800'
      case 'OVERDUE':
      case 'REJECTED':
        return 'bg-red-100 text-red-800'
      case 'SETTLED':
      case 'CLOSED':
        return 'bg-gray-100 text-gray-800'
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800'
      default:
        return 'bg-blue-100 text-blue-800'
    }
  }

  return (
    <Layout>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-6 flex justify-between items-center">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Khoản vay của tôi</h1>
            <p className="text-gray-600 mt-1">Quản lý các khoản vay và hồ sơ vay</p>
          </div>
          <Link
            to="/loan/apply"
            className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2.5 rounded-lg font-medium transition-colors"
          >
            Đăng ký vay mới
          </Link>
        </div>

        {/* Tabs */}
        <div className="mb-6 border-b border-gray-200">
          <div className="flex space-x-8">
            <button
              onClick={() => setActiveTab('accounts')}
              className={`pb-4 px-1 border-b-2 font-medium text-sm ${
                activeTab === 'accounts'
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              Khoản vay đang có
            </button>
            <button
              onClick={() => setActiveTab('applications')}
              className={`pb-4 px-1 border-b-2 font-medium text-sm ${
                activeTab === 'applications'
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              Hồ sơ vay
            </button>
          </div>
        </div>

        {loading && (
          <div className="text-center py-12">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            <p className="text-gray-600 mt-4">Đang tải...</p>
          </div>
        )}

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
            {error}
          </div>
        )}

        {/* Loan Accounts Tab */}
        {!loading && !error && activeTab === 'accounts' && (
          <>
            {loanAccounts.length === 0 ? (
              <div className="text-center py-12 bg-gray-50 rounded-lg">
                <span className="material-icons-round text-gray-400 text-6xl">
                  account_balance
                </span>
                <p className="text-gray-600 mt-4">Bạn chưa có khoản vay nào</p>
                <Link
                  to="/loan/apply"
                  className="inline-block mt-4 text-blue-600 hover:text-blue-700 font-medium"
                >
                  Đăng ký vay ngay →
                </Link>
              </div>
            ) : (
              <div className="space-y-4">
                {loanAccounts.map((loan) => (
                  <div
                    key={loan.loanNumber}
                    className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow"
                  >
                    <div className="flex justify-between items-start mb-4">
                      <div>
                        <p className="text-sm text-gray-600">Số hợp đồng</p>
                        <p className="font-mono font-semibold text-gray-900">
                          {loan.loanNumber}
                        </p>
                      </div>
                      <span
                        className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(
                          loan.status
                        )}`}
                      >
                        {loan.statusLabel}
                      </span>
                    </div>

                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
                      <div>
                        <p className="text-sm text-gray-600">Mục đích</p>
                        <p className="font-medium">{loan.purposeLabel}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600">Số tiền vay</p>
                        <p className="font-bold text-blue-600">{formatCurrency(loan.approvedAmount)}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600">Dư nợ</p>
                        <p className="font-bold text-red-600">
                          {formatCurrency(loan.outstandingPrincipal)}
                        </p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600">Lãi suất</p>
                        <p className="font-semibold text-green-600">{loan.interestRate}%/năm</p>
                      </div>
                    </div>

                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
                      <div>
                        <p className="text-sm text-gray-600">Kỳ hạn</p>
                        <p className="font-medium">{loan.tenor} tháng</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600">Ngày giải ngân</p>
                        <p className="font-medium">{loan.disbursementDate ? formatDate(loan.disbursementDate) : 'Chưa giải ngân'}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600">Ngày đáo hạn</p>
                        <p className="font-medium">{formatDate(loan.maturityDate)}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600">Phương thức</p>
                        <p className="font-medium">{loan.repaymentMethodLabel}</p>
                      </div>
                    </div>

                    <div className="flex gap-3">
                      <Link
                        to={`/loan/pay?loan=${loan.loanNumber}`}
                        className="flex-1 text-center bg-blue-600 hover:bg-blue-700 text-white py-2 rounded-lg font-medium transition-colors"
                      >
                        Thanh toán
                      </Link>
                      <button className="flex-1 text-center border border-gray-300 text-gray-700 py-2 rounded-lg font-medium hover:bg-gray-50">
                        Xem chi tiết
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </>
        )}

        {/* Loan Applications Tab */}
        {!loading && !error && activeTab === 'applications' && (
          <>
            {loanApplications.length === 0 ? (
              <div className="text-center py-12 bg-gray-50 rounded-lg">
                <span className="material-icons-round text-gray-400 text-6xl">description</span>
                <p className="text-gray-600 mt-4">Bạn chưa có hồ sơ vay nào</p>
                <Link
                  to="/loan/apply"
                  className="inline-block mt-4 text-blue-600 hover:text-blue-700 font-medium"
                >
                  Đăng ký vay ngay →
                </Link>
              </div>
            ) : (
              <div className="space-y-4">
                {loanApplications.map((application) => (
                  <div
                    key={application.applicationId}
                    className="bg-white rounded-lg shadow-sm border border-gray-200 p-6"
                  >
                    <div className="flex justify-between items-start mb-4">
                      <div>
                        <p className="text-sm text-gray-600">Số hồ sơ</p>
                        <p className="font-mono font-semibold text-gray-900">
                          {application.applicationId}
                        </p>
                      </div>
                      <span
                        className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(
                          application.status
                        )}`}
                      >
                        {application.statusLabel}
                      </span>
                    </div>

                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
                      <div>
                        <p className="text-sm text-gray-600">Mục đích</p>
                        <p className="font-medium">{application.purposeLabel}</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600">Số tiền vay</p>
                        <p className="font-bold text-blue-600">
                          {formatCurrency(application.requestedAmount)}
                        </p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600">Kỳ hạn</p>
                        <p className="font-medium">{application.tenor} tháng</p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600">Ngày đăng ký</p>
                        <p className="font-medium">{formatDate(application.createdAt)}</p>
                      </div>
                    </div>

                    {application.monthlyIncome && (
                      <div className="mt-3 p-3 bg-gray-50 rounded-lg">
                        <p className="text-sm text-gray-600 mb-1">Thu nhập hàng tháng:</p>
                        <p className="text-sm text-gray-900">{formatCurrency(application.monthlyIncome)}</p>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </>
        )}
      </div>
    </Layout>
  )
}
