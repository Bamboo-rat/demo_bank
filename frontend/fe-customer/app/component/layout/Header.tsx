import React, { useState, useRef, useEffect } from 'react'
import { Link, useNavigate } from 'react-router'
import '~/assets/css/header.css'
import { customerService, type CustomerProfile } from '~/service/customerService'
import { authService } from '~/service/authService'

const Header = () => {
  const navigate = useNavigate()
  const [isDropdownOpen, setIsDropdownOpen] = useState(false)
  const dropdownRef = useRef<HTMLDivElement>(null)
  const [profile, setProfile] = useState<CustomerProfile | null>(null)
  const [profileLoading, setProfileLoading] = useState(true)

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

    fetchProfile()
  }, [])

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsDropdownOpen(false)
      }
    }

    document.addEventListener('mousedown', handleClickOutside)
    return () => {
      document.removeEventListener('mousedown', handleClickOutside)
    }
  }, [])

  const toggleDropdown = () => {
    setIsDropdownOpen(!isDropdownOpen)
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
    <header className="h-20 bg-white border-b border-blue-200 shadow-sm flex items-center justify-end px-8">

      <div className="flex items-center gap-6">

        <button className="relative p-3 rounded-xl hover:bg-blue-50 transition-colors duration-200 group">
          <span className="material-icons-round text-blue-600 text-2xl">
            notifications
          </span>
          <div className="absolute -top-1 -right-1 w-6 h-6 bg-red-500 text-white text-sm rounded-full flex items-center justify-center animate-pulse">
            3
          </div>
          <div className="absolute right-0 top-full mt-2 px-3 py-2 bg-blue-900 text-white text-sm rounded-lg opacity-0 group-hover:opacity-100 transition-opacity duration-200 pointer-events-none whitespace-nowrap z-10">
            Thông báo
            <div className="absolute right-2 -top-1 transform -translate-y-1/2 border-4 border-transparent border-b-blue-900"></div>
          </div>
        </button>

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

            <span className={`material-icons-round text-blue-400 text-xl transition-transform duration-200 ${
              isDropdownOpen ? 'rotate-180' : ''
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
    </header>
  )
}

export default Header