import React, { createContext, useContext, useState, useCallback } from 'react'

export type ToastType = 'success' | 'error' | 'warning' | 'info'

export interface ToastMessage {
  id: string
  message: string
  type: ToastType
  duration?: number
  action?: {
    label: string
    onClick: () => void
  }
}

interface ToastContextType {
  toasts: ToastMessage[]
  showToast: (
    message: string,
    type?: ToastType,
    duration?: number,
    action?: ToastMessage['action']
  ) => void
  hideToast: (id: string) => void
  success: (message: string, duration?: number) => void
  error: (message: string, duration?: number) => void
  warning: (message: string, duration?: number) => void
  info: (message: string, duration?: number) => void
}

const ToastContext = createContext<ToastContextType | undefined>(undefined)

export const ToastProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [toasts, setToasts] = useState<ToastMessage[]>([])

  const hideToast = useCallback((id: string) => {
    setToasts((prev) => prev.filter((toast) => toast.id !== id))
  }, [])

  const showToast = useCallback(
    (
      message: string,
      type: ToastType = 'info',
      duration: number = 5000,
      action?: ToastMessage['action']
    ) => {
      const id = `toast-${Date.now()}-${Math.random()}`
      const newToast: ToastMessage = {
        id,
        message,
        type,
        duration,
        action
      }

      setToasts((prev) => [...prev, newToast])

      // Auto remove after duration
      if (duration > 0) {
        setTimeout(() => {
          hideToast(id)
        }, duration)
      }
    },
    [hideToast]
  )

  const success = useCallback(
    (message: string, duration?: number) => {
      showToast(message, 'success', duration)
    },
    [showToast]
  )

  const error = useCallback(
    (message: string, duration?: number) => {
      showToast(message, 'error', duration)
    },
    [showToast]
  )

  const warning = useCallback(
    (message: string, duration?: number) => {
      showToast(message, 'warning', duration)
    },
    [showToast]
  )

  const info = useCallback(
    (message: string, duration?: number) => {
      showToast(message, 'info', duration)
    },
    [showToast]
  )

  return (
    <ToastContext.Provider
      value={{
        toasts,
        showToast,
        hideToast,
        success,
        error,
        warning,
        info
      }}
    >
      {children}
    </ToastContext.Provider>
  )
}

export const useToast = () => {
  const context = useContext(ToastContext)
  if (!context) {
    throw new Error('useToast must be used within ToastProvider')
  }
  return context
}
