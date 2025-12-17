import React, { useState, useRef, useEffect } from 'react'
import { Link, useNavigate } from 'react-router'
import '~/assets/css/header.css'
import { customerService, type CustomerProfile } from '~/service/customerService'
import { authService } from '~/service/authService'
import { notificationService } from '~/service/notificationService'
import DropDownNotification from '~/component/features/notification/DropDownNotification'

const Header = () => {
  const navigate = useNavigate()
  const [isDropdownOpen, setIsDropdownOpen] = useState(false)
  const [isNotificationOpen, setIsNotificationOpen] = useState(false)
  const dropdownRef = useRef<HTMLDivElement>(null)
  const notificationRef = useRef<HTMLDivElement>(null)
  const [profile, setProfile] = useState<CustomerProfile | null>(null)
  const [profileLoading, setProfileLoading] = useState(true)
  const [unreadCount, setUnreadCount] = useState(0)

  // Fetch customer profile
  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const data = await customerService.getMyProfile()
        setProfile(data)
      } catch (error) {
        console.error('Failed to fetch profile:', error)
      } finally {
        setProfileLoading(false)
      }
    }

    void fetchProfile()
  }, [])

  // Fetch unread notification count
  useEffect(() => {
    const fetchUnreadCount = async () => {
      try {
        const count = await notificationService.getUnreadCount()
        setUnreadCount(count)
      } catch (error) {
        console.error('Failed to fetch unread count:', error)
      }
    }

    void fetchUnreadCount()

    // Poll for unread count every 30 seconds
    const interval = setInterval(() => {
      void fetchUnreadCount()
    }, 30000)

    return () => clearInterval(interval)
  }, [])

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsDropdownOpen(false)
      }
      if (notificationRef.current && !notificationRef.current.contains(event.target as Node)) {
        setIsNotificationOpen(false)
      }
    }

    document.addEventListener('mousedown', handleClickOutside)
    return () => {
      document.removeEventListener('mousedown', handleClickOutside)
    }
  }, [])

  const toggleDropdown = () => {
    setIsDropdownOpen(!isDropdownOpen)
    setIsNotificationOpen(false)
  }

  const toggleNotification = () => {
    setIsNotificationOpen(!isNotificationOpen)
    setIsDropdownOpen(false)
  }

  interface UserMenuItem {
    icon: string
    label: string
    link?: string
    action?: () => void | Promise<void>
  }

  const handleLogout = async () => {
    setIsDropdownOpen(false)
    try {
      await authService.logout()
    } catch (error) {
      console.error('Logout failed:', error)
    } finally {
      navigate('/', { replace: true })
    }
  }

  const userMenuItems: UserMenuItem[] = [
    { icon: 'person', label: 'Hồ sơ người dùng', link: '/profile' },
    { icon: 'settings', label: 'Cấu hình', link: '/configuration' },
    { icon: 'tune', label: 'Cài đặt', link: '/settings' },
    { icon: 'logout', label: 'Đăng xuất', action: handleLogout }
  ]

  const handleMenuItemClick = () => {
    setIsDropdownOpen(false)
  }

  return (
    <header className="bg-white shadow-md sticky top-0 z-50">
      <div className="container mx-auto px-6 py-4 flex items-center justify-end">
        <div className="flex items-center gap-4">
          <div className="relative" ref={notificationRef}>
            <button
              onClick={toggleNotification}
              className="relative p-3 rounded-xl hover:bg-blue-50 transition-colors duration-200 group"
            >
              <span className="material-icons-round text-blue-600 text-2xl">
                notifications
              </span>
              {unreadCount > 0 && (
                <div className="absolute -top-1 -right-1 w-6 h-6 bg-red-500 text-white text-xs rounded-full flex items-center justify-center font-semibold animate-pulse">
                  {unreadCount > 9 ? '9+' : unreadCount}
                </div>
              )}
              <div className="absolute right-0 top-full mt-2 px-3 py-2 bg-blue-900 text-white text-sm rounded-lg opacity-0 group-hover:opacity-100 transition-opacity duration-200 pointer-events-none whitespace-nowrap z-10">
                Thông báo
                <div className="absolute right-2 -top-1 transform -translate-y-1/2 border-4 border-transparent border-b-blue-900"></div>
              </div>
            </button>

            {isNotificationOpen && (
              <DropDownNotification
                onClose={() => {
                  setIsNotificationOpen(false)
                  // Refresh unread count after closing
                  void notificationService.getUnreadCount().then(count => setUnreadCount(count))
                }}
              />
            )}
          </div>

          <div className="relative" ref={dropdownRef}>
            <button
              onClick={toggleDropdown}
              className="flex items-center gap-4 p-2 rounded-xl hover:bg-blue-50 transition-colors duration-200 group"
            >

              <div className="w-12 h-12 rounded-full bg-linear-to-br from-blue-500 to-blue-400 flex items-center justify-center shadow-sm">
                <span className="text-white font-semibold text-base">
                  {profileLoading ? '...' : (profile?.fullName?.substring(0, 2).toUpperCase() || 'KH')}
                </span>
              </div>

              <div className="text-left">
                <p className="text-lg font-semibold text-blue-900">
                  {profileLoading ? 'Đang tải...' : (profile?.fullName || 'N/A')}
                </p>
                <p className="text-sm text-blue-600/70">Khách hàng</p>
              </div>

              <span className={`material-icons-round text-blue-400 text-xl transition-transform duration-200 ${isDropdownOpen ? 'rotate-180' : ''
                }`}>
                expand_more
              </span>
            </button>

            {isDropdownOpen && (
              <div className="absolute right-0 top-full mt-3 w-72 bg-white rounded-2xl shadow-xl border border-blue-100 py-3 z-50 animate-slideDown">
                <div className="px-5 py-4 border-b border-blue-100">
                  <p className="text-lg font-semibold text-blue-900">
                    {profile?.fullName || 'Khách hàng'}
                  </p>
                  <p className="text-sm text-blue-600/70 mt-1">
                    {profile?.email || profile?.phoneNumber || 'N/A'}
                  </p>
                </div>

                <div className="py-2">
                  {userMenuItems.map((item, index) => (
                    item.action ? (
                      <button
                        key={index}
                        type="button"
                        onClick={() => { void item.action?.() }}
                        className="w-full flex items-center gap-4 px-5 py-4 text-left text-base text-blue-900/80 hover:bg-blue-50 hover:text-blue-700 transition-colors duration-200"
                      >
                        <span className="material-icons-round text-blue-500 text-xl flex items-center justify-center w-6">
                          {item.icon}
                        </span>
                        <span className="font-medium">{item.label}</span>
                      </button>
                    ) : (
                      <Link
                        key={index}
                        to={item.link!}
                        onClick={handleMenuItemClick}
                        className="flex items-center gap-4 px-5 py-4 text-base text-blue-900/80 hover:bg-blue-50 hover:text-blue-700 transition-colors duration-200"
                      >
                        <span className="material-icons-round text-blue-500 text-xl flex items-center justify-center w-6">
                          {item.icon}
                        </span>
                        <span className="font-medium">{item.label}</span>
                      </Link>
                    )
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  )
}

export default Header