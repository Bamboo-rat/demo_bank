export enum NotificationType {
  BALANCE_CHANGE = 'BALANCE_CHANGE',
  SYSTEM = 'SYSTEM',
  SECURITY = 'SECURITY',
  LOAN = 'LOAN'
}

export enum Priority {
  LOW = 'LOW',
  NORMAL = 'NORMAL',
  HIGH = 'HIGH'
}

export enum ReferenceType {
  TRANSACTION = 'TRANSACTION',
  ACCOUNT = 'ACCOUNT',
  LOAN = 'LOAN'
}

export interface Notification {
  id: string
  type: NotificationType
  title: string
  content: string
  referenceType?: ReferenceType
  referenceId?: number
  priority: Priority
  createdAt: string
  isRead: boolean
  readAt?: string
  deliveredAt: string
}

export interface NotificationPageResponse {
  notifications: Notification[]
  totalElements: number
  totalPages: number
  currentPage: number
  pageSize: number
  hasNext: boolean
  hasPrevious: boolean
}

export interface UnreadCountResponse {
  unreadCount: number
}
