import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router'
import Layout from '~/component/layout/Layout'
import { transactionService, type TransferRequest, type TransferResponse, type AccountInfo } from '~/service/transactionService'
import { accountService, type AccountSummary } from '~/service/accountService'
import { customerService, type CustomerProfile } from '~/service/customerService'
import ConfigDigitalOtp from '~/component/features/ConfigDigitalOtp'
import BeneficiarySelectModal from '~/component/features/beneficiary/BeneficiarySelectModal'
import SaveBeneficiaryForm from '~/component/features/beneficiary/SaveBeneficiaryForm'
import { digitalOtpService, type DigitalOtpStatus } from '~/service/digitalOtpService'
import type { Beneficiary } from '~/type/beneficiary'
import { beneficiaryService } from '~/service/beneficiaryService'
import {
  computeTotpToken,
  getCurrentTimeSlice,
  getStoredOtpSecret,
  getStoredSalt,
  getTimeRemainingInSlice,
  hashPinWithSalt
} from '~/utils/digitalOtp'

interface TransferStep {
  step: 'input' | 'confirm' | 'otp' | 'success'
}

const DIGITAL_PIN_LENGTH = 6

const buildDigitalOtpPayload = (transaction: TransferResponse, timeSlice: number): string => {
  const destinationBank = transaction.destinationBankCode ?? 'KIENLONG'
  return [
    transaction.transactionId,
    transaction.sourceAccountNumber,
    transaction.destinationAccountNumber,
    destinationBank,
    transaction.amount.toString(),
    timeSlice.toString()
  ].join('|')
}

const TransferInternal = () => {
  const navigate = useNavigate()
  const [customerId, setCustomerId] = useState('')
  const [currentStep, setCurrentStep] = useState<TransferStep['step']>('input')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string>('')

  // Initialize customerId from localStorage on client-side only
  useEffect(() => {
    if (typeof window !== 'undefined') {
      setCustomerId(localStorage.getItem('customerId') || '')
    }
  }, [])
  
  // Form data
  const [sourceAccount, setSourceAccount] = useState<AccountSummary | null>(null)
  const [destinationAccountNumber, setDestinationAccountNumber] = useState('')
  const [destinationAccountInfo, setDestinationAccountInfo] = useState<AccountInfo | null>(null)
  const [amount, setAmount] = useState('')
  const [description, setDescription] = useState('')
  const [feePaymentMethod, setFeePaymentMethod] = useState<'SOURCE' | 'DESTINATION'>('SOURCE')
  
  // Save to beneficiary
  const [showSaveBeneficiaryModal, setShowSaveBeneficiaryModal] = useState(false)
  const [isExistingBeneficiary, setIsExistingBeneficiary] = useState(false)
  const [saveBeneficiaryLoading, setSaveBeneficiaryLoading] = useState(false)
  const [showBeneficiaryModal, setShowBeneficiaryModal] = useState(false)
  
  // Transaction data
  const [transactionData, setTransactionData] = useState<TransferResponse | null>(null)
  const [digitalOtpPin, setDigitalOtpPin] = useState('')
  const [otpToken, setOtpToken] = useState('')
  const [otpCountdown, setOtpCountdown] = useState(0)
  const [generatingToken, setGeneratingToken] = useState(false)
  const tokenTimerRef = React.useRef<number | null>(null)
  const currentSliceRef = React.useRef<number | null>(null)
  
  // Load user accounts
  const [myAccounts, setMyAccounts] = useState<AccountSummary[]>([])
  const [profile, setProfile] = useState<CustomerProfile | null>(null)
  const [digitalOtpStatus, setDigitalOtpStatus] = useState<DigitalOtpStatus | null>(null)
  const [digitalOtpLoading, setDigitalOtpLoading] = useState(true)
  const [digitalOtpError, setDigitalOtpError] = useState('')
  const [showDigitalOtpModal, setShowDigitalOtpModal] = useState(false)
  
  React.useEffect(() => {
    loadMyAccounts()
    void initializeDigitalOtp()
  }, [])

  const stopTokenTimer = React.useCallback(() => {
    if (tokenTimerRef.current) {
      window.clearInterval(tokenTimerRef.current)
      tokenTimerRef.current = null
    }
  }, [])

  const refreshOtpToken = React.useCallback(async (secret: string) => {
    if (!transactionData) return
    const slice = getCurrentTimeSlice()
    const payload = buildDigitalOtpPayload(transactionData, slice)
    const token = await computeTotpToken(secret, payload)
    currentSliceRef.current = slice
    setOtpToken(token)
    setOtpCountdown(getTimeRemainingInSlice())
  }, [transactionData])

  const startTokenTimer = React.useCallback((secret: string) => {
    stopTokenTimer()
    void refreshOtpToken(secret)
    tokenTimerRef.current = window.setInterval(() => {
      setOtpCountdown(getTimeRemainingInSlice())
      const sliceNow = getCurrentTimeSlice()
      if (currentSliceRef.current !== sliceNow) {
        void refreshOtpToken(secret)
      }
    }, 1000)
  }, [refreshOtpToken, stopTokenTimer])

  React.useEffect(() => {
    return () => {
      stopTokenTimer()
    }
  }, [stopTokenTimer])
  
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

  const initializeDigitalOtp = async () => {
    setDigitalOtpLoading(true)
    setDigitalOtpError('')

    try {
      const profileData = await customerService.getMyProfile()
      setProfile(profileData)
      await fetchDigitalOtpStatus(profileData.customerId)
    } catch (err) {
      setDigitalOtpError(err instanceof Error ? err.message : 'Không thể kiểm tra Digital OTP')
    } finally {
      setDigitalOtpLoading(false)
    }
  }

  const fetchDigitalOtpStatus = async (customerIdValue: string) => {
    try {
      const status = await digitalOtpService.getStatus(customerIdValue)
      setDigitalOtpStatus(status)
      setShowDigitalOtpModal(!status.enrolled)
      setDigitalOtpError('')
    } catch (err) {
      setDigitalOtpError(err instanceof Error ? err.message : 'Không thể kiểm tra Digital OTP')
    }
  }

  const handleDigitalOtpSuccess = async () => {
    if (!profile?.customerId) return
    await fetchDigitalOtpStatus(profile.customerId)
    setShowDigitalOtpModal(false)
  }

  const digitalOtpReady = Boolean(digitalOtpStatus?.enrolled && !digitalOtpStatus.locked)
  const digitalOtpLockedUntil = digitalOtpStatus?.lockedUntilTimestamp
    ? new Date(digitalOtpStatus.lockedUntilTimestamp).toLocaleString('vi-VN')
    : null
  const disableTransfers = digitalOtpLoading || !digitalOtpReady
  
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
      
      // Check if this account is already in beneficiaries
      if (customerId) {
        try {
          const beneficiaries = await beneficiaryService.getAllBeneficiaries(customerId)
          const exists = beneficiaries.some(b => b.beneficiaryAccountNumber === destinationAccountNumber)
          setIsExistingBeneficiary(exists)
        } catch {
          setIsExistingBeneficiary(false)
        }
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Không tìm thấy tài khoản')
      setDestinationAccountInfo(null)
    } finally {
      setLoading(false)
    }
  }

  const handleBeneficiarySelect = (beneficiary: Beneficiary) => {
    setDestinationAccountNumber(beneficiary.beneficiaryAccountNumber)
    // Auto-verify the selected account
    void handleGetAccountInfo()
  }
  
  const handleSubmitTransfer = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!digitalOtpReady) {
      setError('Vui lòng kích hoạt Digital OTP trước khi chuyển tiền')
      setShowDigitalOtpModal(true)
      return
    }
    
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

    if (!digitalOtpReady) {
      setError('Vui lòng kích hoạt Digital OTP trước khi chuyển tiền')
      setShowDigitalOtpModal(true)
      return
    }
    
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
      setDigitalOtpPin('')
      setOtpToken('')
      setOtpCountdown(0)
      stopTokenTimer()
      setCurrentStep('otp')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Không thể khởi tạo giao dịch')
    } finally {
      setLoading(false)
    }
  }

  // Auto-generate token when PIN is complete
  useEffect(() => {
    const generateToken = async () => {
      if (currentStep !== 'otp' || !transactionData || digitalOtpPin.length !== DIGITAL_PIN_LENGTH) {
        return
      }

      const secret = getStoredOtpSecret()
      const saltBase64 = getStoredSalt()

      if (!secret || !saltBase64) {
        setError('Không tìm thấy khóa Digital OTP trên trình duyệt. Vui lòng cấu hình lại.')
        setShowDigitalOtpModal(true)
        return
      }

      setGeneratingToken(true)
      setError('')

      try {
        await hashPinWithSalt(digitalOtpPin, saltBase64)
        startTokenTimer(secret)
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Không thể tạo mã Digital OTP')
        stopTokenTimer()
        setOtpToken('')
        setOtpCountdown(0)
      } finally {
        setGeneratingToken(false)
      }
    }

    void generateToken()
  }, [digitalOtpPin, currentStep, transactionData])
  
  const handleVerifyDigitalOtp = async (event: React.FormEvent) => {
    event.preventDefault()

    if (!transactionData) return

    if (digitalOtpPin.length !== DIGITAL_PIN_LENGTH) {
      setError(`PIN Digital OTP phải gồm ${DIGITAL_PIN_LENGTH} chữ số`)
      return
    }

    const secret = getStoredOtpSecret()
    const saltBase64 = getStoredSalt()

    if (!secret || !saltBase64) {
      setError('Không tìm thấy khóa Digital OTP trên trình duyệt. Vui lòng cấu hình lại.')
      setShowDigitalOtpModal(true)
      return
    }

    setLoading(true)
    setError('')

    try {
      const pinHashCurrent = await hashPinWithSalt(digitalOtpPin, saltBase64)
      const timeSlice = getCurrentTimeSlice()
      const payload = buildDigitalOtpPayload(transactionData, timeSlice)
      const digitalOtpToken = await computeTotpToken(secret, payload)

      const confirmed = await transactionService.confirmTransfer({
        transactionId: transactionData.transactionId,
        digitalOtpToken,
        pinHashCurrent,
        timestamp: timeSlice
      })
      setDigitalOtpPin('')
      setOtpToken('')
      setOtpCountdown(0)
      stopTokenTimer()
      setTransactionData(confirmed)
      setCurrentStep('success')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Không thể xác thực Digital OTP')
    } finally {
      setLoading(false)
    }
  }
  
  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
  }

  const handleSaveBeneficiary = async (nickname: string, note: string) => {
    if (!customerId || !destinationAccountInfo) return

    setSaveBeneficiaryLoading(true)
    try {
      await beneficiaryService.createBeneficiary(customerId, {
        beneficiaryAccountNumber: destinationAccountInfo.accountNumber,
        beneficiaryName: destinationAccountInfo.accountHolderName,
        bankCode: destinationAccountInfo.bankCode || 'KIENLONG',
        bankName: destinationAccountInfo.bankName || 'KienLongBank',
        nickname: nickname || undefined,
        note: note || undefined
      })
      setShowSaveBeneficiaryModal(false)
      // Success notification handled by form
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Không thể lưu vào danh bạ')
    } finally {
      setSaveBeneficiaryLoading(false)
    }
  }
  
  const resetForm = () => {
    stopTokenTimer()
    setCurrentStep('input')
    setDestinationAccountNumber('')
    setDestinationAccountInfo(null)
    setAmount('')
    setDescription('')
    setDigitalOtpPin('')
    setOtpToken('')
    setOtpCountdown(0)
    setTransactionData(null)
    setError('')
    setShowSaveBeneficiaryModal(false)
    setIsExistingBeneficiary(false)
  }

  return (
    <Layout>
      <div className="max-w-4xl mx-auto p-6">
        {/* Header */}
        <div className="mb-6">
          <h1 className="text-3xl font-bold text-gray-900">Chuyển tiền nội bộ</h1>
          <p className="text-gray-600 mt-2">Chuyển tiền đến tài khoản KienlongBank khác</p>
        </div>

        {digitalOtpLoading && (
          <div className="mb-6 rounded-lg border border-blue-200 bg-blue-50 px-4 py-3 text-blue-900">
            Đang kiểm tra trạng thái Digital OTP của bạn...
          </div>
        )}

        {digitalOtpError && (
          <div className="mb-6 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-red-700">
            <div className="flex items-center justify-between gap-4">
              <span>{digitalOtpError}</span>
              <button
                type="button"
                onClick={() => { void initializeDigitalOtp() }}
                className="px-3 py-1 rounded-md border border-red-300 text-sm hover:bg-red-100"
              >
                Thử lại
              </button>
            </div>
          </div>
        )}

        {!digitalOtpLoading && digitalOtpStatus && !digitalOtpStatus.enrolled && (
          <div className="mb-6 rounded-lg border border-yellow-200 bg-yellow-50 px-4 py-3 flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
            <div>
              <p className="font-semibold text-yellow-900">Bạn chưa cấu hình Digital OTP</p>
              <p className="text-sm text-yellow-800">Vui lòng cấu hình để tiếp tục sử dụng tính năng chuyển tiền.</p>
            </div>
            <button
              type="button"
              onClick={() => setShowDigitalOtpModal(true)}
              className="px-4 py-2 rounded-lg bg-yellow-600 text-white font-semibold hover:bg-yellow-700"
            >
              Cấu hình ngay
            </button>
          </div>
        )}

        {digitalOtpStatus?.locked && (
          <div className="mb-6 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-red-700">
            Digital OTP đang bị khóa do nhập sai quá nhiều lần.
            {digitalOtpLockedUntil && (
              <span className="block text-sm mt-1">Sẽ tự mở khóa vào: {digitalOtpLockedUntil}</span>
            )}
          </div>
        )}

        <div className={disableTransfers ? 'pointer-events-none opacity-50' : ''}>
          {/* Progress Steps */}
          <div className="mb-8">
            <div className="flex items-center justify-between">
              {['Nhập thông tin', 'Xác nhận', 'Xác thực Digital OTP', 'Hoàn thành'].map((label, idx) => (
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
                      onClick={() => setShowBeneficiaryModal(true)}
                      title="Chọn từ danh bạ"
                      className="px-3 py-2 border border-gray-300 text-gray-600 rounded-lg hover:bg-gray-50 hover:border-blue-500 hover:text-blue-600 transition-colors"
                    >
                      <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                      </svg>
                    </button>
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
                    min="1"
                    step="1"
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

        {/* Step 3: Digital OTP Verification */}
        {currentStep === 'otp' && transactionData && (
          <form onSubmit={handleVerifyDigitalOtp} className="bg-white rounded-lg shadow p-6">
            <h2 className="text-xl font-semibold mb-4">Xác thực giao dịch</h2>
            
            <div className="mb-6 space-y-4">
              <p className="text-gray-600">
                Nhập PIN Digital OTP để mở khóa mã 6 số tự động thay đổi mỗi 30 giây. Mã này sẽ được gửi kèm theo giao dịch để xác nhận với hệ thống.
              </p>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  PIN Digital OTP <span className="text-red-500">*</span>
                </label>
                <input
                  type="password"
                  inputMode="numeric"
                  pattern="[0-9]*"
                  value={digitalOtpPin}
                  onChange={(e) => setDigitalOtpPin(e.target.value.replace(/\D/g, '').slice(0, DIGITAL_PIN_LENGTH))}
                  placeholder="••••••"
                  maxLength={DIGITAL_PIN_LENGTH}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-center text-2xl tracking-widest"
                  required
                  autoFocus
                />
                {generatingToken && (
                  <p className="text-sm text-blue-600 mt-2 text-center">Đang tạo mã...</p>
                )}
              </div>

              <div className="rounded-2xl border border-blue-100 bg-blue-50 px-5 py-4 flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
                <div>
                  <p className="text-sm uppercase tracking-wide text-blue-500 font-semibold">Mã Digital OTP</p>
                  <p className="text-4xl font-mono font-semibold text-blue-900 tracking-widest">
                    {otpToken || '••••••'}
                  </p>
                </div>
                <div className="text-right">
                  <p className="text-sm text-blue-800">Thời gian còn lại</p>
                  <p className="text-2xl font-bold text-blue-900">{otpToken ? `${otpCountdown}s` : '--'}</p>
                </div>
              </div>
            </div>

            <div className="flex gap-4">
              <button
                type="button"
                onClick={() => {
                  stopTokenTimer()
                  setOtpToken('')
                  setOtpCountdown(0)
                  setDigitalOtpPin('')
                  setCurrentStep('confirm')
                }}
                className="flex-1 px-6 py-3 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300"
              >
                Quay lại
              </button>
              <button
                type="submit"
                disabled={loading || digitalOtpPin.length !== DIGITAL_PIN_LENGTH || !otpToken}
                className="flex-1 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400"
              >
                {loading ? 'Đang xác thực...' : 'Ký và xác nhận'}
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

            {/* Show save to beneficiary button if not existing */}
            {!isExistingBeneficiary && destinationAccountInfo && (
              <button
                type="button"
                onClick={() => setShowSaveBeneficiaryModal(true)}
                className="mt-4 w-full px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 flex items-center justify-center gap-2"
              >
                <span className="material-icons-round text-sm">person_add</span>
                Lưu vào danh bạ thụ hưởng
              </button>
            )}
          </div>
        )}
        </div>

        {disableTransfers && !digitalOtpLoading && (
          <p className="mt-4 text-center text-sm text-gray-600">
            Vui lòng hoàn tất cấu hình Digital OTP trước khi thực hiện giao dịch chuyển tiền.
          </p>
        )}
      </div>

      {profile?.customerId && (
        <ConfigDigitalOtp
          open={showDigitalOtpModal}
          customerId={profile.customerId}
          mode={digitalOtpStatus?.enrolled ? 'update' : 'enroll'}
          disableClose={!digitalOtpStatus?.enrolled}
          onClose={() => setShowDigitalOtpModal(false)}
          onSuccess={() => { void handleDigitalOtpSuccess() }}
        />
      )}

      {/* Beneficiary Select Modal */}
      <BeneficiarySelectModal
        open={showBeneficiaryModal}
        onClose={() => setShowBeneficiaryModal(false)}
        onSelect={handleBeneficiarySelect}
      />

      {/* Save to Beneficiary Modal */}
      {showSaveBeneficiaryModal && destinationAccountInfo && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl max-w-md w-full p-6">
            <h3 className="text-xl font-semibold mb-4 text-gray-900">Lưu vào danh bạ thụ hưởng</h3>
            <SaveBeneficiaryForm
              accountInfo={destinationAccountInfo}
              onSubmit={handleSaveBeneficiary}
              onCancel={() => setShowSaveBeneficiaryModal(false)}
              loading={saveBeneficiaryLoading}
            />
          </div>
        </div>
      )}
    </Layout>
  )
}

export default TransferInternal
