import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { savingsService } from '~/service/savingsService'
import type { SavingsAccount } from '~/type/savings'
import Layout from '~/component/layout/Layout'

export default function SavingsAccountList() {
  const [accounts, setAccounts] = useState<SavingsAccount[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    loadSavingsAccounts()
  }, [])

  const loadSavingsAccounts = async () => {
    try {
      setLoading(true)
      setError(null)
      const data = await savingsService.getMySavingsAccounts()
      setAccounts(data)
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
        return 'bg-green-100 text-green-800'
      case 'MATURED':
        return 'bg-blue-100 text-blue-800'
      case 'CLOSED':
        return 'bg-gray-100 text-gray-800'
      default:
        return 'bg-yellow-100 text-yellow-800'
    }
  }

  return (
    <Layout>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-6 flex justify-between items-center">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Sổ tiết kiệm</h1>
            <p className="text-gray-600 mt-1">Quản lý các sổ tiết kiệm của bạn</p>
          </div>
          <Link
            to="/saving/open"
            className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2.5 rounded-lg font-medium transition-colors"
          >
            Mở sổ mới
          </Link>
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

        {!loading && !error && accounts.length === 0 && (
          <div className="text-center py-12 bg-gray-50 rounded-lg">
            <span className="material-icons-round text-gray-400 text-6xl">savings</span>
            <p className="text-gray-600 mt-4">Bạn chưa có sổ tiết kiệm nào</p>
            <Link
              to="/saving/open"
              className="inline-block mt-4 text-blue-600 hover:text-blue-700 font-medium"
            >
              Mở sổ tiết kiệm ngay →
            </Link>
          </div>
        )}

        {!loading && !error && accounts.length > 0 && (
          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {accounts.map((account) => (
              <div
                key={account.accountNumber}
                className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow"
              >
                <div className="flex justify-between items-start mb-4">
                  <div>
                    <p className="text-sm text-gray-600">Số sổ</p>
                    <p className="font-mono font-semibold text-gray-900">
                      {account.accountNumber}
                    </p>
                  </div>
                  <span
                    className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(
                      account.status
                    )}`}
                  >
                    {account.statusLabel}
                  </span>
                </div>

                <div className="space-y-3 mb-4">
                  <div>
                    <p className="text-sm text-gray-600">Loại sổ</p>
                    <p className="font-medium text-gray-900">{account.savingsTypeName}</p>
                  </div>

                  <div>
                    <p className="text-sm text-gray-600">Số tiền gửi</p>
                    <p className="text-lg font-bold text-blue-600">
                      {formatCurrency(account.depositAmount)}
                    </p>
                  </div>

                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <p className="text-sm text-gray-600">Lãi suất</p>
                      <p className="font-semibold text-green-600">
                        {account.interestRate}%/năm
                      </p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600">Kỳ hạn</p>
                      <p className="font-semibold text-gray-900">{account.termMonths} tháng</p>
                    </div>
                  </div>

                  <div>
                    <p className="text-sm text-gray-600">Ngày đáo hạn</p>
                    <p className="font-medium text-gray-900">{formatDate(account.maturityDate)}</p>
                  </div>

                  <div>
                    <p className="text-sm text-gray-600">Tổng nhận khi đáo hạn</p>
                    <p className="text-lg font-bold text-blue-600">
                      {formatCurrency(account.maturityAmount)}
                    </p>
                  </div>
                </div>

                {account.status === 'MATURED' && (
                  <Link
                    to={`/saving/close?account=${account.accountNumber}`}
                    className="block w-full text-center bg-blue-600 hover:bg-blue-700 text-white py-2 rounded-lg font-medium transition-colors"
                  >
                    Tất toán
                  </Link>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </Layout>
  )
}
