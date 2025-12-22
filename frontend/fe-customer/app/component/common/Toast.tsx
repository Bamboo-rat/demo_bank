import React, { useEffect, useState } from 'react'
import { useToast, type ToastMessage } from '~/context/ToastContext'

const Toast: React.FC = () => {
  const { toasts } = useToast()

  return (
    <div className="fixed top-4 right-4 z-50 flex flex-col gap-2 max-w-md">
      {toasts.map((toast) => (
        <ToastItem key={toast.id} toast={toast} />
      ))}
    </div>
  )
}

interface ToastItemProps {
  toast: ToastMessage
}

const ToastItem: React.FC<ToastItemProps> = ({ toast }) => {
  const { hideToast } = useToast()
  const [isVisible, setIsVisible] = useState(false)
  const [isLeaving, setIsLeaving] = useState(false)

  useEffect(() => {
    // Trigger enter animation
    requestAnimationFrame(() => {
      setIsVisible(true)
    })
  }, [])

  const handleClose = () => {
    setIsLeaving(true)
    setTimeout(() => {
      hideToast(toast.id)
    }, 300) // Match animation duration
  }

  const getTypeStyles = () => {
    switch (toast.type) {
      case 'success':
        return {
          bg: 'bg-green-50 border-green-200',
          icon: 'text-green-600',
          iconName: 'check_circle'
        }
      case 'error':
        return {
          bg: 'bg-red-50 border-red-200',
          icon: 'text-red-600',
          iconName: 'error'
        }
      case 'warning':
        return {
          bg: 'bg-yellow-50 border-yellow-200',
          icon: 'text-yellow-600',
          iconName: 'warning'
        }
      case 'info':
      default:
        return {
          bg: 'bg-blue-50 border-blue-200',
          icon: 'text-blue-600',
          iconName: 'info'
        }
    }
  }

  const styles = getTypeStyles()

  return (
    <div
      className={`
        ${styles.bg}
        border rounded-lg shadow-lg p-4 min-w-[320px]
        transform transition-all duration-300 ease-in-out
        ${isVisible && !isLeaving ? 'translate-y-0 opacity-100' : '-translate-y-4 opacity-0'}
      `}
    >
      <div className="flex items-start gap-3">
        <span className={`material-icons-round ${styles.icon} text-2xl`}>
          {styles.iconName}
        </span>
        
        <div className="flex-1">
          <p className="text-gray-800 text-sm font-medium leading-relaxed">
            {toast.message}
          </p>
          
          {toast.action && (
            <button
              onClick={() => {
                toast.action?.onClick()
                handleClose()
              }}
              className="mt-2 text-sm font-semibold text-blue-600 hover:text-blue-700 transition-colors"
            >
              {toast.action.label}
            </button>
          )}
        </div>

        <button
          onClick={handleClose}
          className="text-gray-400 hover:text-gray-600 transition-colors flex-shrink-0"
          aria-label="Close notification"
        >
          <span className="material-icons-round text-xl">close</span>
        </button>
      </div>
    </div>
  )
}

export default Toast
