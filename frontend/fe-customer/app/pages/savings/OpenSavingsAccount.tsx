import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router'
import { savingsService } from '~/service/savingsService'
import { accountService } from '~/service/accountService'
import type { SavingsProduct } from '~/type/savings'
import type { AccountSummary } from '~/service/accountService'
import Layout from '~/component/layout/Layout'

export default function OpenSavingsAccount() {
  const navigate = useNavigate()
  const [step, setStep] = useState(1)
  const [loading, setLoading] = useState(false)
  const [products, setProducts] = useState<SavingsProduct[]>([])
  const [accounts, setAccounts] = useState<AccountSummary[]>([])
  const [selectedProduct, setSelectedProduct] = useState<SavingsProduct | null>(null)
  const [formData, setFormData] = useState({
    sourceAccountNumber: '',
    depositAmount: '',
    termMonths: 0,
    autoRenew: false
  })
  const [calculatedInterest, setCalculatedInterest] = useState({
    estimatedInterest: 0,
    maturityAmount: 0
  })

  useEffect(() => {
    loadInitialData()
  }, [])

  useEffect(() => {
    if (selectedProduct && formData.depositAmount) {
      calculateInterest()
    }
  }, [selectedProduct, formData.depositAmount])

  const loadInitialData = async () => {
    try {
      const [productsData, accountsData] = await Promise.all([
        savingsService.getSavingsProducts(),
        accountService.getMyAccounts()
      ])
      setProducts(productsData)
      setAccounts(accountsData.filter((a) => a.accountType === 'CHECKING' && a.status === 'ACTIVE'))
    } catch (error) {
      console.error('Failed to load data:', error)
    }
  }

  const calculateInterest = async () => {
    if (!selectedProduct) return
    try {
      const result = await savingsService.calculateInterest(
        Number(formData.depositAmount),
        `${selectedProduct.termMonths}M`  // Convert to tenor format: "6M", "12M", etc.
      )
      setCalculatedInterest({
        estimatedInterest: result.projectedInterest,
        maturityAmount: result.maturityAmount
      })
    } catch (error) {
      console.error('Failed to calculate interest:', error)
    }
  }

  const handleProductSelect = (product: SavingsProduct) => {
    setSelectedProduct(product)
    setFormData((prev) => ({ ...prev, termMonths: product.termMonths }))
    setStep(2)
  }

  const handleSubmit = async () => {
    if (!selectedProduct) return

    try {
      setLoading(true)
      await savingsService.createSavingsAccount({
        sourceAccountNumber: formData.sourceAccountNumber,
        principalAmount: Number(formData.depositAmount),
        tenor: `${selectedProduct.termMonths}M`,  // Convert to tenor format
        interestPaymentMethod: 'END_OF_TERM',
        autoRenewType: formData.autoRenew ? 'PRINCIPAL_AND_INTEREST' : 'NONE',
        description: `Mở sổ ${selectedProduct.productName}`
      })

      alert('Mở sổ tiết kiệm thành công!')
      navigate('/saving/books')
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

  return (
    <Layout>
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-900">Mở sổ tiết kiệm online</h1>
          <p className="text-gray-600 mt-1">Gửi tiết kiệm nhanh chóng, lãi suất hấp dẫn</p>
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

        {/* Step 1: Choose product */}
        {step === 1 && (
          <div>
            <h2 className="text-lg font-semibold mb-4">Chọn sản phẩm tiết kiệm</h2>
            <div className="grid gap-4 md:grid-cols-2">
              {products.map((product) => (
                <button
                  key={product.productCode}
                  onClick={() => handleProductSelect(product)}
                  className="text-left bg-white rounded-lg shadow-sm border border-gray-200 p-6 hover:border-blue-500 hover:shadow-md transition-all"
                >
                  <h3 className="font-semibold text-lg text-gray-900 mb-2">
                    {product.productName}
                  </h3>
                  <p className="text-sm text-gray-600 mb-4">{product.description}</p>
                  <div className="space-y-2">
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-600">Lãi suất:</span>
                      <span className="font-bold text-green-600">{product.interestRate}%/năm</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-600">Kỳ hạn:</span>
                      <span className="font-semibold">{product.termMonths} tháng</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-600">Số tiền tối thiểu:</span>
                      <span className="font-semibold">{formatCurrency(product.minAmount)}</span>
                    </div>
                  </div>
                </button>
              ))}
            </div>
          </div>
        )}

        {/* Step 2: Enter amount */}
        {step === 2 && selectedProduct && (
          <div>
            <button
              onClick={() => setStep(1)}
              className="mb-4 text-blue-600 hover:text-blue-700 flex items-center"
            >
              <span className="material-icons-round text-sm mr-1">arrow_back</span>
              Chọn sản phẩm khác
            </button>

            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
              <h2 className="text-lg font-semibold mb-4">Thông tin gửi tiết kiệm</h2>

              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Tài khoản nguồn
                  </label>
                  <select
                    value={formData.sourceAccountNumber}
                    onChange={(e) =>
                      setFormData({ ...formData, sourceAccountNumber: e.target.value })
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
                    Số tiền gửi
                  </label>
                  <input
                    type="text"
                    value={formData.depositAmount}
                    onChange={(e) => {
                      const value = e.target.value.replace(/\D/g, '')
                      setFormData({ ...formData, depositAmount: value })
                    }}
                    placeholder={`Tối thiểu ${formatCurrency(selectedProduct.minAmount)}`}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                  {formData.depositAmount && (
                    <p className="text-sm text-gray-600 mt-1">
                      {formatCurrency(Number(formData.depositAmount))}
                    </p>
                  )}
                </div>

                <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                  <h3 className="font-semibold text-gray-900 mb-2">Dự kiến khi đáo hạn</h3>
                  <div className="space-y-2">
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-600">Tiền gốc:</span>
                      <span className="font-semibold">
                        {formatCurrency(Number(formData.depositAmount || 0))}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-600">Lãi dự kiến:</span>
                      <span className="font-semibold text-green-600">
                        {formatCurrency(calculatedInterest.estimatedInterest)}
                      </span>
                    </div>
                    <div className="flex justify-between border-t border-blue-300 pt-2">
                      <span className="font-medium text-gray-900">Tổng nhận:</span>
                      <span className="text-lg font-bold text-blue-600">
                        {formatCurrency(calculatedInterest.maturityAmount)}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="flex items-center">
                  <input
                    type="checkbox"
                    id="autoRenew"
                    checked={formData.autoRenew}
                    onChange={(e) => setFormData({ ...formData, autoRenew: e.target.checked })}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <label htmlFor="autoRenew" className="ml-2 text-sm text-gray-700">
                    Tự động gia hạn khi đáo hạn
                  </label>
                </div>
              </div>

              <div className="mt-6 flex gap-3">
                <button
                  onClick={() => setStep(1)}
                  className="flex-1 px-6 py-2.5 border border-gray-300 text-gray-700 rounded-lg font-medium hover:bg-gray-50"
                >
                  Quay lại
                </button>
                <button
                  onClick={() => setStep(3)}
                  disabled={
                    !formData.sourceAccountNumber ||
                    !formData.depositAmount ||
                    Number(formData.depositAmount) < selectedProduct.minAmount
                  }
                  className="flex-1 bg-blue-600 hover:bg-blue-700 text-white px-6 py-2.5 rounded-lg font-medium disabled:bg-gray-300 disabled:cursor-not-allowed"
                >
                  Tiếp tục
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Step 3: Confirm */}
        {step === 3 && selectedProduct && (
          <div>
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
              <h2 className="text-lg font-semibold mb-4">Xác nhận thông tin</h2>

              <div className="space-y-4 mb-6">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm text-gray-600">Sản phẩm</p>
                    <p className="font-medium">{selectedProduct.productName}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Kỳ hạn</p>
                    <p className="font-medium">{selectedProduct.termMonths} tháng</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Lãi suất</p>
                    <p className="font-medium text-green-600">{selectedProduct.interestRate}%/năm</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Tài khoản nguồn</p>
                    <p className="font-medium font-mono">{formData.sourceAccountNumber}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Số tiền gửi</p>
                    <p className="font-bold text-blue-600">
                      {formatCurrency(Number(formData.depositAmount))}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Tổng nhận khi đáo hạn</p>
                    <p className="font-bold text-blue-600">
                      {formatCurrency(calculatedInterest.maturityAmount)}
                    </p>
                  </div>
                </div>

                {formData.autoRenew && (
                  <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-3">
                    <p className="text-sm text-yellow-800">
                      ✓ Sổ sẽ tự động gia hạn khi đáo hạn
                    </p>
                  </div>
                )}
              </div>

              <div className="flex gap-3">
                <button
                  onClick={() => setStep(2)}
                  className="flex-1 px-6 py-2.5 border border-gray-300 text-gray-700 rounded-lg font-medium hover:bg-gray-50"
                >
                  Quay lại
                </button>
                <button
                  onClick={handleSubmit}
                  disabled={loading}
                  className="flex-1 bg-blue-600 hover:bg-blue-700 text-white px-6 py-2.5 rounded-lg font-medium disabled:bg-gray-300 disabled:cursor-not-allowed"
                >
                  {loading ? 'Đang xử lý...' : 'Xác nhận mở sổ'}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </Layout>
  )
}
