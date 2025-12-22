import React, { useEffect } from 'react'
import type { Notification, NotificationType, Priority } from '../../../type/notification'

interface DetailNotificationProps {
  notification: Notification | null
  isOpen: boolean
  onClose: () => void
}

const DetailNotification: React.FC<DetailNotificationProps> = ({ notification, isOpen, onClose }) => {
  // Close modal on Esc key press
  useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        onClose()
      }
    }

    if (isOpen) {
      document.addEventListener('keydown', handleEscape)
      // Prevent body scroll when modal is open
      document.body.style.overflow = 'hidden'
    }

    return () => {
      document.removeEventListener('keydown', handleEscape)
      document.body.style.overflow = 'unset'
    }
  }, [isOpen, onClose])

  if (!isOpen || !notification) return null

  const getPriorityColor = (priority: Priority): string => {
    switch (priority) {
      case 'HIGH':
        return 'text-red-600 bg-red-50 border-red-200'
      case 'NORMAL':
        return 'text-blue-600 bg-blue-50 border-blue-200'
      case 'LOW':
        return 'text-gray-600 bg-gray-50 border-gray-200'
      default:
        return 'text-gray-600 bg-gray-50 border-gray-200'
    }
  }

  const getPriorityLabel = (priority: Priority): string => {
    switch (priority) {
      case 'HIGH':
        return 'Cao'
      case 'NORMAL':
        return 'Bình thường'
      case 'LOW':
        return 'Thấp'
      default:
        return 'Không xác định'
    }
  }

  const getTypeLabel = (type: NotificationType): string => {
    switch (type) {
      case 'BALANCE_CHANGE':
        return 'Biến động số dư'
      case 'SYSTEM':
        return 'Hệ thống'
      case 'SECURITY':
        return 'Bảo mật'
      case 'LOAN':
        return 'Khoản vay'
      default:
        return 'Khác'
    }
  }

  const getTypeIcon = (type: NotificationType): string => {
    switch (type) {
      case 'BALANCE_CHANGE':
        return 'account_balance_wallet'
      case 'SYSTEM':
        return 'info'
      case 'SECURITY':
        return 'shield'
      case 'LOAN':
        return 'payments'
      default:
        return 'notifications'
    }
  }

  const getTypeColor = (type: NotificationType): string => {
    switch (type) {
      case 'BALANCE_CHANGE':
        return 'text-green-600 bg-green-50'
      case 'SYSTEM':
        return 'text-blue-600 bg-blue-50'
      case 'SECURITY':
        return 'text-orange-600 bg-orange-50'
      case 'LOAN':
        return 'text-purple-600 bg-purple-50'
      default:
        return 'text-gray-600 bg-gray-50'
    }
  }

  const formatDateTime = (dateString: string): string => {
    const date = new Date(dateString)
    return new Intl.DateTimeFormat('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    }).format(date)
  }

  return (
    <>
      {/* Backdrop - Overlay với hiệu ứng mờ */}
      <div
        className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 transition-opacity duration-300"
        onClick={onClose}
      />

      {/* Modal Container */}
      <div className="fixed inset-0 z-50 flex items-center justify-center p-4 pointer-events-none">
        <div
          className="bg-white rounded-2xl shadow-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto pointer-events-auto transform transition-all duration-300 animate-slideUp"
          onClick={(e) => e.stopPropagation()}
        >
          {/* Header */}
          <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between rounded-t-2xl z-70">
            <div className="flex items-center gap-3">
              <div className={`w-12 h-12 rounded-full ${getTypeColor(notification.type)} flex items-center justify-center`}>
                <span className="material-icons-round text-2xl">
                  {getTypeIcon(notification.type)}
                </span>
              </div>
              <div>
                <h2 className="text-xl font-bold text-gray-900">Chi tiết thông báo</h2>
                <p className="text-sm text-gray-500">{getTypeLabel(notification.type)}</p>
              </div>
            </div>
            <button
              onClick={onClose}
              className="w-10 h-10 rounded-full hover:bg-gray-100 flex items-center justify-center transition-colors duration-200 group"
              aria-label="Đóng"
            >
              <span className="material-icons-round text-gray-400 group-hover:text-gray-600">close</span>
            </button>
          </div>

          {/* Body */}
          <div className="px-6 py-6 space-y-6">
            {/* Title Section */}
            <div className="space-y-2">
              <div className="flex items-start justify-between gap-4">
                <h3 className="text-2xl font-bold text-gray-900 leading-tight flex-1">
                  {notification.title}
                </h3>
                <span className={`px-3 py-1 rounded-full text-xs font-semibold border ${getPriorityColor(notification.priority)} whitespace-nowrap`}>
                  {getPriorityLabel(notification.priority)}
                </span>
              </div>
            </div>

            {/* Content Section */}
            <div className="bg-gray-50 rounded-xl p-5 border border-gray-200">
              <p className="text-gray-700 leading-relaxed whitespace-pre-wrap text-base">
                {notification.content}
              </p>
            </div>

            {/* Time Information */}
            <div className="flex gap-4 text-sm">
              <div className="flex items-center gap-2 text-gray-600">
                <span className="material-icons-round text-base">schedule</span>
                <span>{formatDateTime(notification.createdAt)}</span>
              </div>
              {notification.deliveredAt && (
                <>
                  <span className="text-gray-300">•</span>
                  <div className="flex items-center gap-2 text-gray-600">
                    <span className="material-icons-round text-base">done_all</span>
                    <span>{formatDateTime(notification.deliveredAt)}</span>
                  </div>
                </>
              )}
            </div>
          </div>

          {/* Footer */}
          <div className="sticky bottom-0 bg-gray-50 border-t border-gray-200 px-6 py-4 flex justify-end gap-3 rounded-b-2xl">
            <button
              onClick={onClose}
              className="px-6 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors duration-200 font-semibold shadow-sm hover:shadow-md flex items-center gap-2"
            >
              <span className="material-icons-round text-lg">check</span>
              Đóng
            </button>
          </div>
        </div>
      </div>

      <style>{`
        @keyframes slideUp {
          from {
            opacity: 0;
            transform: translateY(20px) scale(0.95);
          }
          to {
            opacity: 1;
            transform: translateY(0) scale(1);
          }
        }

        .animate-slideUp {
          animation: slideUp 0.3s ease-out forwards;
        }

        /* Custom scrollbar */
        .overflow-y-auto::-webkit-scrollbar {
          width: 8px;
        }

        .overflow-y-auto::-webkit-scrollbar-track {
          background: #f1f5f9;
          border-radius: 10px;
        }

        .overflow-y-auto::-webkit-scrollbar-thumb {
          background: #cbd5e1;
          border-radius: 10px;
        }

        .overflow-y-auto::-webkit-scrollbar-thumb:hover {
          background: #94a3b8;
        }
      `}</style>
    </>
  )
}

export default DetailNotification
