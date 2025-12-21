import React, { useState } from 'react'
import type { AccountInfo } from '~/service/transactionService'

interface SaveBeneficiaryFormProps {
  accountInfo: AccountInfo
  onSubmit: (nickname: string, note: string) => void
  onCancel: () => void
  loading?: boolean
}

const SaveBeneficiaryForm: React.FC<SaveBeneficiaryFormProps> = ({
  accountInfo,
  onSubmit,
  onCancel,
  loading = false
}) => {
  const [nickname, setNickname] = useState('')
  const [note, setNote] = useState('')

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    onSubmit(nickname.trim(), note.trim())
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {/* Display pre-filled info (read-only) */}
      <div className="p-4 bg-blue-50 border border-blue-200 rounded-lg space-y-2">
        <div className="flex justify-between">
          <span className="text-sm text-gray-600">Tên:</span>
          <span className="font-medium text-gray-900">{accountInfo.accountHolderName}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-sm text-gray-600">Số tài khoản:</span>
          <span className="font-medium text-gray-900">{accountInfo.accountNumber}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-sm text-gray-600">Ngân hàng:</span>
          <span className="font-medium text-gray-900">{accountInfo.bankName || 'KienLongBank'}</span>
        </div>
      </div>

      {/* Nickname input (optional) */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Biệt danh (tùy chọn)
        </label>
        <input
          type="text"
          value={nickname}
          onChange={(e) => setNickname(e.target.value)}
          placeholder="VD: Anh Tú, Chị Hoa, ..."
          maxLength={50}
          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
        />
        <p className="text-xs text-gray-500 mt-1">
          Đặt biệt danh để dễ nhớ và tìm kiếm sau này
        </p>
      </div>

      {/* Note input (optional) */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Ghi chú (tùy chọn)
        </label>
        <textarea
          value={note}
          onChange={(e) => setNote(e.target.value)}
          placeholder="VD: Khách hàng thân thiết, Nhà cung cấp, ..."
          maxLength={200}
          rows={3}
          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 resize-none"
        />
      </div>

      {/* Actions */}
      <div className="flex gap-3 pt-4">
        <button
          type="button"
          onClick={onCancel}
          disabled={loading}
          className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          Bỏ qua
        </button>
        <button
          type="submit"
          disabled={loading}
          className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {loading ? 'Đang lưu...' : 'Lưu vào danh bạ'}
        </button>
      </div>
    </form>
  )
}

export default SaveBeneficiaryForm
