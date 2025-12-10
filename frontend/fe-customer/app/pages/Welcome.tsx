import React from 'react'
import { Link } from 'react-router'
import { customerService, type CustomerProfile } from '~/service/customerService'
import { accountService, type AccountSummary } from '~/service/accountService'
import Layout from '~/component/layout/Layout'

const ACCENT_CONFIG = {
  purple: {
    iconBg: 'bg-purple-100',
    iconText: 'text-purple-500',
    gradientFrom: 'from-purple-50',
    border: 'border-purple-100',
    hoverBg: 'hover:bg-purple-50'
  },
  green: {
    iconBg: 'bg-green-100',
    iconText: 'text-green-500',
    gradientFrom: 'from-green-50',
    border: 'border-green-100',
    hoverBg: 'hover:bg-green-50'
  },
  orange: {
    iconBg: 'bg-orange-100',
    iconText: 'text-orange-500',
    gradientFrom: 'from-orange-50',
    border: 'border-orange-100',
    hoverBg: 'hover:bg-orange-50'
  },
  blue: {
    iconBg: 'bg-blue-100',
    iconText: 'text-blue-500',
    gradientFrom: 'from-blue-50',
    border: 'border-blue-100',
    hoverBg: 'hover:bg-blue-50'
  }
} as const

type AccentColor = keyof typeof ACCENT_CONFIG

const formatCurrency = (value: number, currency = 'VND') => {
  try {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency,
      minimumFractionDigits: 0
    }).format(value)
  } catch (error) {
    console.error('Currency format error:', error)
    return value.toLocaleString('vi-VN')
  }
}

const Dashboard = () => {
  const [profile, setProfile] = React.useState<CustomerProfile | null>(null)
  const [profileLoading, setProfileLoading] = React.useState(true)
  const [accounts, setAccounts] = React.useState<AccountSummary[]>([])
  const [accountsLoading, setAccountsLoading] = React.useState(true)
  const [accountsError, setAccountsError] = React.useState<string | null>(null)
  const [selectedAccountId, setSelectedAccountId] = React.useState('')
  const [balanceLoading, setBalanceLoading] = React.useState(false)
  const [selectedAccountBalance, setSelectedAccountBalance] = React.useState<{
    amount: number
    currency: string
  } | null>(null)
  const [showBalance, setShowBalance] = React.useState(false)

  React.useEffect(() => {
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

  React.useEffect(() => {
    const fetchAccounts = async () => {
      setAccountsLoading(true)
      setAccountsError(null)

      try {
        const data = await accountService.getMyAccounts()
        setAccounts(data)

        const defaultAccount =
          data.find((item) => item.accountType === 'CHECKING') ?? data[0]

        if (defaultAccount) {
          setSelectedAccountId(defaultAccount.accountId)
        } else {
          setSelectedAccountId('')
        }
      } catch (error) {
        console.error('Failed to fetch accounts:', error)
        setAccountsError(
          error instanceof Error
            ? error.message
            : 'Không thể tải danh sách tài khoản'
        )
        setAccounts([])
        setSelectedAccountId('')
      } finally {
        setAccountsLoading(false)
      }
    }

    fetchAccounts()
  }, [])

  React.useEffect(() => {
    if (!selectedAccountId) {
      setSelectedAccountBalance(null)
      return
    }

    const selectedAccount = accounts.find(
      (item) => item.accountId === selectedAccountId
    )

    if (!selectedAccount) {
      setSelectedAccountBalance(null)
      return
    }

    let isMounted = true
    setBalanceLoading(true)

    const fetchDetail = async () => {
      try {
        const detail = await accountService.getAccountDetail(
          selectedAccount.accountNumber
        )

        if (isMounted) {
          setSelectedAccountBalance({
            amount: detail.balance,
            currency: detail.currency
          })
        }
      } catch (error) {
        if (isMounted) {
          console.error('Failed to fetch account detail:', error)
          setSelectedAccountBalance(null)
        }
      } finally {
        if (isMounted) {
          setBalanceLoading(false)
        }
      }
    }

    fetchDetail()

    return () => {
      isMounted = false
    }
  }, [selectedAccountId, accounts])

  const selectedAccount = React.useMemo(() => {
    return accounts.find((item) => item.accountId === selectedAccountId) ?? null
  }, [accounts, selectedAccountId])

  const handleAccountChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedAccountId(event.target.value)
    setShowBalance(false)
  }

  const toggleBalanceVisibility = () => {
    setShowBalance((prev) => !prev)
  }

  const balanceDisplay = React.useMemo(() => {
    if (balanceLoading) {
      return (
        <div className="flex items-center gap-2">
          <div className="h-8 w-32 bg-blue-100 rounded animate-pulse"></div>
        </div>
      )
    }

    if (!selectedAccount) {
      return '--'
    }

    if (!showBalance) {
      return (
        <span className="text-3xl font-bold tracking-wider">
          •••••••
        </span>
      )
    }

    if (!selectedAccountBalance) {
      return '---'
    }

    return (
      <span className="text-3xl font-bold text-dark-blue">
        {formatCurrency(selectedAccountBalance.amount, selectedAccountBalance.currency)}
      </span>
    )
  }, [balanceLoading, selectedAccount, selectedAccountBalance, showBalance])

  const dashboardCards: Array<{
    title: string
    icon: string
    link: string
    color: AccentColor
    description: string
  }> = [
    {
      title: 'Vay vốn',
      icon: 'handshake',
      link: '#',
      color: 'purple',
      description: 'Vay linh hoạt với lãi suất ưu đãi'
    },
    {
      title: 'Tiết kiệm',
      icon: 'savings',
      link: '#',
      color: 'green',
      description: 'Gửi tiết kiệm với nhiều kỳ hạn'
    },
    {
      title: 'Thẻ',
      icon: 'credit_card',
      link: '#',
      color: 'orange',
      description: 'Quản lý thẻ thanh toán'
    },
    {
      title: 'Hỗ trợ',
      icon: 'support_agent',
      link: '#',
      color: 'blue',
      description: 'Liên hệ tổng đài 24/7'
    }
  ]

  const quickActions: Array<{
    title: string
    icon: string
    link: string
    color: AccentColor
  }> = [
    { title: 'Chuyển tiền', icon: 'send', link: '#', color: 'blue' },
    { title: 'Nạp tiền', icon: 'add_circle', link: '#', color: 'green' },
    { title: 'Thanh toán', icon: 'payment', link: '#', color: 'orange' },
    { title: 'QR Code', icon: 'qr_code_scanner', link: '#', color: 'purple' }
  ]

  return (
    <Layout>
      <div className="min-h-screen bg-linear-to-br from-blue-25 to-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {/* Welcome Section */}
          <div className="mb-8">
            <div className="bg-linear-to-r from-blue-500 to-blue-600 rounded-2xl shadow-xl p-6 md:p-8">
              <div className="flex flex-col md:flex-row md:items-center md:justify-between">
                <div className="mb-4 md:mb-0">
                  <h1 className="text-2xl md:text-3xl font-bold text-white mb-2">
                    Chào mừng trở lại, {profile?.fullName || 'Khách hàng'}!
                  </h1>
                  <p className="text-blue-100">
                    Chúc bạn một ngày làm việc hiệu quả
                  </p>
                </div>
                <div className="flex items-center gap-4">
                  <div className="bg-white/20 backdrop-blur-sm rounded-xl p-3">
                    <span className="material-icons-round text-white text-2xl">account_balance</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Account Card */}
          <div className="mb-8">
            <div className="bg-white rounded-2xl shadow-lg overflow-hidden">
              <div className="bg-linear-to-r from-blue-50 to-blue-100 p-6 border-b border-blue-200">
                <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
                  <div className="flex-1">
                    <h2 className="text-xl font-bold text-dark-blue mb-3">
                      Tài khoản chính
                    </h2>
                    <div className="space-y-2">
                      <div className="flex items-center gap-3">
                        <span className="material-icons-round text-blue-500 text-xl">account_balance</span>
                        <select
                          className="flex-1 rounded-lg border border-blue-200 bg-white px-4 py-2 text-dark-blue focus:border-blue-primary focus:outline-none focus:ring-2 focus:ring-blue-primary/20 transition-all duration-200"
                          value={selectedAccountId}
                          onChange={handleAccountChange}
                          disabled={accountsLoading || accounts.length === 0}
                        >
                          {accountsLoading && <option>Đang tải tài khoản...</option>}
                          {!accountsLoading && accounts.length === 0 && (
                            <option value="">Không có tài khoản khả dụng</option>
                          )}
                          {!accountsLoading &&
                            accounts.map((account) => (
                              <option key={account.accountId} value={account.accountId}>
                                {`${account.accountTypeLabel} • ${account.accountNumber}`}
                              </option>
                            ))}
                        </select>
                      </div>
                      {selectedAccount && (
                        <div className="flex items-center gap-2 text-sm text-dark-blue/70">
                          <span className="material-icons-round text-base">badge</span>
                          <span>{selectedAccount.accountNumber}</span>
                        </div>
                      )}
                    </div>
                  </div>
                  
                  <div className="bg-white rounded-xl p-6 shadow-sm min-w-[280px]">
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-sm font-medium text-dark-blue/70">
                        Số dư khả dụng
                      </span>
                      <button
                        type="button"
                        onClick={toggleBalanceVisibility}
                        className="flex h-8 w-8 items-center justify-center rounded-full border border-blue-200 bg-blue-50 text-blue-500 transition-all duration-200 hover:bg-blue-500 hover:text-white hover:scale-105 disabled:cursor-not-allowed disabled:opacity-60"
                        disabled={balanceLoading || !selectedAccount}
                      >
                        <span className="material-icons-round text-lg">
                          {showBalance ? 'visibility' : 'visibility_off'}
                        </span>
                      </button>
                    </div>
                    <div className="min-h-10 flex items-center">
                      {balanceDisplay}
                    </div>
                    {selectedAccount && (
                      <div className="mt-4 pt-4 border-t border-blue-100">
                        <p className="text-xs text-dark-blue/50">
                          {selectedAccount.accountTypeLabel}
                        </p>
                      </div>
                    )}
                  </div>
                </div>
              </div>
              
              <div className="p-6">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-lg font-semibold text-dark-blue">
                    Sản phẩm & Dịch vụ
                  </h3>
                  <Link
                    to="#"
                    className="text-sm text-blue-primary hover:text-blue-600 font-medium flex items-center gap-1"
                  >
                    Xem tất cả
                    <span className="material-icons-round text-base">chevron_right</span>
                  </Link>
                </div>
                
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                  {dashboardCards.map((card) => {
                    const accent = ACCENT_CONFIG[card.color]
                    return (
                      <Link
                        key={card.title}
                        to={card.link}
                        className={`group bg-white rounded-xl border ${accent.border} p-5 transition-all duration-300 hover:shadow-lg hover:-translate-y-1 ${accent.hoverBg}`}
                      >
                        <div className="flex items-center gap-4 mb-3">
                          <div className={`flex h-12 w-12 items-center justify-center rounded-lg ${accent.iconBg} group-hover:scale-105 transition-transform duration-300`}>
                            <span className={`material-icons-round text-xl ${accent.iconText}`}>
                              {card.icon}
                            </span>
                          </div>
                          <div>
                            <p className="font-semibold text-dark-blue">{card.title}</p>
                            <p className="text-xs text-dark-blue/60">{card.description}</p>
                          </div>
                        </div>
                        <div className="flex items-center justify-between">
                          <span className="text-xs text-dark-blue/50">Truy cập ngay</span>
                          <span className={`material-icons-round text-base ${accent.iconText} opacity-0 group-hover:opacity-100 transition-opacity duration-300`}>
                            arrow_forward
                          </span>
                        </div>
                      </Link>
                    )
                  })}
                </div>
              </div>
            </div>
          </div>

          {/* Quick Actions */}
          <div className="mb-8">
            <div className="bg-white rounded-2xl shadow-lg p-6">
              <h3 className="text-xl font-bold text-dark-blue mb-6">
                Thao tác nhanh
              </h3>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                {quickActions.map((action) => {
                  const accent = ACCENT_CONFIG[action.color]
                  return (
                    <Link
                      key={action.title}
                      to={action.link}
                      className={`group bg-linear-to-br ${accent.gradientFrom} to-white rounded-xl border ${accent.border} p-5 text-center transition-all duration-300 hover:shadow-lg hover:-translate-y-1`}
                    >
                      <div className="flex flex-col items-center">
                        <div className={`flex h-14 w-14 items-center justify-center rounded-full ${accent.iconBg} mb-3 group-hover:scale-110 transition-transform duration-300`}>
                          <span className={`material-icons-round text-2xl ${accent.iconText}`}>
                            {action.icon}
                          </span>
                        </div>
                        <p className="font-medium text-dark-blue">{action.title}</p>
                        <span className="text-xs text-dark-blue/50 mt-2 opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                          Nhấn để thực hiện
                        </span>
                      </div>
                    </Link>
                  )
                })}
              </div>
            </div>
          </div>

          {/* Recent Activity */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* Recent Transactions */}
            <div className="bg-white rounded-2xl shadow-lg overflow-hidden">
              <div className="bg-linear-to-r from-blue-50 to-blue-100 p-6 border-b border-blue-200">
                <div className="flex items-center justify-between">
                  <h3 className="text-xl font-bold text-dark-blue">
                    Giao dịch gần đây
                  </h3>
                  <span className="material-icons-round text-blue-500 text-2xl">receipt_long</span>
                </div>
              </div>
              <div className="p-6">
                <div className="text-center py-10">
                  <div className="mb-4">
                    <span className="material-icons-round text-dark-blue/20 text-5xl">swap_horiz</span>
                  </div>
                  <p className="text-dark-blue/70 mb-2">
                    Chưa có giao dịch nào trong ngày
                  </p>
                  <p className="text-sm text-dark-blue/50">
                    Các giao dịch sẽ hiển thị tại đây
                  </p>
                </div>
                <div className="flex justify-center">
                  <Link
                    to="#"
                    className="inline-flex items-center gap-2 px-4 py-2 rounded-lg bg-blue-50 text-blue-primary font-medium hover:bg-blue-100 transition-colors duration-200"
                  >
                    <span className="material-icons-round text-base">history</span>
                    Xem lịch sử giao dịch
                  </Link>
                </div>
              </div>
            </div>

            {/* Quick Stats */}
            <div className="bg-white rounded-2xl shadow-lg overflow-hidden">
              <div className="bg-linear-to-r from-green-50 to-green-100 p-6 border-b border-green-200">
                <div className="flex items-center justify-between">
                  <h3 className="text-xl font-bold text-dark-blue">
                    Tổng quan tài chính
                  </h3>
                  <span className="material-icons-round text-green-500 text-2xl">trending_up</span>
                </div>
              </div>
              <div className="p-6">
                <div className="space-y-4">
                  <div className="flex items-center justify-between p-4 bg-blue-25 rounded-xl">
                    <div className="flex items-center gap-3">
                      <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-blue-100">
                        <span className="material-icons-round text-blue-500 text-lg">savings</span>
                      </div>
                      <div>
                        <p className="font-medium text-dark-blue">Tổng tiết kiệm</p>
                        <p className="text-sm text-dark-blue/60">3 tài khoản</p>
                      </div>
                    </div>
                    <span className="text-lg font-bold text-green-600">
                      150,000,000 ₫
                    </span>
                  </div>
                  
                  <div className="flex items-center justify-between p-4 bg-orange-25 rounded-xl">
                    <div className="flex items-center gap-3">
                      <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-orange-100">
                        <span className="material-icons-round text-orange-500 text-lg">credit_card</span>
                      </div>
                      <div>
                        <p className="font-medium text-dark-blue">Hạn mức thẻ</p>
                        <p className="text-sm text-dark-blue/60">2 thẻ đang sử dụng</p>
                      </div>
                    </div>
                    <span className="text-lg font-bold text-dark-blue">
                      50,000,000 ₫
                    </span>
                  </div>
                  
                  <div className="flex items-center justify-between p-4 bg-purple-25 rounded-xl">
                    <div className="flex items-center gap-3">
                      <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-purple-100">
                        <span className="material-icons-round text-purple-500 text-lg">handshake</span>
                      </div>
                      <div>
                        <p className="font-medium text-dark-blue">Khoản vay</p>
                        <p className="text-sm text-dark-blue/60">Đang vay</p>
                      </div>
                    </div>
                    <span className="text-lg font-bold text-dark-blue">
                      75,000,000 ₫
                    </span>
                  </div>
                </div>
                
                <div className="mt-6 pt-6 border-t border-blue-100">
                  <Link
                    to="#"
                    className="inline-flex items-center gap-2 px-4 py-2 rounded-lg bg-green-50 text-green-600 font-medium hover:bg-green-100 transition-colors duration-200"
                  >
                    <span className="material-icons-round text-base">analytics</span>
                    Xem báo cáo chi tiết
                  </Link>
                </div>
              </div>
            </div>
          </div>

          {/* Promo Banner */}
          <div className="mt-8">
            <div className="bg-linear-to-r from-orange-400 to-orange-500 rounded-2xl shadow-xl p-6 md:p-8">
              <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-6">
                <div className="flex-1">
                  <h3 className="text-xl md:text-2xl font-bold text-white mb-2">
                    Ưu đãi đặc biệt dành cho bạn!
                  </h3>
                  <p className="text-orange-100 mb-4">
                    Nhận lãi suất ưu đãi khi gửi tiết kiệm online. Không phí mở sổ, không phí duy trì.
                  </p>
                  <Link
                    to="#"
                    className="inline-flex items-center gap-2 px-5 py-2.5 bg-white text-orange-500 font-semibold rounded-lg hover:bg-orange-50 transition-colors duration-200"
                  >
                    <span>Khám phá ngay</span>
                    <span className="material-icons-round text-lg">arrow_forward</span>
                  </Link>
                </div>
                <div className="shrink-0">
                  <div className="flex h-20 w-20 items-center justify-center rounded-full bg-white/20 backdrop-blur-sm">
                    <span className="material-icons-round text-white text-4xl">local_offer</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  )
}

export default Dashboard