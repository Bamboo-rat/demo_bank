import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router'
import { savingsService } from '~/service/savingsService'
import { accountService } from '~/service/accountService'
import type { SavingsAccount } from '~/type/savings'
import type { AccountSummary } from '~/service/accountService'
import Layout from '~/component/layout/Layout'

export default function CloseSavingsAccount() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const accountNumber = searchParams.get('account')

  const [loading, setLoading] = useState(false)
  const [savingsAccount, setSavingsAccount] = useState<SavingsAccount | null>(null)
  const [destinationAccounts, setDestinationAccounts] = useState<AccountSummary[]>([])
  const [destinationAccountNumber, setDestinationAccountNumber] = useState('')
  const [confirming, setConfirming] = useState(false)

  useEffect(() => {
    if (accountNumber) {
      loadData()
    }
  }, [accountNumber])

  const loadData = async () => {
    if (!accountNumber) return

    try {
      setLoading(true)
      const [savingsData, accountsData] = await Promise.all([
        savingsService.getSavingsAccountDetail(accountNumber),
        accountService.getMyAccounts()
      ])

      setSavingsAccount(savingsData)
      setDestinationAccounts(
        accountsData.filter((a) => a.accountType === 'CHECKING' && a.status === 'ACTIVE')
      )
    } catch (error) {
      console.error('Failed to load data:', error)
      alert(error instanceof Error ? error.message : 'Có lỗi xảy ra')
    } finally {
      setLoading(false)
    }
  }

  const handleClose = async () => {
    if (!accountNumber || !destinationAccountNumber) return

    try {
      setLoading(true)
      await savingsService.closeSavingsAccount({
        accountNumber: accountNumber,
        destinationAccountNumber
      })

      alert('Tất toán sổ tiết kiệm thành công!')
      navigate('/saving/books')
    } catch (error) {
      alert(error instanceof Error ? error.message : 'Có lỗi xảy ra')
    } finally {
      setLoading(false)
      setConfirming(false)
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

  if (loading && !savingsAccount) {
    return (
      <Layout>
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="text-center py-12">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            <p className="text-gray-600 mt-4">Đang tải...</p>
          </div>
        </div>
      </Layout>
    )
  }

  if (!accountNumber || !savingsAccount) {
    return (
      <Layout>
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="text-center py-12">
            <p className="text-gray-600">Không tìm thấy sổ tiết kiệm</p>
            <button
              onClick={() => navigate('/saving/books')}
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
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-6">
          <button
            onClick={() => navigate('/saving/books')}
            className="mb-4 text-blue-600 hover:text-blue-700 flex items-center"
          >
            <span className="material-icons-round text-sm mr-1">arrow_back</span>
            Quay lại
          </button>
          <h1 className="text-2xl font-bold text-gray-900">Tất toán sổ tiết kiệm</h1>
          <p className="text-gray-600 mt-1">Xác nhận thông tin trước khi tất toán</p>
        </div>

        <div className="space-y-6">
          {/* Savings account info */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 className="text-lg font-semibold mb-4">Thông tin sổ tiết kiệm</h2>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-gray-600">Số sổ</p>
                <p className="font-mono font-semibold">{savingsAccount.accountNumber}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Loại sổ</p>
                <p className="font-medium">{savingsAccount.savingsTypeName}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Số tiền gửi</p>
                <p className="font-bold text-blue-600">
                  {formatCurrency(savingsAccount.depositAmount)}
                </p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Lãi suất</p>
                <p className="font-semibold text-green-600">{savingsAccount.interestRate}%/năm</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Ngày mở</p>
                <p className="font-medium">{formatDate(savingsAccount.createdAt)}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Ngày đáo hạn</p>
                <p className="font-medium">{formatDate(savingsAccount.maturityDate)}</p>
              </div>
            </div>
          </div>

          {/* Settlement amount */}
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
            <h3 className="font-semibold text-gray-900 mb-3">Số tiền nhận được</h3>
            <div className="space-y-2">
              <div className="flex justify-between">
                <span className="text-gray-600">Tiền gốc:</span>
                <span className="font-semibold">{formatCurrency(savingsAccount.depositAmount)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Lãi:</span>
                <span className="font-semibold text-green-600">
                  {formatCurrency(
                    savingsAccount.maturityAmount - savingsAccount.depositAmount
                  )}
                </span>
              </div>
              <div className="flex justify-between border-t border-blue-300 pt-2">
                <span className="font-bold text-gray-900">Tổng cộng:</span>
                <span className="text-2xl font-bold text-blue-600">
                  {formatCurrency(savingsAccount.maturityAmount)}
                </span>
              </div>
            </div>
          </div>

          {/* Destination account */}
          {!confirming && (
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
              <h2 className="text-lg font-semibold mb-4">Chọn tài khoản nhận tiền</h2>
              <select
                value={destinationAccountNumber}
                onChange={(e) => setDestinationAccountNumber(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">Chọn tài khoản</option>
                {destinationAccounts.map((account) => (
                  <option key={account.accountNumber} value={account.accountNumber}>
                    {account.accountNumber} - {account.accountTypeLabel}
                  </option>
                ))}
              </select>

              <div className="mt-6">
                <button
                  onClick={() => setConfirming(true)}
                  disabled={!destinationAccountNumber}
                  className="w-full bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-medium disabled:bg-gray-300 disabled:cursor-not-allowed"
                >
                  Tiếp tục
                </button>
              </div>
            </div>
          )}

          {/* Confirmation */}
          {confirming && (
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
              <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6">
                <div className="flex">
                  <span className="material-icons-round text-yellow-600 mr-3">warning</span>
                  <div>
                    <p className="font-semibold text-yellow-900 mb-1">Lưu ý quan trọng</p>
                    <p className="text-sm text-yellow-800">
                      Sau khi tất toán, sổ tiết kiệm sẽ bị đóng và không thể khôi phục. Vui lòng
                      kiểm tra kỹ thông tin trước khi xác nhận.
                    </p>
                  </div>
                </div>
              </div>

              <div className="space-y-3 mb-6">
                <div className="flex justify-between py-2 border-b border-gray-200">
                  <span className="text-gray-600">Số sổ tất toán:</span>
                  <span className="font-mono font-semibold">{savingsAccount.accountNumber}</span>
                </div>
                <div className="flex justify-between py-2 border-b border-gray-200">
                  <span className="text-gray-600">Tài khoản nhận tiền:</span>
                  <span className="font-mono font-semibold">{destinationAccountNumber}</span>
                </div>
                <div className="flex justify-between py-2">
                  <span className="text-gray-600">Số tiền nhận:</span>
                  <span className="text-xl font-bold text-blue-600">
                    {formatCurrency(savingsAccount.maturityAmount)}
                  </span>
                </div>
              </div>

              <div className="flex gap-3">
                <button
                  onClick={() => setConfirming(false)}
                  disabled={loading}
                  className="flex-1 px-6 py-3 border border-gray-300 text-gray-700 rounded-lg font-medium hover:bg-gray-50 disabled:opacity-50"
                >
                  Quay lại
                </button>
                <button
                  onClick={handleClose}
                  disabled={loading}
                  className="flex-1 bg-red-600 hover:bg-red-700 text-white px-6 py-3 rounded-lg font-medium disabled:bg-gray-300 disabled:cursor-not-allowed"
                >
                  {loading ? 'Đang xử lý...' : 'Xác nhận tất toán'}
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </Layout>
  )
}
