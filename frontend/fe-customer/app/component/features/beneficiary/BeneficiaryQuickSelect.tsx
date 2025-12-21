import { useState, useEffect } from 'react'
import type { Beneficiary } from '~/type/beneficiary'
import { beneficiaryService } from '~/service/beneficiaryService'

interface BeneficiaryQuickSelectProps {
  onSelect: (beneficiary: Beneficiary) => void
  bankCode?: string
  className?: string
}

export default function BeneficiaryQuickSelect({ 
  onSelect, 
  bankCode,
  className = '' 
}: BeneficiaryQuickSelectProps) {
  const customerId = localStorage.getItem('customerId') || ''
  const [beneficiaries, setBeneficiaries] = useState<Beneficiary[]>([])
  const [filteredBeneficiaries, setFilteredBeneficiaries] = useState<Beneficiary[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showDropdown, setShowDropdown] = useState(false)

  useEffect(() => {
    loadBeneficiaries()
  }, [customerId])

  useEffect(() => {
    filterBeneficiaries()
  }, [searchTerm, beneficiaries, bankCode])

  const loadBeneficiaries = async () => {
    if (!customerId) return
    
    try {
      setLoading(true)
      setError('')
      const data = await beneficiaryService.getMostUsedBeneficiaries(customerId, 10)
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
    setSearchTerm('')
    setShowDropdown(false)
  }

  const getDisplayName = (beneficiary: Beneficiary) => {
    return beneficiary.nickname || beneficiary.beneficiaryName
  }

  return (
    <div className={`relative ${className}`}>
      <div className="mb-2">
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Chọn từ danh bạ
        </label>
        <div className="relative">
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => {
              setSearchTerm(e.target.value)
              setShowDropdown(true)
            }}
            onFocus={() => setShowDropdown(true)}
            placeholder="Tìm người thụ hưởng..."
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          {searchTerm && (
            <button
              onClick={() => {
                setSearchTerm('')
                setShowDropdown(false)
              }}
              className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
            >
              ✕
            </button>
          )}
        </div>
      </div>

      {showDropdown && (
        <>
          <div 
            className="fixed inset-0 z-10" 
            onClick={() => setShowDropdown(false)}
          />
          <div className="absolute z-20 w-full mt-1 bg-white border border-gray-300 rounded-lg shadow-lg max-h-80 overflow-y-auto">
            {loading && (
              <div className="p-4 text-center text-gray-500">
                Đang tải...
              </div>
            )}

            {error && (
              <div className="p-4 text-center text-red-600 text-sm">
                {error}
              </div>
            )}

            {!loading && !error && filteredBeneficiaries.length === 0 && (
              <div className="p-4 text-center text-gray-500">
                {searchTerm ? 'Không tìm thấy người thụ hưởng' : 'Chưa có người thụ hưởng'}
              </div>
            )}

            {!loading && !error && filteredBeneficiaries.map((beneficiary) => (
              <button
                key={beneficiary.beneficiaryId}
                onClick={() => handleSelect(beneficiary)}
                className="w-full text-left px-4 py-3 hover:bg-gray-50 border-b border-gray-100 last:border-b-0 transition-colors"
              >
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <div className="font-medium text-gray-900">
                      {getDisplayName(beneficiary)}
                    </div>
                    <div className="text-sm text-gray-500">
                      {beneficiary.beneficiaryAccountNumber}
                      {beneficiary.bankCode && beneficiary.bankCode !== 'KIENLONG' && (
                        <span className="ml-2">• {beneficiary.bankName}</span>
                      )}
                    </div>
                    {beneficiary.transferCount > 0 && (
                      <div className="text-xs text-gray-400 mt-1">
                        Đã chuyển {beneficiary.transferCount} lần
                      </div>
                    )}
                  </div>
                  <div className="ml-3 text-blue-600">
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                    </svg>
                  </div>
                </div>
              </button>
            ))}
          </div>
        </>
      )}
    </div>
  )
}
