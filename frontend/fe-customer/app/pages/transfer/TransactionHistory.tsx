import React, { useState } from 'react'
import Layout from '~/component/layout/Layout'
import { transactionService, type TransferResponse } from '~/service/transactionService'
import { accountService, type AccountSummary } from '~/service/accountService'

const TransactionHistory = () => {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string>('')
  const [transactions, setTransactions] = useState<TransferResponse[]>([])
  const [selectedAccount, setSelectedAccount] = useState<AccountSummary | null>(null)
  const [myAccounts, setMyAccounts] = useState<AccountSummary[]>([])
  const [page, setPage] = useState(0)
  const [hasMore, setHasMore] = useState(true)
  const [selectedTransaction, setSelectedTransaction] = useState<TransferResponse | null>(null)
  
  React.useEffect(() => {
    loadMyAccounts()
  }, [])
  
  React.useEffect(() => {
    if (selectedAccount) {
      loadTransactions()
    }
  }, [selectedAccount, page])
  
  const loadMyAccounts = async () => {
    try {
      const accounts = await accountService.getMyAccounts()
      setMyAccounts(accounts)
      if (accounts.length > 0) {
        setSelectedAccount(accounts[0])
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Không thể tải danh sách tài khoản')
    }
  }
  
  const loadTransactions = async () => {
    if (!selectedAccount) return
    
    setLoading(true)
    setError('')
    
    try {
      const data = await transactionService.getTransactionHistory(
        selectedAccount.accountNumber,
        page,
        20
      )
      
      if (page === 0) {
        setTransactions(data)
      } else {
        setTransactions(prev => [...prev, ...data])
      }
      
      setHasMore(data.length === 20)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Không thể tải lịch sử giao dịch')
    } finally {
      setLoading(false)
    }
  }
  
  const handleLoadMore = () => {
    setPage(prev => prev + 1)
  }
  
  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
  }
  
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
  
  const getStatusBadge = (status: string) => {
    const statusMap: Record<string, { label: string; className: string }> = {
      PENDING: { label: 'Đang xử lý', className: 'bg-yellow-100 text-yellow-800' },
      COMPLETED: { label: 'Hoàn thành', className: 'bg-green-100 text-green-800' },
      FAILED: { label: 'Thất bại', className: 'bg-red-100 text-red-800' },
      CANCELLED: { label: 'Đã hủy', className: 'bg-gray-100 text-gray-800' }
    }
    
    const config = statusMap[status] || { label: status, className: 'bg-gray-100 text-gray-800' }
    
    return (
      <span className={`px-3 py-1 rounded-full text-sm font-medium ${config.className}`}>
        {config.label}
      </span>
    )
  }
  
  const getTransferTypeBadge = (type: string) => {
    const typeMap: Record<string, { label: string; className: string }> = {
      INTERNAL: { label: 'Nội bộ', className: 'bg-blue-100 text-blue-800' },
      INTERBANK: { label: 'Liên ngân hàng', className: 'bg-purple-100 text-purple-800' }
    }
    
    const config = typeMap[type] || { label: type, className: 'bg-gray-100 text-gray-800' }
    
    return (
      <span className={`px-3 py-1 rounded-full text-sm font-medium ${config.className}`}>
        {config.label}
      </span>
    )
  }

  return (
    <Layout>
      <div className="max-w-6xl mx-auto p-6">
        {/* Header */}
        <div className="mb-6">
          <h1 className="text-3xl font-bold text-gray-900">Lịch sử giao dịch</h1>
          <p className="text-gray-600 mt-2">Xem lại các giao dịch chuyển tiền của bạn</p>
        </div>

        {/* Account Selector */}
        <div className="bg-white rounded-lg shadow p-6 mb-6">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Chọn tài khoản
          </label>
          <select
            value={selectedAccount?.accountNumber || ''}
            onChange={(e) => {
              const account = myAccounts.find(a => a.accountNumber === e.target.value)
              setSelectedAccount(account || null)
              setPage(0)
              setTransactions([])
            }}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
          >
            {myAccounts.map(account => (
              <option key={account.accountId} value={account.accountNumber}>
                {account.accountNumber} - {account.accountTypeLabel}
              </option>
            ))}
          </select>
        </div>

        {/* Error Message */}
        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
            {error}
          </div>
        )}

        {/* Transactions List */}
        <div className="bg-white rounded-lg shadow overflow-hidden">
          {loading && transactions.length === 0 ? (
            <div className="p-8 text-center text-gray-500">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
              Đang tải dữ liệu...
            </div>
          ) : transactions.length === 0 ? (
            <div className="p-8 text-center text-gray-500">
              Chưa có giao dịch nào
            </div>
          ) : (
            <>
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-gray-50 border-b">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Mã GD
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Thời gian
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Loại
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Người nhận
                      </th>
                      <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Số tiền
                      </th>
                      <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Trạng thái
                      </th>
                      <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Chi tiết
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {transactions.map((txn) => (
                      <tr key={txn.transactionId} className="hover:bg-gray-50">
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                          {txn.transactionId.slice(0, 8)}...
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {formatDate(txn.createdAt)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          {getTransferTypeBadge(txn.transferType)}
                        </td>
                        <td className="px-6 py-4 text-sm text-gray-900">
                          <div>
                            <p className="font-medium">{txn.destinationAccountNumber}</p>
                            {txn.destinationBankName && (
                              <p className="text-gray-500 text-xs">{txn.destinationBankName}</p>
                            )}
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-right">
                          <span className={`font-semibold ${
                            txn.sourceAccountNumber === selectedAccount?.accountNumber
                              ? 'text-red-600'
                              : 'text-green-600'
                          }`}>
                            {txn.sourceAccountNumber === selectedAccount?.accountNumber ? '-' : '+'}
                            {formatCurrency(txn.amount)}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-center">
                          {getStatusBadge(txn.status)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-center">
                          <button
                            onClick={() => setSelectedTransaction(txn)}
                            className="text-blue-600 hover:text-blue-800 font-medium"
                          >
                            Xem
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Load More Button */}
              {hasMore && (
                <div className="border-t p-4 text-center">
                  <button
                    onClick={handleLoadMore}
                    disabled={loading}
                    className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400"
                  >
                    {loading ? 'Đang tải...' : 'Xem thêm'}
                  </button>
                </div>
              )}
            </>
          )}
        </div>

        {/* Transaction Detail Modal */}
        {selectedTransaction && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <div className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
              <div className="p-6">
                <div className="flex justify-between items-center mb-6">
                  <h2 className="text-2xl font-bold">Chi tiết giao dịch</h2>
                  <button
                    onClick={() => setSelectedTransaction(null)}
                    className="text-gray-500 hover:text-gray-700"
                  >
                    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                </div>

                <div className="space-y-4">
                  <div className="flex justify-between py-3 border-b">
                    <span className="text-gray-600">Mã giao dịch:</span>
                    <span className="font-semibold">{selectedTransaction.transactionId}</span>
                  </div>
                  <div className="flex justify-between py-3 border-b">
                    <span className="text-gray-600">Loại giao dịch:</span>
                    {getTransferTypeBadge(selectedTransaction.transferType)}
                  </div>
                  <div className="flex justify-between py-3 border-b">
                    <span className="text-gray-600">Tài khoản nguồn:</span>
                    <span className="font-semibold">{selectedTransaction.sourceAccountNumber}</span>
                  </div>
                  <div className="flex justify-between py-3 border-b">
                    <span className="text-gray-600">Tài khoản đích:</span>
                    <span className="font-semibold">{selectedTransaction.destinationAccountNumber}</span>
                  </div>
                  {selectedTransaction.destinationBankName && (
                    <div className="flex justify-between py-3 border-b">
                      <span className="text-gray-600">Ngân hàng nhận:</span>
                      <span className="font-semibold">{selectedTransaction.destinationBankName}</span>
                    </div>
                  )}
                  <div className="flex justify-between py-3 border-b">
                    <span className="text-gray-600">Số tiền:</span>
                    <span className="font-semibold text-blue-600">{formatCurrency(selectedTransaction.amount)}</span>
                  </div>
                  <div className="flex justify-between py-3 border-b">
                    <span className="text-gray-600">Phí giao dịch:</span>
                    <span className="font-semibold">{formatCurrency(selectedTransaction.fee)}</span>
                  </div>
                  <div className="flex justify-between py-3 border-b">
                    <span className="text-gray-600">Tổng tiền:</span>
                    <span className="font-semibold text-blue-600">{formatCurrency(selectedTransaction.totalAmount)}</span>
                  </div>
                  <div className="flex justify-between py-3 border-b">
                    <span className="text-gray-600">Nội dung:</span>
                    <span className="font-semibold text-right">{selectedTransaction.description}</span>
                  </div>
                  <div className="flex justify-between py-3 border-b">
                    <span className="text-gray-600">Trạng thái:</span>
                    {getStatusBadge(selectedTransaction.status)}
                  </div>
                  <div className="flex justify-between py-3 border-b">
                    <span className="text-gray-600">Thời gian tạo:</span>
                    <span className="font-semibold">{formatDate(selectedTransaction.createdAt)}</span>
                  </div>
                  {selectedTransaction.completedAt && (
                    <div className="flex justify-between py-3 border-b">
                      <span className="text-gray-600">Thời gian hoàn thành:</span>
                      <span className="font-semibold">{formatDate(selectedTransaction.completedAt)}</span>
                    </div>
                  )}
                </div>

                <div className="mt-6 flex gap-4">
                  <button
                    onClick={() => setSelectedTransaction(null)}
                    className="flex-1 px-6 py-3 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300"
                  >
                    Đóng
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </Layout>
  )
}

export default TransactionHistory
