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
                          {getTransferTypeBadge(txn.transferType || 'INTERNAL')}
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

        {/* Transaction Detail Modal - Simple version */}
{selectedTransaction && (
  <div className="fixed inset-0 z-50 overflow-y-auto">
    {/* Background overlay */}
    <div 
      className="fixed inset-0 bg-black/20"
      onClick={() => setSelectedTransaction(null)}
    />
    
    {/* Modal container */}
    <div className="flex min-h-full items-center justify-center p-4">
      {/* Modal content */}
      <div className="relative bg-white rounded-lg shadow-lg w-full max-w-lg">
        {/* Header */}
        <div className="px-6 py-4 border-b">
          <div className="flex justify-between items-center">
            <h2 className="text-xl font-semibold text-gray-800">Chi tiết giao dịch</h2>
            <button
              onClick={() => setSelectedTransaction(null)}
              className="p-1 hover:bg-gray-100 rounded"
            >
              <svg 
                className="w-5 h-5 text-gray-500" 
                fill="none" 
                stroke="currentColor" 
                viewBox="0 0 24 24"
              >
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>

        {/* Transaction Info */}
        <div className="px-6 py-4 space-y-4 max-h-[60vh] overflow-y-auto">
          {/* Basic Info */}
          <div className="space-y-3">
            <div className="flex justify-between items-start">
              <div>
                <div className="text-sm text-gray-500">Mã giao dịch</div>
                <div className="font-medium text-gray-800">{selectedTransaction.transactionId}</div>
              </div>
              {getStatusBadge(selectedTransaction.status)}
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <div className="text-sm text-gray-500">Loại giao dịch</div>
                <div className="font-medium">{getTransferTypeBadge(selectedTransaction.transferType || 'INTERNAL')}</div>
              </div>
              <div>
                <div className="text-sm text-gray-500">Thời gian</div>
                <div className="font-medium">{formatDate(selectedTransaction.createdAt)}</div>
              </div>
            </div>
          </div>

          {/* Account Info */}
          <div className="border-t pt-4">
            <h3 className="font-medium text-gray-700 mb-3">Thông tin tài khoản</h3>
            <div className="space-y-3">
              <div>
                <div className="text-sm text-gray-500">Tài khoản nguồn</div>
                <div className="font-medium">{selectedTransaction.sourceAccountNumber}</div>
              </div>
              <div>
                <div className="text-sm text-gray-500">Tài khoản đích</div>
                <div className="font-medium">{selectedTransaction.destinationAccountNumber}</div>
                {selectedTransaction.destinationBankName && (
                  <div className="text-sm text-gray-500">{selectedTransaction.destinationBankName}</div>
                )}
              </div>
            </div>
          </div>

          {/* Amount Info */}
          <div className="border-t pt-4">
            <h3 className="font-medium text-gray-700 mb-3">Số tiền</h3>
            <div className="space-y-2">
              <div className="flex justify-between">
                <span className="text-gray-600">Số tiền:</span>
                <span className="font-semibold text-lg text-blue-600">
                  {formatCurrency(selectedTransaction.amount)}
                </span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">Phí giao dịch:</span>
                <span className="font-medium">{formatCurrency(selectedTransaction.fee || 0)}</span>
              </div>
              <div className="flex justify-between pt-2 border-t">
                <span className="text-gray-700 font-medium">Tổng tiền:</span>
                <span className="font-bold text-green-600">
                  {formatCurrency((selectedTransaction.totalAmount) || (selectedTransaction.amount + (selectedTransaction.fee || 0)))}
                </span>
              </div>
            </div>
          </div>

          {/* Description */}
          <div className="border-t pt-4">
            <h3 className="font-medium text-gray-700 mb-2">Nội dung giao dịch</h3>
            <div className="bg-gray-50 p-3 rounded border">
              <div className="text-gray-800">{selectedTransaction.description}</div>
            </div>
          </div>

          {/* Completed time if available */}
          {selectedTransaction.completedAt && (
            <div className="border-t pt-4">
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">Thời gian hoàn thành:</span>
                <span className="font-medium">{formatDate(selectedTransaction.completedAt)}</span>
              </div>
            </div>
          )}
        </div>

        {/* Footer buttons */}
        <div className="px-6 py-4 border-t bg-gray-50">
          <div className="flex gap-3">
            <button
              onClick={() => setSelectedTransaction(null)}
              className="flex-1 px-4 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300 transition-colors font-medium"
            >
              Đóng
            </button>
            <button
              onClick={() => {
                // Add print or share functionality here
                console.log('Print transaction:', selectedTransaction.transactionId)
              }}
              className="flex-1 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition-colors font-medium"
            >
              In biên lai
            </button>
          </div>
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