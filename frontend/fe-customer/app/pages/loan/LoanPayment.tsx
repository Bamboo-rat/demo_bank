import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router'
import { loanService } from '~/service/loanService'
import { accountService } from '~/service/accountService'
import type { LoanAccount, RepaymentSchedule, LoanPaymentHistory } from '~/type/loan'
import type { AccountSummary } from '~/service/accountService'
import Layout from '~/component/layout/Layout'

export default function LoanPayment() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const loanAccountNumber = searchParams.get('loan')

  const [loading, setLoading] = useState(false)
  const [loanAccount, setLoanAccount] = useState<LoanAccount | null>(null)
  const [schedule, setSchedule] = useState<RepaymentSchedule[]>([])
  const [paymentHistory, setPaymentHistory] = useState<LoanPaymentHistory[]>([])
  const [sourceAccounts, setSourceAccounts] = useState<AccountSummary[]>([])
  const [activeTab, setActiveTab] = useState<'schedule' | 'history'>('schedule')
  const [selectedInstallment, setSelectedInstallment] = useState<RepaymentSchedule | null>(null)
  const [sourceAccountNumber, setSourceAccountNumber] = useState('')

  useEffect(() => {
    if (loanAccountNumber) {
      loadData()
    }
  }, [loanAccountNumber, activeTab])

  const loadData = async () => {
    if (!loanAccountNumber) return

    try {
      setLoading(true)
      const [loanData, accountsData] = await Promise.all([
        loanService.getLoanDetail(loanAccountNumber),
        accountService.getMyAccounts()
      ])

      setLoanAccount(loanData)
      setSourceAccounts(
        accountsData.filter((a) => a.accountType === 'CHECKING' && a.status === 'ACTIVE')
      )

      if (activeTab === 'schedule') {
        const scheduleData = await loanService.getRepaymentSchedule(loanAccountNumber)
        setSchedule(scheduleData)
        
        // Auto-select current due installment
        const currentInstallment = await loanService.getCurrentInstallment(loanAccountNumber)
        if (currentInstallment) {
          setSelectedInstallment(currentInstallment)
        }
      } else {
        const historyData = await loanService.getPaymentHistory(loanAccountNumber)
        setPaymentHistory(historyData)
      }
    } catch (error) {
      console.error('Failed to load data:', error)
    } finally {
      setLoading(false)
    }
  }

  const handlePayment = async () => {
    if (!loanAccountNumber || !selectedInstallment || !sourceAccountNumber) return

    try {
      setLoading(true)
      await loanService.repayInstallment({
        loanAccountId: loanAccountNumber,
        scheduleId: selectedInstallment.scheduleId,
        paymentMethod: 'ACCOUNT_TRANSFER'
      })

      alert('Thanh toán thành công!')
      loadData() // Reload data
      setSelectedInstallment(null)
    } catch (error) {
      alert(error instanceof Error ? error.message : 'Có lỗi xảy ra')
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

  const getInstallmentStatusColor = (status: string) => {
    switch (status) {
      case 'PAID':
        return 'bg-green-100 text-green-800'
      case 'OVERDUE':
        return 'bg-red-100 text-red-800'
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  if (loading && !loanAccount) {
    return (
      <Layout>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="text-center py-12">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            <p className="text-gray-600 mt-4">Đang tải...</p>
          </div>
        </div>
      </Layout>
    )
  }

  if (!loanAccountNumber || !loanAccount) {
    return (
      <Layout>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="text-center py-12">
            <p className="text-gray-600">Không tìm thấy khoản vay</p>
            <button
              onClick={() => navigate('/loan/my-loans')}
              className="mt-4 text-blue-600 hover:text-blue-700 font-medium"
            >
              ← Quay lại danh sách
            </button>
          </div>
        </div>
      </Layout>
    )
  }

  return (
    <Layout>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <button
          onClick={() => navigate('/loan/my-loans')}
          className="mb-4 text-blue-600 hover:text-blue-700 flex items-center"
        >
          <span className="material-icons-round text-sm mr-1">arrow_back</span>
          Quay lại
        </button>

        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-900">Thanh toán khoản vay</h1>
          <p className="text-gray-600 mt-1">Số hợp đồng: {loanAccount.loanNumber}</p>
        </div>

        {/* Loan summary */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
          <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
            <div>
              <p className="text-sm text-gray-600">Số tiền vay</p>
              <p className="font-bold text-blue-600">{formatCurrency(loanAccount.approvedAmount)}</p>
            </div>
            <div>
              <p className="text-sm text-gray-600">Dư nợ gốc</p>
              <p className="font-bold text-red-600">
                {formatCurrency(loanAccount.outstandingPrincipal)}
              </p>
            </div>
            <div>
              <p className="text-sm text-gray-600">Lãi suất</p>
              <p className="font-semibold text-green-600">{loanAccount.interestRate}%/năm</p>
            </div>
            <div>
              <p className="text-sm text-gray-600">Kỳ hạn</p>
              <p className="font-medium">{loanAccount.tenor} tháng</p>
            </div>
            <div>
              <p className="text-sm text-gray-600">Phương thức</p>
              <p className="font-medium">{loanAccount.repaymentMethodLabel}</p>
            </div>
          </div>
        </div>

        {/* Tabs */}
        <div className="mb-6 border-b border-gray-200">
          <div className="flex space-x-8">
            <button
              onClick={() => setActiveTab('schedule')}
              className={`pb-4 px-1 border-b-2 font-medium text-sm ${
                activeTab === 'schedule'
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              Lịch trả nợ
            </button>
            <button
              onClick={() => setActiveTab('history')}
              className={`pb-4 px-1 border-b-2 font-medium text-sm ${
                activeTab === 'history'
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              Lịch sử thanh toán
            </button>
          </div>
        </div>

        {/* Schedule Tab */}
        {activeTab === 'schedule' && (
          <div className="space-y-6">
            {/* Current payment */}
            {selectedInstallment && (
              <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
                <h3 className="font-semibold text-gray-900 mb-4">Kỳ thanh toán hiện tại</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
                  <div>
                    <p className="text-sm text-gray-600">Kỳ số</p>
                    <p className="font-semibold">{selectedInstallment.installmentNo}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Ngày đến hạn</p>
                    <p className="font-semibold">{formatDate(selectedInstallment.dueDate)}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Tiền gốc</p>
                    <p className="font-bold">{formatCurrency(selectedInstallment.principalAmount)}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Tiền lãi</p>
                    <p className="font-bold">{formatCurrency(selectedInstallment.interestAmount)}</p>
                  </div>
                </div>
                <div className="mb-4">
                  <p className="text-sm text-gray-600 mb-1">Tổng phải trả</p>
                  <p className="text-2xl font-bold text-blue-600">
                    {formatCurrency(selectedInstallment.totalAmount)}
                  </p>
                </div>

                <div className="space-y-3">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Tài khoản thanh toán
                    </label>
                    <select
                      value={sourceAccountNumber}
                      onChange={(e) => setSourceAccountNumber(e.target.value)}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    >
                      <option value="">Chọn tài khoản</option>
                      {sourceAccounts.map((account) => (
                        <option key={account.accountNumber} value={account.accountNumber}>
                          {account.accountNumber} - {account.accountTypeLabel}
                        </option>
                      ))}
                    </select>
                  </div>

                  <button
                    onClick={handlePayment}
                    disabled={!sourceAccountNumber || loading}
                    className="w-full bg-blue-600 hover:bg-blue-700 text-white py-3 rounded-lg font-medium disabled:bg-gray-300 disabled:cursor-not-allowed"
                  >
                    {loading ? 'Đang xử lý...' : 'Xác nhận thanh toán'}
                  </button>
                </div>
              </div>
            )}

            {/* Schedule table */}
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                        Kỳ
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                        Ngày đến hạn
                      </th>
                      <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">
                        Tiền gốc
                      </th>
                      <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">
                        Tiền lãi
                      </th>
                      <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">
                        Tổng phải trả
                      </th>
                      <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase">
                        Trạng thái
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {schedule.map((item) => (
                      <tr
                        key={item.installmentNo}
                        className={
                          selectedInstallment?.installmentNo === item.installmentNo
                            ? 'bg-blue-50'
                            : 'hover:bg-gray-50'
                        }
                      >
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                          {item.installmentNo}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                          {formatDate(item.dueDate)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 text-right">
                          {formatCurrency(item.principalAmount)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 text-right">
                          {formatCurrency(item.interestAmount)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-semibold text-gray-900 text-right">
                          {formatCurrency(item.totalAmount)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-center">
                          <span
                            className={`px-2 py-1 rounded-full text-xs font-medium ${getInstallmentStatusColor(
                              item.status
                            )}`}
                          >
                            {item.statusLabel}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {/* History Tab */}
        {activeTab === 'history' && (
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
            {paymentHistory.length === 0 ? (
              <div className="text-center py-12">
                <p className="text-gray-600">Chưa có giao dịch thanh toán nào</p>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                        Ngày thanh toán
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                        Kỳ thanh toán
                      </th>
                      <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">
                        Tiền gốc
                      </th>
                      <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">
                        Tiền lãi
                      </th>
                      <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">
                        Phí phạt
                      </th>
                      <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">
                        Tổng thanh toán
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {paymentHistory.map((payment, index) => (
                      <tr key={index} className="hover:bg-gray-50">
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                          {formatDate(payment.paidDate)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                          Lịch sử thanh toán
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 text-right">
                          {formatCurrency(payment.principalPaid)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 text-right">
                          {formatCurrency(payment.interestPaid)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-red-600 text-right">
                          {formatCurrency(payment.penaltyPaid || 0)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-semibold text-gray-900 text-right">
                          {formatCurrency(payment.paidAmount)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
      </div>
    </Layout>
  )
}
