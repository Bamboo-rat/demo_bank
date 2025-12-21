import { useState } from 'react'
import type { Beneficiary } from '~/type/beneficiary'

interface BeneficiaryListProps {
  beneficiaries: Beneficiary[]
  onEdit?: (beneficiary: Beneficiary) => void
  onDelete?: (beneficiary: Beneficiary) => void
  onSelect?: (beneficiary: Beneficiary) => void
  loading?: boolean
  selectable?: boolean
}

export default function BeneficiaryList({
  beneficiaries,
  onEdit,
  onDelete,
  onSelect,
  loading = false,
  selectable = false
}: BeneficiaryListProps) {
  const [searchTerm, setSearchTerm] = useState('')
  const [filterType, setFilterType] = useState<'all' | 'internal' | 'interbank'>('all')

  const getDisplayName = (beneficiary: Beneficiary) => {
    return beneficiary.nickname || beneficiary.beneficiaryName
  }

  const isInternal = (beneficiary: Beneficiary) => {
    return !beneficiary.bankCode || beneficiary.bankCode === 'KIENLONG'
  }

  const filteredBeneficiaries = beneficiaries.filter(b => {
    // Filter by type
    if (filterType === 'internal' && !isInternal(b)) return false
    if (filterType === 'interbank' && isInternal(b)) return false

    // Filter by search term
    if (searchTerm) {
      const term = searchTerm.toLowerCase()
      return (
        b.beneficiaryName.toLowerCase().includes(term) ||
        b.beneficiaryAccountNumber.includes(term) ||
        (b.nickname && b.nickname.toLowerCase().includes(term)) ||
        (b.bankName && b.bankName.toLowerCase().includes(term))
      )
    }

    return true
  })

  if (loading) {
    return (
      <div className="flex justify-center items-center py-12">
        <div className="text-gray-500">Đang tải danh sách...</div>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      {/* Search and Filter */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="flex-1">
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Tìm theo tên, số tài khoản, ngân hàng..."
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div className="flex gap-2">
          <button
            onClick={() => setFilterType('all')}
            className={`px-4 py-2 rounded-lg font-medium transition-colors ${
              filterType === 'all'
                ? 'bg-blue-600 text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            }`}
          >
            Tất cả
          </button>
          <button
            onClick={() => setFilterType('internal')}
            className={`px-4 py-2 rounded-lg font-medium transition-colors ${
              filterType === 'internal'
                ? 'bg-blue-600 text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            }`}
          >
            Nội bộ
          </button>
          <button
            onClick={() => setFilterType('interbank')}
            className={`px-4 py-2 rounded-lg font-medium transition-colors ${
              filterType === 'interbank'
                ? 'bg-blue-600 text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            }`}
          >
            Liên ngân hàng
          </button>
        </div>
      </div>

      {/* Beneficiary List */}
      {filteredBeneficiaries.length === 0 ? (
        <div className="text-center py-12 bg-gray-50 rounded-lg">
          <div className="text-gray-500">
            {searchTerm ? 'Không tìm thấy người thụ hưởng phù hợp' : 'Chưa có người thụ hưởng nào'}
          </div>
        </div>
      ) : (
        <div className="grid gap-3">
          {filteredBeneficiaries.map((beneficiary) => (
            <div
              key={beneficiary.beneficiaryId}
              className="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow"
            >
              <div className="flex items-start justify-between">
                <div 
                  className={`flex-1 ${selectable && onSelect ? 'cursor-pointer' : ''}`}
                  onClick={() => selectable && onSelect && onSelect(beneficiary)}
                >
                  <div className="flex items-center gap-2 mb-2">
                    <h3 className="font-semibold text-gray-900">
                      {getDisplayName(beneficiary)}
                    </h3>
                    {beneficiary.isVerified && (
                      <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-green-100 text-green-800">
                        ✓ Đã xác thực
                      </span>
                    )}
                    {!isInternal(beneficiary) && (
                      <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800">
                        Liên NH
                      </span>
                    )}
                  </div>

                  <div className="space-y-1 text-sm text-gray-600">
                    <div className="flex items-center gap-2">
                      <span className="font-medium">STK:</span>
                      <span className="font-mono">{beneficiary.beneficiaryAccountNumber}</span>
                    </div>
                    
                    {beneficiary.nickname && beneficiary.nickname !== beneficiary.beneficiaryName && (
                      <div className="flex items-center gap-2">
                        <span className="font-medium">Tên thật:</span>
                        <span>{beneficiary.beneficiaryName}</span>
                      </div>
                    )}

                    {!isInternal(beneficiary) && beneficiary.bankName && (
                      <div className="flex items-center gap-2">
                        <span className="font-medium">Ngân hàng:</span>
                        <span>{beneficiary.bankName}</span>
                      </div>
                    )}

                    {beneficiary.note && (
                      <div className="flex items-center gap-2">
                        <span className="font-medium">Ghi chú:</span>
                        <span className="text-gray-500 italic">{beneficiary.note}</span>
                      </div>
                    )}

                    {beneficiary.transferCount > 0 && (
                      <div className="flex items-center gap-2 text-gray-500">
                        <span>Đã chuyển {beneficiary.transferCount} lần</span>
                        {beneficiary.lastTransferDate && (
                          <span>• Lần cuối: {new Date(beneficiary.lastTransferDate).toLocaleDateString('vi-VN')}</span>
                        )}
                      </div>
                    )}
                  </div>
                </div>

                {/* Actions */}
                {(onEdit || onDelete) && (
                  <div className="flex gap-2 ml-4">
                    {onEdit && (
                      <button
                        onClick={() => onEdit(beneficiary)}
                        className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                        title="Chỉnh sửa"
                      >
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                        </svg>
                      </button>
                    )}
                    {onDelete && (
                      <button
                        onClick={() => onDelete(beneficiary)}
                        className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                        title="Xóa"
                      >
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                        </svg>
                      </button>
                    )}
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
