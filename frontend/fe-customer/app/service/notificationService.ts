import { isAxiosError } from 'axios'
import axiosNotification from '~/config/axiosNotification'
import type { 
  Notification, 
  NotificationPageResponse, 
  UnreadCountResponse,
  NotificationType 
} from '../type/notification'

interface ApiResponse<T> {
  success: boolean
  message?: string
  data: T
  errorCode?: string
  timestamp?: string
}

const normalizeError = (error: unknown) => {
  if (isAxiosError(error)) {
    const message = (error.response?.data as { message?: string } | undefined)?.message
    return new Error(message ?? 'Có lỗi xảy ra, vui lòng thử lại')
  }
  return error instanceof Error ? error : new Error('Có lỗi xảy ra, vui lòng thử lại')
}

export const notificationService = {
  /**
   * Lấy danh sách thông báo với pagination
   */
  getNotifications: async (page = 0, size = 20): Promise<NotificationPageResponse> => {
    try {
      const { data } = await axiosNotification.get<ApiResponse<NotificationPageResponse>>(
        '/notifications',
        { params: { page, size } }
      )
      return data.data
    } catch (error) {
      throw normalizeError(error)
    }
  },

  /**
   * Lấy danh sách thông báo theo loại
   */
  getNotificationsByType: async (
    type: NotificationType,
    page = 0,
    size = 20
  ): Promise<NotificationPageResponse> => {
    try {
      const { data } = await axiosNotification.get<ApiResponse<NotificationPageResponse>>(
        `/notifications/type/${type}`,
        { params: { page, size } }
      )
      return data.data
    } catch (error) {
      throw normalizeError(error)
    }
  },

  /**
   * Lấy chi tiết một thông báo
   */
  getNotificationDetail: async (notificationId: string): Promise<Notification> => {
    try {
      const { data } = await axiosNotification.get<ApiResponse<Notification>>(
        `/notifications/${notificationId}`
      )
      return data.data
    } catch (error) {
      throw normalizeError(error)
    }
  },

  /**
   * Đánh dấu thông báo đã đọc
   */
  markAsRead: async (notificationId: string): Promise<Notification> => {
    try {
      const { data } = await axiosNotification.put<ApiResponse<Notification>>(
        `/notifications/${notificationId}/mark-read`
      )
      return data.data
    } catch (error) {
      throw normalizeError(error)
    }
  },

  /**
   * Đánh dấu tất cả thông báo đã đọc
   */
  markAllAsRead: async (): Promise<number> => {
    try {
      const { data } = await axiosNotification.put<ApiResponse<number>>(
        '/notifications/mark-all-read'
      )
      return data.data
    } catch (error) {
      throw normalizeError(error)
    }
  },

  /**
   * Đếm số thông báo chưa đọc
   */
  getUnreadCount: async (): Promise<number> => {
    try {
      const { data } = await axiosNotification.get<ApiResponse<UnreadCountResponse>>(
        '/notifications/unread-count'
      )
      return data.data.unreadCount
    } catch (error) {
      throw normalizeError(error)
    }
  }
}
