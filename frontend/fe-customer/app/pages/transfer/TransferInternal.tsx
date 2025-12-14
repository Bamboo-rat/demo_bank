import React, { useState } from 'react'
import { useNavigate } from 'react-router'
import Layout from '~/component/layout/Layout'
import { transactionService, type TransferRequest, type TransferResponse, type AccountInfo } from '~/service/transactionService'
import { accountService, type AccountSummary } from '~/service/accountService'

interface TransferStep {
  step: 'input' | 'confirm' | 'otp' | 'success'
}

const TransferInternal = () => {
  const navigate = useNavigate()
  const [currentStep, setCurrentStep] = useState<TransferStep['step']>('input')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string>('')
  
  // Form data
  const [sourceAccount, setSourceAccount] = useState<AccountSummary | null>(null)
  const [destinationAccountNumber, setDestinationAccountNumber] = useState('')
  const [destinationAccountInfo, setDestinationAccountInfo] = useState<AccountInfo | null>(null)
  const [amount, setAmount] = useState('')
  const [description, setDescription] = useState('')
  const [feePaymentMethod, setFeePaymentMethod] = useState<'SOURCE' | 'DESTINATION'>('SOURCE')
  
  // Transaction data
  const [transactionData, setTransactionData] = useState<TransferResponse | null>(null)
  const [otp, setOtp] = useState('')
  
  // Load user accounts
  const [myAccounts, setMyAccounts] = useState<AccountSummary[]>([])
  
  React.useEffect(() => {
    loadMyAccounts()
  }, [])
  
  const loadMyAccounts = async () => {
    try {
      const accounts = await accountService.getMyAccounts()
      setMyAccounts(accounts)
      if (accounts.length > 0) {
        setSourceAccount(accounts[0])
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Không thể tải danh sách tài khoản')
    }
  }
  
  const handleGetAccountInfo = async () => {
    if (!destinationAccountNumber.trim()) {
      setError('Vui lòng nhập số tài khoản người nhận')
      return
    }
    
    if (!sourceAccount) {
      setError('Vui lòng chọn tài khoản nguồn')
      return
    }
    
    // Check if user is trying to transfer to their own account
    if (destinationAccountNumber.trim() === sourceAccount.accountNumber) {
      setError('Không thể chuyển tiền cho chính mình')
      setDestinationAccountInfo(null)
      return
    }
    
    setLoading(true)
    setError('')
    
    try {
      const info = await transactionService.getAccountInfo(destinationAccountNumber)
      setDestinationAccountInfo(info)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Không tìm thấy tài khoản')
      setDestinationAccountInfo(null)
    } finally {
      setLoading(false)
    }
  }
  
  const handleSubmitTransfer = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!sourceAccount) {
      setError('Vui lòng chọn tài khoản nguồn')
      return
    }
    
    if (!destinationAccountInfo) {
      setError('Vui lòng kiểm tra thông tin tài khoản người nhận')
      return
    }
    
    const amountNum = parseFloat(amount)
    if (isNaN(amountNum) || amountNum <= 0) {
      setError('Số tiền không hợp lệ')
      return
    }
    
    setCurrentStep('confirm')
  }
  
  const handleConfirmTransfer = async () => {
    if (!sourceAccount) return
    
    setLoading(true)
    setError('')
    
    try {
      const request: TransferRequest = {
        sourceAccountNumber: sourceAccount.accountNumber,
        destinationAccountNumber: destinationAccountNumber,
        amount: parseFloat(amount),
        description: description || 'Chuyển tiền nội bộ',
        feePaymentMethod,
        transferType: 'INTERNAL'
      }
      
      const response = await transactionService.initiateTransfer(request)
      setTransactionData(response)
      setCurrentStep('otp')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Không thể khởi tạo giao dịch')
    } finally {
      setLoading(false)
    }
  }
  
  const handleVerifyOTP = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!transactionData) return
    
    if (!otp.trim() || otp.length !== 6) {
      setError('Vui lòng nhập mã OTP 6 số')
      return
    }
    
    setLoading(true)
    setError('')
    
    try {
      const confirmed = await transactionService.confirmTransfer({
        transactionId: transactionData.transactionId,
        otp
      })
      setTransactionData(confirmed)
      setCurrentStep('success')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Mã OTP không đúng')
    } finally {
      setLoading(false)
    }
  }
  
  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
  }
  
  const resetForm = () => {
    setCurrentStep('input')
    setDestinationAccountNumber('')
    setDestinationAccountInfo(null)
    setAmount('')
    setDescription('')
    setOtp('')
    setTransactionData(null)
    setError('')
  }

  return (
    <Layout>
      <div className="max-w-4xl mx-auto p-6">
        {/* Header */}
        <div className="mb-6">
          <h1 className="text-3xl font-bold text-gray-900">Chuyển tiền nội bộ</h1>
          <p className="text-gray-600 mt-2">Chuyển tiền đến tài khoản KienlongBank khác</p>
        </div>

        {/* Progress Steps */}
        <div className="mb-8">
          <div className="flex items-center justify-between">
            {['Nhập thông tin', 'Xác nhận', 'Xác thực OTP', 'Hoàn thành'].map((label, idx) => (
              <div key={idx} className="flex-1 flex items-center">
                <div className={`flex items-center justify-center w-10 h-10 rounded-full ${
                  idx <= ['input', 'confirm', 'otp', 'success'].indexOf(currentStep)
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-300 text-gray-600'
                }`}>
                  {idx + 1}
                </div>
                <div className="flex-1 ml-2 text-sm font-medium text-gray-700">{label}</div>
                {idx < 3 && <div className="flex-1 h-1 bg-gray-300 mx-2" />}
              </div>
            ))}
          </div>
        </div>

        {/* Error Message */}
        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
            {error}
          </div>
        )}

        {/* Step 1: Input Form */}
        {currentStep === 'input' && (
          <form onSubmit={handleSubmitTransfer} className="bg-white rounded-lg shadow p-6">
            <div className="space-y-6">
              {/* Source Account */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Từ tài khoản <span className="text-red-500">*</span>
                </label>
                <select
                  value={sourceAccount?.accountNumber || ''}
                  onChange={(e) => setSourceAccount(myAccounts.find(a => a.accountNumber === e.target.value) || null)}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  required
                >
                  {myAccounts.map(account => (
                    <option key={account.accountId} value={account.accountNumber}>
                      {account.accountNumber} - {account.accountTypeLabel}
                    </option>
                  ))}
                </select>
              </div>

              {/* Destination Account */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Đến tài khoản <span className="text-red-500">*</span>
                </label>
                <div className="flex gap-2">
                  <input
                    type="text"
                    value={destinationAccountNumber}
                    onChange={(e) => setDestinationAccountNumber(e.target.value)}
                    onKeyPress={(e) => e.key === 'Enter' && (e.preventDefault(), handleGetAccountInfo())}
                    placeholder="Nhập số tài khoản"
                    className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    required
                  />
                  <button
                    type="button"
                    onClick={handleGetAccountInfo}
                    disabled={loading}
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400"
                  >
                    {loading ? 'Đang kiểm tra...' : 'Kiểm tra'}
                  </button>
                </div>
                
                {destinationAccountInfo && (
                  <div className="mt-2 p-3 bg-green-50 border border-green-100 rounded">
                    <p className="text-md text-black">
                      {destinationAccountInfo.accountHolderName}
                    </p>
                  </div>
                )}
              </div>

              {/* Amount */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Số tiền <span className="text-red-500">*</span>
                </label>
                <input
                  type="number"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  placeholder="0"
                  min="1000"
                  step="1000"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  required
                />
                {amount && !isNaN(parseFloat(amount)) && (
                  <p className="text-sm text-gray-600 mt-1">{formatCurrency(parseFloat(amount))}</p>
                )}
              </div>

              {/* Description */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Nội dung chuyển tiền
                </label>
                <input
                  type="text"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Nhập nội dung chuyển tiền"
                  maxLength={200}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                />
              </div>

              {/* Actions */}
              <div className="flex gap-4 pt-4">
                <button
                  type="button"
                  onClick={() => navigate('/transfer')}
                  className="flex-1 px-6 py-3 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  disabled={!destinationAccountInfo || loading}
                  className="flex-1 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400"
                >
                  Tiếp tục
                </button>
              </div>
            </div>
          </form>
        )}

        {/* Step 2: Confirmation */}
        {currentStep === 'confirm' && destinationAccountInfo && (
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-xl font-semibold mb-4">Xác nhận thông tin chuyển tiền</h2>
            
            <div className="space-y-4 mb-6">
              <div className="flex justify-between py-2 border-b">
                <span className="text-gray-600">Từ tài khoản:</span>
                <span className="font-semibold">{sourceAccount?.accountNumber}</span>
              </div>
              <div className="flex justify-between py-2 border-b">
                <span className="text-gray-600">Đến tài khoản:</span>
                <span className="font-semibold">{destinationAccountNumber}</span>
              </div>
              <div className="flex justify-between py-2 border-b">
                <span className="text-gray-600">Tên người nhận:</span>
                <span className="font-semibold">{destinationAccountInfo.accountHolderName}</span>
              </div>
              <div className="flex justify-between py-2 border-b">
                <span className="text-gray-600">Ngân hàng:</span>
                <span className="font-semibold">KienlongBank</span>
              </div>
              <div className="flex justify-between py-2 border-b">
                <span className="text-gray-600">Số tiền:</span>
                <span className="font-semibold text-blue-600">{formatCurrency(parseFloat(amount))}</span>
              </div>
              <div className="flex justify-between py-2 border-b">
                <span className="text-gray-600">Phí giao dịch:</span>
                <span className="font-semibold">0 VND (Miễn phí)</span>
              </div>
              <div className="flex justify-between py-2 border-b">
                <span className="text-gray-600">Nội dung:</span>
                <span className="font-semibold">{description || 'Chuyển tiền nội bộ'}</span>
              </div>
            </div>

            <div className="flex gap-4">
              <button
                type="button"
                onClick={() => setCurrentStep('input')}
                className="flex-1 px-6 py-3 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300"
              >
                Quay lại
              </button>
              <button
                type="button"
                onClick={handleConfirmTransfer}
                disabled={loading}
                className="flex-1 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400"
              >
                {loading ? 'Đang xử lý...' : 'Xác nhận chuyển tiền'}
              </button>
            </div>
          </div>
        )}

        {/* Step 3: OTP Verification */}
        {currentStep === 'otp' && transactionData && (
          <form onSubmit={handleVerifyOTP} className="bg-white rounded-lg shadow p-6">
            <h2 className="text-xl font-semibold mb-4">Xác thực giao dịch</h2>
            
            <div className="mb-6">
              <p className="text-gray-600 mb-4">
                Mã OTP đã được gửi đến số điện thoại của bạn. Vui lòng nhập mã để hoàn tất giao dịch.
              </p>
              
              <div className="bg-blue-50 border border-blue-200 rounded p-4 mb-4">
                <p className="text-sm text-blue-800">
                  <strong>Mã giao dịch:</strong> {transactionData.transactionId}
                </p>
              </div>

              <label className="block text-sm font-medium text-gray-700 mb-2">
                Mã OTP <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                value={otp}
                onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                placeholder="Nhập mã OTP 6 số"
                maxLength={6}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-center text-2xl tracking-widest"
                required
                autoFocus
              />
            </div>

            <div className="flex gap-4">
              <button
                type="button"
                onClick={() => setCurrentStep('confirm')}
                className="flex-1 px-6 py-3 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300"
              >
                Quay lại
              </button>
              <button
                type="submit"
                disabled={loading || otp.length !== 6}
                className="flex-1 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400"
              >
                {loading ? 'Đang xác thực...' : 'Xác nhận'}
              </button>
            </div>
          </form>
        )}

        {/* Step 4: Success */}
        {currentStep === 'success' && transactionData && (
          <div className="bg-white rounded-lg shadow p-6 text-center">
            <div className="mb-6">
              <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg className="w-12 h-12 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
                </svg>
              </div>
              <h2 className="text-2xl font-bold text-green-600 mb-2">Giao dịch thành công!</h2>
              <p className="text-gray-600">Giao dịch của bạn đã được thực hiện thành công</p>
            </div>

            <div className="bg-gray-50 rounded-lg p-4 mb-6 text-left">
              <div className="space-y-3">
                <div className="flex justify-between">
                  <span className="text-gray-600">Mã giao dịch:</span>
                  <span className="font-semibold">{transactionData.transactionId}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Số tiền:</span>
                  <span className="font-semibold text-green-600">{formatCurrency(transactionData.amount)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Người nhận:</span>
                  <span className="font-semibold">{destinationAccountInfo?.accountHolderName}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Thời gian:</span>
                  <span className="font-semibold">{new Date(transactionData.createdAt).toLocaleString('vi-VN')}</span>
                </div>
              </div>
            </div>

            <div className="flex gap-4">
              <button
                type="button"
                onClick={resetForm}
                className="flex-1 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
              >
                Giao dịch mới
              </button>
              <button
                type="button"
                onClick={() => navigate('/transactions')}
                className="flex-1 px-6 py-3 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300"
              >
                Xem lịch sử
              </button>
            </div>
          </div>
        )}
      </div>
    </Layout>
  )
}

export default TransferInternal
