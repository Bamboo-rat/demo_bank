import React, { useEffect, useState } from 'react'
import Layout from '~/component/layout/Layout'
import { notificationService } from '~/service/notificationService'
import DetailNotification from '~/component/features/notification/DetailNotification'
import type { Notification as NotificationModel } from '../../type/notification'
import { NotificationType as NotificationTypeEnum } from '../../type/notification'

type FilterType = NotificationTypeEnum

const Notification = () => {
  const [notifications, setNotifications] = useState<NotificationModel[]>([])
  const [loading, setLoading] = useState(true)
  const [selectedType, setSelectedType] = useState<FilterType>(NotificationTypeEnum.BALANCE_CHANGE)
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [selectedNotification, setSelectedNotification] = useState<NotificationModel | null>(null)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const pageSize = 10

  const filterButtons: Array<{ type: FilterType; label: string; icon: string; color: string }> = [
    { type: NotificationTypeEnum.BALANCE_CHANGE, label: 'Biến động số dư', icon: 'account_balance_wallet', color: 'green' },
    { type: NotificationTypeEnum.SYSTEM, label: 'Hệ thống', icon: 'info', color: 'blue' },
    { type: NotificationTypeEnum.SECURITY, label: 'Bảo mật', icon: 'shield', color: 'orange' },
    { type: NotificationTypeEnum.LOAN, label: 'Khoản vay', icon: 'payments', color: 'purple' }
  ]

  useEffect(() => {
    void fetchNotifications()
  }, [selectedType, currentPage])

  const fetchNotifications = async () => {
    setLoading(true)
    try {
      const response = await notificationService.getNotificationsByType(selectedType, currentPage, pageSize)
      
      setNotifications(response.notifications)
      setTotalPages(response.totalPages)
    } catch (error) {
      console.error('Failed to fetch notifications:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleNotificationClick = (notification: NotificationModel) => {
    setSelectedNotification(notification)
    setIsModalOpen(true)
    if (!notification.isRead) {
      void handleMarkAsRead(notification.id)
    }
  }

  const handleMarkAsRead = async (notificationId: string) => {
    try {
      await notificationService.markAsRead(notificationId)
      setNotifications(prevNotifications =>
        prevNotifications.map(notif =>
          notif.id === notificationId ? { ...notif, isRead: true, readAt: new Date().toISOString() } : notif
        )
      )
    } catch (error) {
      console.error('Failed to mark notification as read:', error)
    }
  }

  const formatDateTime = (dateString: string): string => {
    const date = new Date(dateString)
    return new Intl.DateTimeFormat('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date)
  }

  const getPriorityBadge = (priority: string) => {
    switch (priority) {
      case 'HIGH':
        return 'bg-red-100 text-red-700 border-red-200'
      case 'NORMAL':
        return 'bg-blue-100 text-blue-700 border-blue-200'
      case 'LOW':
        return 'bg-gray-100 text-gray-700 border-gray-200'
      default:
        return 'bg-gray-100 text-gray-700 border-gray-200'
    }
  }

  const getNotificationIcon = (type: string) => {
    switch (type) {
      case 'BALANCE_CHANGE':
        return 'account_balance_wallet'
      case 'SECURITY':
        return 'shield'
      case 'SYSTEM':
        return 'info'
      case 'LOAN':
        return 'payments'
      default:
        return 'notifications'
    }
  }

  const getTypeColor = (type: string) => {
    switch (type) {
      case 'BALANCE_CHANGE':
        return 'bg-green-100 text-green-700'
      case 'SECURITY':
        return 'bg-orange-100 text-orange-700'
      case 'SYSTEM':
        return 'bg-blue-100 text-blue-700'
      case 'LOAN':
        return 'bg-purple-100 text-purple-700'
      default:
        return 'bg-gray-100 text-gray-700'
    }
  }

  return (
    <Layout>
      <div className="container mx-auto pl-1">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-blue-900 mb-2">Thông báo</h1>
        </div>

        {/* Filter Buttons */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-4 mb-6">
          <div className="flex flex-wrap gap-3">
            {filterButtons.map((button) => (
              <button
                key={button.type}
                onClick={() => {
                  setSelectedType(button.type)
                  setCurrentPage(0)
                }}
                className={`flex items-center gap-2 px-4 py-2.5 rounded-xl font-medium transition-all duration-200 ${
                  selectedType === button.type
                    ? `bg-${button.color}-600 text-white shadow-md`
                    : `bg-${button.color}-50 text-${button.color}-700 hover:bg-${button.color}-100`
                }`}
                style={{
                  backgroundColor: selectedType === button.type 
                    ? button.color === 'green' ? '#16a34a' 
                    : button.color === 'orange' ? '#ea580c'
                    : button.color === 'purple' ? '#9333ea'
                    : '#2563eb'
                    : undefined
                }}
              >
                <span className="material-icons-round text-xl">{button.icon}</span>
                <span>{button.label}</span>
              </button>
            ))}
          </div>
        </div>

        {/* Notifications List */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-200 overflow-hidden">
          {loading ? (
            <div className="flex items-center justify-center py-16">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
            </div>
          ) : notifications.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-16 px-4">
              <span className="material-icons-round text-gray-300 text-7xl mb-4">
                notifications_none
              </span>
              <p className="text-gray-500 text-lg font-medium">Không có thông báo nào</p>
              <p className="text-gray-400 text-sm mt-1">Các thông báo của bạn sẽ hiển thị tại đây</p>
            </div>
          ) : (
            <div className="divide-y divide-gray-100">
              {notifications.map((notification) => (
                <div
                  key={notification.id}
                  className={`px-6 py-5 hover:bg-gray-50 transition-colors cursor-pointer ${
                    !notification.isRead ? 'bg-blue-50/30' : ''
                  }`}
                  onClick={() => handleNotificationClick(notification)}
                >
                  <div className="flex gap-4">
                    {/* Icon */}
                    <div className={`shrink-0 w-14 h-14 rounded-full ${
                      getTypeColor(notification.type)
                    } flex items-center justify-center`}>
                      <span className="material-icons-round text-2xl">
                        {getNotificationIcon(notification.type)}
                      </span>
                    </div>

                    {/* Content */}
                    <div className="flex-1 min-w-0">
                      <div className="flex items-start justify-between gap-3 mb-2">
                        <h3 className={`text-lg font-semibold ${
                          !notification.isRead ? 'text-blue-900' : 'text-gray-800'
                        } line-clamp-1`}>
                          {notification.title}
                        </h3>
                        <div className="flex items-center gap-2 shrink-0">
                          <span className={`px-3 py-1 rounded-full text-xs font-semibold border ${
                            getPriorityBadge(notification.priority)
                          }`}>
                            {notification.priority === 'HIGH' ? 'Cao' : notification.priority === 'NORMAL' ? 'Bình thường' : 'Thấp'}
                          </span>
                          {!notification.isRead && (
                            <div className="w-3 h-3 rounded-full bg-blue-600"></div>
                          )}
                        </div>
                      </div>
                      <p className="text-gray-600 line-clamp-2 mb-3">
                        {notification.content}
                      </p>
                      <div className="flex items-center gap-4 text-sm text-gray-500">
                        <div className="flex items-center gap-1">
                          <span className="material-icons-round text-base">schedule</span>
                          <span>{formatDateTime(notification.createdAt)}</span>
                        </div>
                        {notification.isRead && notification.readAt && (
                          <div className="flex items-center gap-1">
                            <span className="material-icons-round text-base">mark_email_read</span>
                            <span>Đã đọc</span>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* Pagination */}
          {!loading && totalPages > 1 && (
            <div className="px-6 py-4 border-t border-gray-200 flex items-center justify-between">
              <p className="text-sm text-gray-600">
                Trang {currentPage + 1} / {totalPages}
              </p>
              <div className="flex gap-2">
                <button
                  onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
                  disabled={currentPage === 0}
                  className="px-4 py-2 rounded-lg bg-blue-600 text-white disabled:bg-gray-300 disabled:cursor-not-allowed hover:bg-blue-700 transition-colors flex items-center gap-1"
                >
                  <span className="material-icons-round text-sm">chevron_left</span>
                  Trước
                </button>
                <button
                  onClick={() => setCurrentPage(prev => Math.min(totalPages - 1, prev + 1))}
                  disabled={currentPage >= totalPages - 1}
                  className="px-4 py-2 rounded-lg bg-blue-600 text-white disabled:bg-gray-300 disabled:cursor-not-allowed hover:bg-blue-700 transition-colors flex items-center gap-1"
                >
                  Sau
                  <span className="material-icons-round text-sm">chevron_right</span>
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Detail Modal */}
        <DetailNotification
          notification={selectedNotification}
          isOpen={isModalOpen}
          onClose={() => setIsModalOpen(false)}
        />
      </div>
    </Layout>
  )
}

export default Notification
