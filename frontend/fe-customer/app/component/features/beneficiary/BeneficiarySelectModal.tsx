import { useState, useEffect } from 'react'
import type { Beneficiary } from '~/type/beneficiary'
import { beneficiaryService } from '~/service/beneficiaryService'

interface BeneficiarySelectModalProps {
  open: boolean
  onClose: () => void
  onSelect: (beneficiary: Beneficiary) => void
  bankCode?: string
}

export default function BeneficiarySelectModal({ 
  open, 
  onClose, 
  onSelect, 
  bankCode 
}: BeneficiarySelectModalProps) {
  const [customerId, setCustomerId] = useState('')
  const [beneficiaries, setBeneficiaries] = useState<Beneficiary[]>([])
  const [filteredBeneficiaries, setFilteredBeneficiaries] = useState<Beneficiary[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (typeof window !== 'undefined') {
      setCustomerId(localStorage.getItem('customerId') || '')
    }
  }, [])

  useEffect(() => {
    if (open && customerId) {
      loadBeneficiaries()
      setSearchTerm('')
    }
  }, [open, customerId])

  useEffect(() => {
    filterBeneficiaries()
  }, [searchTerm, beneficiaries, bankCode])

  const loadBeneficiaries = async () => {
    if (!customerId) return
    
    try {
      setLoading(true)
      setError('')
      const data = await beneficiaryService.getAllBeneficiaries(customerId)
      setBeneficiaries(data)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Không thể tải danh sách')
    } finally {
      setLoading(false)
    }
  }

  const filterBeneficiaries = () => {
    let filtered = beneficiaries

    // Filter by bank code if provided (for interbank transfers)
    if (bankCode) {
      filtered = filtered.filter(b => b.bankCode === bankCode)
    } else {
      // For internal transfers, show only internal beneficiaries
      filtered = filtered.filter(b => !b.bankCode || b.bankCode === 'KIENLONG')
    }

    // Filter by search term
    if (searchTerm) {
      const term = searchTerm.toLowerCase()
      filtered = filtered.filter(b => 
        b.beneficiaryName.toLowerCase().includes(term) ||
        b.beneficiaryAccountNumber.includes(term) ||
        (b.nickname && b.nickname.toLowerCase().includes(term))
      )
    }

    setFilteredBeneficiaries(filtered)
  }

  const handleSelect = (beneficiary: Beneficiary) => {
    onSelect(beneficiary)
    onClose()
  }

  const getDisplayName = (beneficiary: Beneficiary) => {
    return beneficiary.nickname || beneficiary.beneficiaryName
  }

  if (!open) return null

  return (
    <div className="fixed inset-0 z-100 flex items-center justify-center bg-black/50 p-4">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-2xl max-h-[90vh] flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200">
          <h2 className="text-xl font-semibold text-gray-900">Chọn từ danh bạ</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {/* Search */}
        <div className="px-6 py-4 border-b border-gray-200">
          <div className="relative">
            <svg 
              className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400"
              fill="none" 
              stroke="currentColor" 
              viewBox="0 0 24 24"
            >
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Tìm tên, số tài khoản..."
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              autoFocus
            />
            {searchTerm && (
              <button
                onClick={() => setSearchTerm('')}
                className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            )}
          </div>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto">
          {loading && (
            <div className="p-8 text-center text-gray-500">
              <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
              <p className="mt-2">Đang tải danh bạ...</p>
            </div>
          )}

          {error && (
            <div className="p-8 text-center">
              <div className="text-red-600 mb-2">{error}</div>
              <button
                onClick={loadBeneficiaries}
                className="px-4 py-2 text-sm bg-red-100 text-red-700 rounded-lg hover:bg-red-200"
              >
                Thử lại
              </button>
            </div>
          )}

          {!loading && !error && filteredBeneficiaries.length === 0 && (
            <div className="p-8 text-center text-gray-500">
              <svg className="w-16 h-16 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
              <p className="text-lg font-medium">
                {searchTerm ? 'Không tìm thấy người thụ hưởng' : 'Chưa có danh bạ'}
              </p>
              <p className="text-sm text-gray-400 mt-1">
                {searchTerm ? 'Thử tìm kiếm với từ khóa khác' : 'Thêm người thụ hưởng sau khi chuyển tiền'}
              </p>
            </div>
          )}

          {!loading && !error && filteredBeneficiaries.length > 0 && (
            <div className="divide-y divide-gray-100">
              {filteredBeneficiaries.map((beneficiary) => (
                <button
                  key={beneficiary.beneficiaryId}
                  onClick={() => handleSelect(beneficiary)}
                  className="w-full text-left px-6 py-4 hover:bg-blue-50 transition-colors group"
                >
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1">
                        <div className="flex-shrink-0 w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 font-semibold">
                          {getDisplayName(beneficiary).charAt(0).toUpperCase()}
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="font-medium text-gray-900 truncate">
                            {getDisplayName(beneficiary)}
                          </div>
                          {beneficiary.nickname && beneficiary.beneficiaryName !== beneficiary.nickname && (
                            <div className="text-sm text-gray-500 truncate">
                              {beneficiary.beneficiaryName}
                            </div>
                          )}
                        </div>
                      </div>
                      <div className="flex items-center gap-2 ml-12">
                        <span className="text-sm font-mono text-gray-700">
                          {beneficiary.beneficiaryAccountNumber}
                        </span>
                        {beneficiary.bankCode && beneficiary.bankCode !== 'KIENLONG' && (
                          <span className="text-xs px-2 py-0.5 bg-gray-100 text-gray-600 rounded">
                            {beneficiary.bankCode}
                          </span>
                        )}
                      </div>
                    </div>
                    <svg 
                      className="flex-shrink-0 w-5 h-5 text-gray-400 group-hover:text-blue-600 transition-colors"
                      fill="none" 
                      stroke="currentColor" 
                      viewBox="0 0 24 24"
                    >
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                    </svg>
                  </div>
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="px-6 py-4 border-t border-gray-200 bg-gray-50">
          <button
            onClick={onClose}
            className="w-full px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors"
          >
            Đóng
          </button>
        </div>
      </div>
    </div>
  )
}
