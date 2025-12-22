import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router'
import { loanService } from '~/service/loanService'
import { accountService } from '~/service/accountService'
import type { AccountSummary } from '~/service/accountService'
import Layout from '~/component/layout/Layout'

export default function LoanApplication() {
  const navigate = useNavigate()
  const [step, setStep] = useState(1)
  const [loading, setLoading] = useState(false)
  const [accounts, setAccounts] = useState<AccountSummary[]>([])
  const [formData, setFormData] = useState({
    disbursementAccountNumber: '',
    requestedAmount: '',
    tenor: 12,
    purpose: '',
    repaymentMethod: 'EQUAL_INSTALLMENT' as const,
    monthlyIncome: '',
    employmentStatus: '',
    collateralInfo: ''
  })

  useEffect(() => {
    loadAccounts()
  }, [])

  const loadAccounts = async () => {
    try {
      const data = await accountService.getMyAccounts()
      setAccounts(data.filter((a) => a.accountType === 'CHECKING' && a.status === 'ACTIVE'))
    } catch (error) {
      console.error('Failed to load accounts:', error)
    }
  }

  const loanPurposes = [
    { value: 'PERSONAL', label: 'Tiêu dùng cá nhân' },
    { value: 'HOME_PURCHASE', label: 'Mua nhà' },
    { value: 'CAR_PURCHASE', label: 'Mua xe' },
    { value: 'EDUCATION', label: 'Học tập' },
    { value: 'BUSINESS', label: 'Kinh doanh' },
    { value: 'OTHER', label: 'Khác' }
  ]

  const termOptions = [6, 12, 18, 24, 36, 48, 60]

  const employmentStatuses = [
    { value: 'EMPLOYED', label: 'Đang làm việc' },
    { value: 'SELF_EMPLOYED', label: 'Tự kinh doanh' },
    { value: 'RETIRED', label: 'Đã nghỉ hưu' },
    { value: 'STUDENT', label: 'Sinh viên' },
    { value: 'UNEMPLOYED', label: 'Chưa có việc làm' }
  ]

  const handleSubmit = async () => {
    try {
      setLoading(true)
      await loanService.createLoanApplication({
        requestedAmount: Number(formData.requestedAmount),
        tenor: formData.tenor,
        purpose: formData.purpose,
        repaymentMethod: formData.repaymentMethod,
        monthlyIncome: Number(formData.monthlyIncome),
        employmentStatus: formData.employmentStatus,
        collateralInfo: formData.collateralInfo || undefined
      })

      alert('Đăng ký khoản vay thành công! Hồ sơ của bạn đang được xem xét.')
      navigate('/loan/my-loans')
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

  const isStep1Valid = () => {
    return (
      formData.requestedAmount &&
      Number(formData.requestedAmount) >= 10000000 &&
      formData.tenor > 0 &&
      formData.purpose &&
      formData.disbursementAccountNumber
    )
  }

  const isStep2Valid = () => {
    return (
      formData.monthlyIncome &&
      Number(formData.monthlyIncome) > 0 &&
      formData.employmentStatus
    )
  }

  return (
    <Layout>
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-900">Đăng ký vay vốn</h1>
          <p className="text-gray-600 mt-1">Điền đầy đủ thông tin để đăng ký khoản vay</p>
        </div>

        {/* Steps indicator */}
        <div className="flex items-center justify-center mb-8">
          <div className="flex items-center">
            <div
              className={`flex items-center justify-center w-10 h-10 rounded-full ${
                step >= 1 ? 'bg-blue-600 text-white' : 'bg-gray-300 text-gray-600'
              }`}
            >
              1
            </div>
            <div className={`w-24 h-1 ${step >= 2 ? 'bg-blue-600' : 'bg-gray-300'}`}></div>
            <div
              className={`flex items-center justify-center w-10 h-10 rounded-full ${
                step >= 2 ? 'bg-blue-600 text-white' : 'bg-gray-300 text-gray-600'
              }`}
            >
              2
            </div>
            <div className={`w-24 h-1 ${step >= 3 ? 'bg-blue-600' : 'bg-gray-300'}`}></div>
            <div
              className={`flex items-center justify-center w-10 h-10 rounded-full ${
                step >= 3 ? 'bg-blue-600 text-white' : 'bg-gray-300 text-gray-600'
              }`}
            >
              3
            </div>
          </div>
        </div>

        {/* Step 1: Loan Details */}
        {step === 1 && (
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 className="text-lg font-semibold mb-4">Thông tin khoản vay</h2>

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Tài khoản nhận tiền <span className="text-red-600">*</span>
                </label>
                <select
                  value={formData.disbursementAccountNumber}
                  onChange={(e) =>
                    setFormData({ ...formData, disbursementAccountNumber: e.target.value })
                  }
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="">Chọn tài khoản</option>
                  {accounts.map((account) => (
                    <option key={account.accountNumber} value={account.accountNumber}>
                      {account.accountNumber} - {account.accountTypeLabel}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Số tiền vay <span className="text-red-600">*</span>
                </label>
                <input
                  type="text"
                  value={formData.requestedAmount}
                  onChange={(e) => {
                    const value = e.target.value.replace(/\D/g, '')
                    setFormData({ ...formData, requestedAmount: value })
                  }}
                  placeholder="Tối thiểu 10.000.000 VND"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                {formData.requestedAmount && (
                  <p className="text-sm text-gray-600 mt-1">
                    {formatCurrency(Number(formData.requestedAmount))}
                  </p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Kỳ hạn vay <span className="text-red-600">*</span>
                </label>
                <div className="grid grid-cols-4 gap-3">
                  {termOptions.map((term) => (
                    <button
                      key={term}
                      onClick={() => setFormData({ ...formData, tenor: term })}
                      className={`px-4 py-2 border rounded-lg font-medium transition-colors ${
                        formData.tenor === term
                          ? 'bg-blue-600 text-white border-blue-600'
                          : 'border-gray-300 text-gray-700 hover:border-blue-500'
                      }`}
                    >
                      {term} tháng
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Mục đích vay <span className="text-red-600">*</span>
                </label>
                <select
                  value={formData.purpose}
                  onChange={(e) => setFormData({ ...formData, purpose: e.target.value })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="">Chọn mục đích</option>
                  {loanPurposes.map((purpose) => (
                    <option key={purpose.value} value={purpose.value}>
                      {purpose.label}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Mô tả tài sản đảm bảo (nếu có)
                </label>
                <textarea
                  value={formData.collateralInfo}
                  onChange={(e) =>
                    setFormData({ ...formData, collateralInfo: e.target.value })
                  }
                  rows={3}
                  placeholder="Ví dụ: Sổ đỏ nhà số 123, quận ABC..."
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
            </div>

            <div className="mt-6">
              <button
                onClick={() => setStep(2)}
                disabled={!isStep1Valid()}
                className="w-full bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-medium disabled:bg-gray-300 disabled:cursor-not-allowed"
              >
                Tiếp tục
              </button>
            </div>
          </div>
        )}

        {/* Step 2: Income & Employment */}
        {step === 2 && (
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 className="text-lg font-semibold mb-4">Thông tin thu nhập</h2>

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Thu nhập hàng tháng <span className="text-red-600">*</span>
                </label>
                <input
                  type="text"
                  value={formData.monthlyIncome}
                  onChange={(e) => {
                    const value = e.target.value.replace(/\D/g, '')
                    setFormData({ ...formData, monthlyIncome: value })
                  }}
                  placeholder="Nhập thu nhập hàng tháng"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                {formData.monthlyIncome && (
                  <p className="text-sm text-gray-600 mt-1">
                    {formatCurrency(Number(formData.monthlyIncome))}
                  </p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Tình trạng công việc <span className="text-red-600">*</span>
                </label>
                <select
                  value={formData.employmentStatus}
                  onChange={(e) => setFormData({ ...formData, employmentStatus: e.target.value })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="">Chọn tình trạng</option>
                  {employmentStatuses.map((status) => (
                    <option key={status.value} value={status.value}>
                      {status.label}
                    </option>
                  ))}
                </select>
              </div>

              <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
                <p className="text-sm text-yellow-800">
                  <strong>Lưu ý:</strong> Thông tin thu nhập của bạn sẽ được xác minh trong quá
                  trình xét duyệt. Vui lòng cung cấp thông tin chính xác.
                </p>
              </div>
            </div>

            <div className="mt-6 flex gap-3">
              <button
                onClick={() => setStep(1)}
                className="flex-1 px-6 py-3 border border-gray-300 text-gray-700 rounded-lg font-medium hover:bg-gray-50"
              >
                Quay lại
              </button>
              <button
                onClick={() => setStep(3)}
                disabled={!isStep2Valid()}
                className="flex-1 bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-medium disabled:bg-gray-300 disabled:cursor-not-allowed"
              >
                Tiếp tục
              </button>
            </div>
          </div>
        )}

        {/* Step 3: Review & Submit */}
        {step === 3 && (
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 className="text-lg font-semibold mb-4">Xác nhận thông tin</h2>

            <div className="space-y-6">
              <div>
                <h3 className="font-medium text-gray-900 mb-3">Thông tin khoản vay</h3>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm text-gray-600">Số tiền vay</p>
                    <p className="font-bold text-blue-600">
                      {formatCurrency(Number(formData.requestedAmount))}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Kỳ hạn</p>
                    <p className="font-medium">{formData.tenor} tháng</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Mục đích</p>
                    <p className="font-medium">
                      {loanPurposes.find((p) => p.value === formData.purpose)?.label}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Tài khoản nhận</p>
                    <p className="font-mono font-medium">{formData.disbursementAccountNumber}</p>
                  </div>
                </div>
                {formData.collateralInfo && (
                  <div className="mt-3">
                    <p className="text-sm text-gray-600">Tài sản đảm bảo</p>
                    <p className="font-medium">{formData.collateralInfo}</p>
                  </div>
                )}
              </div>

              <div>
                <h3 className="font-medium text-gray-900 mb-3">Thông tin thu nhập</h3>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm text-gray-600">Thu nhập hàng tháng</p>
                    <p className="font-bold text-green-600">
                      {formatCurrency(Number(formData.monthlyIncome))}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Tình trạng công việc</p>
                    <p className="font-medium">
                      {employmentStatuses.find((s) => s.value === formData.employmentStatus)?.label}
                    </p>
                  </div>
                </div>
              </div>

              <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                <p className="text-sm text-blue-800">
                  <strong>Thông báo:</strong> Sau khi gửi đăng ký, hồ sơ của bạn sẽ được xem xét
                  trong vòng 1-3 ngày làm việc. Chúng tôi sẽ liên hệ với bạn qua email hoặc số điện
                  thoại đã đăng ký.
                </p>
              </div>
            </div>

            <div className="mt-6 flex gap-3">
              <button
                onClick={() => setStep(2)}
                disabled={loading}
                className="flex-1 px-6 py-3 border border-gray-300 text-gray-700 rounded-lg font-medium hover:bg-gray-50 disabled:opacity-50"
              >
                Quay lại
              </button>
              <button
                onClick={handleSubmit}
                disabled={loading}
                className="flex-1 bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-medium disabled:bg-gray-300 disabled:cursor-not-allowed"
              >
                {loading ? 'Đang xử lý...' : 'Xác nhận đăng ký'}
              </button>
            </div>
          </div>
        )}
      </div>
    </Layout>
  )
}
