import React, { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { notificationService } from '~/service/notificationService'
import type { Notification } from '../../../type/notification'
import DetailNotification from './DetailNotification'

interface DropDownNotificationProps {
  onClose: () => void
}

const DropDownNotification: React.FC<DropDownNotificationProps> = ({ onClose }) => {
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [loading, setLoading] = useState(true)
  const [selectedNotification, setSelectedNotification] = useState<Notification | null>(null)
  const [isModalOpen, setIsModalOpen] = useState(false)

  useEffect(() => {
    const fetchNotifications = async () => {
      try {
        const response = await notificationService.getNotifications(0, 5)
        setNotifications(response.notifications)
      } catch (error) {
        console.error('Failed to fetch notifications:', error)
      } finally {
        setLoading(false)
      }
    }

    void fetchNotifications()
  }, [])

  const handleMarkAsRead = async (notificationId: string) => {
    try {
      await notificationService.markAsRead(notificationId)
      setNotifications(prevNotifications =>
        prevNotifications.map(notif =>
          notif.id === notificationId ? { ...notif, isRead: true } : notif
        )
      )
    } catch (error) {
      console.error('Failed to mark notification as read:', error)
    }
  }

  const handleNotificationClick = (notification: Notification) => {
    setSelectedNotification(notification)
    setIsModalOpen(true)
    if (!notification.isRead) {
      void handleMarkAsRead(notification.id)
    }
  }

  const formatTimeAgo = (dateString: string) => {
    const date = new Date(dateString)
    const now = new Date()
    const diffMs = now.getTime() - date.getTime()
    const diffMins = Math.floor(diffMs / 60000)
    const diffHours = Math.floor(diffMs / 3600000)
    const diffDays = Math.floor(diffMs / 86400000)

    if (diffMins < 1) return 'Vừa xong'
    if (diffMins < 60) return `${diffMins} phút trước`
    if (diffHours < 24) return `${diffHours} giờ trước`
    if (diffDays < 7) return `${diffDays} ngày trước`
    return date.toLocaleDateString('vi-VN')
  }

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'HIGH':
        return 'text-red-600'
      case 'NORMAL':
        return 'text-blue-600'
      case 'LOW':
        return 'text-gray-600'
      default:
        return 'text-blue-600'
    }
  }

  const getNotificationIcon = (type: string) => {
    switch (type) {
      case 'BALANCE_CHANGE':
        return 'account_balance_wallet'
      case 'SECURITY':
        return 'security'
      case 'SYSTEM':
        return 'info'
      case 'LOAN':
        return 'payments'
      default:
        return 'notifications'
    }
  }

  return (
    <div className="absolute right-0 top-full mt-2 w-96 bg-white rounded-2xl shadow-xl border border-blue-100 z-50 animate-slideDown max-h-[500px] flex flex-col">
      {/* Header */}
      <div className="px-5 py-4 border-b border-blue-100 flex items-center justify-between">
        <h3 className="text-lg font-semibold text-blue-900">Thông báo</h3>
        <Link
          to="/notifications"
          onClick={onClose}
          className="text-sm text-blue-600 hover:text-blue-700 font-medium"
        >
          Xem tất cả
        </Link>
      </div>

      {/* Notifications List */}
      <div className="overflow-y-auto flex-1">
        {loading ? (
          <div className="flex items-center justify-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          </div>
        ) : notifications.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-12 px-4">
            <span className="material-icons-round text-blue-200 text-5xl mb-3">
              notifications_none
            </span>
            <p className="text-blue-400 text-sm">Không có thông báo mới</p>
          </div>
        ) : (
          notifications.map((notification) => (
            <div
              key={notification.id}
              className={`px-5 py-4 border-b border-blue-50 hover:bg-blue-50 transition-colors cursor-pointer ${
                !notification.isRead ? 'bg-blue-50/50' : ''
              }`}
              onClick={() => handleNotificationClick(notification)}
            >
              <div className="flex gap-3">
                {/* Icon */}
                <div className={`shrink-0 w-10 h-10 rounded-full ${
                  !notification.isRead ? 'bg-blue-100' : 'bg-gray-100'
                } flex items-center justify-center`}>
                  <span className={`material-icons-round text-lg ${getPriorityColor(notification.priority)}`}>
                    {getNotificationIcon(notification.type)}
                  </span>
                </div>

                {/* Content */}
                <div className="flex-1 min-w-0">
                  <div className="flex items-start justify-between gap-2">
                    <p className={`text-sm font-medium ${
                      !notification.isRead ? 'text-blue-900' : 'text-gray-700'
                    } line-clamp-1`}>
                      {notification.title}
                    </p>
                    {!notification.isRead && (
                      <div className="w-2 h-2 rounded-full bg-blue-600 shrink-0 mt-1.5"></div>
                    )}
                  </div>
                  <p className="text-xs text-gray-600 mt-1 line-clamp-2">
                    {notification.content}
                  </p>
                  <p className="text-xs text-blue-500 mt-2">
                    {formatTimeAgo(notification.createdAt)}
                  </p>
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Footer */}
      {notifications.length > 0 && (
        <div className="px-5 py-3 border-t border-blue-100">
          <button
            onClick={async () => {
              try {
                await notificationService.markAllAsRead()
                setNotifications(prevNotifications =>
                  prevNotifications.map(notif => ({ ...notif, isRead: true }))
                )
              } catch (error) {
                console.error('Failed to mark all as read:', error)
              }
            }}
            className="w-full text-sm text-blue-600 hover:text-blue-700 font-medium py-2 hover:bg-blue-50 rounded-lg transition-colors"
          >
            Đánh dấu tất cả đã đọc
          </button>
        </div>
      )}

      {/* Detail Modal */}
      <DetailNotification
        notification={selectedNotification}
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
      />
    </div>
  )
}

export default DropDownNotification
