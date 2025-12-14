import React from 'react'
import { Link } from 'react-router'
import { customerService, type CustomerProfile } from '~/service/customerService'
import { accountService, type AccountSummary } from '~/service/accountService'
import { transactionService, type TransferResponse } from '~/service/transactionService'
import Layout from '~/component/layout/Layout'
import {
  ArrowUpRight,
  ArrowDownRight,
  Check,
  Clock,
  X
} from 'lucide-react';

const ACCENT_CONFIG = {
  purple: {
    iconBg: 'bg-purple-50',
    iconText: 'text-purple-600',
    border: 'border-purple-100'
  },
  green: {
    iconBg: 'bg-green-50',
    iconText: 'text-green-600',
    border: 'border-green-100'
  },
  orange: {
    iconBg: 'bg-orange-50',
    iconText: 'text-orange-600',
    border: 'border-orange-100'
  },
  blue: {
    iconBg: 'bg-blue-50',
    iconText: 'text-blue-600',
    border: 'border-blue-100'
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
  const [recentTransactions, setRecentTransactions] = React.useState<TransferResponse[]>([])
  const [transactionsLoading, setTransactionsLoading] = React.useState(false)

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

  React.useEffect(() => {
    if (!selectedAccount) {
      setRecentTransactions([])
      return
    }

    let isMounted = true
    setTransactionsLoading(true)

    const fetchTransactions = async () => {
      try {
        const transactions = await transactionService.getTransactionHistory(
          selectedAccount.accountNumber,
          0,
          5
        )
        if (isMounted) {
          setRecentTransactions(transactions)
        }
      } catch (error) {
        if (isMounted) {
          console.error('Failed to fetch transactions:', error)
          setRecentTransactions([])
        }
      } finally {
        if (isMounted) {
          setTransactionsLoading(false)
        }
      }
    }

    fetchTransactions()

    return () => {
      isMounted = false
    }
  }, [selectedAccount])

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
        <div className="h-8 w-48 bg-blue-100 rounded animate-pulse"></div>
      )
    }

    if (!selectedAccount) {
      return '--'
    }

    if (!showBalance) {
      return (
        <span className="text-2xl font-bold tracking-wider text-dark-blue">
          •••••••
        </span>
      )
    }

    if (!selectedAccountBalance) {
      return '---'
    }

    return (
      <span className="text-2xl font-bold text-dark-blue">
        {formatCurrency(selectedAccountBalance.amount, selectedAccountBalance.currency)}
      </span>
    )
  }, [balanceLoading, selectedAccount, selectedAccountBalance, showBalance])

  const dashboardCards: Array<{
    title: string
    icon: string
    link: string
    color: AccentColor
  }> = [
      { title: 'Chuyển tiền', icon: 'send', link: '/transfer', color: 'blue' },
      { title: 'Nạp tiền', icon: 'account_balance_wallet', link: '/deposit', color: 'green' },
      { title: 'Thanh toán', icon: 'payments', link: '/payments', color: 'orange' },
      { title: 'QR Code', icon: 'qr_code_scanner', link: '/qr-pay', color: 'purple' },
      { title: 'Vay vốn', icon: 'monetization_on', link: '/loan', color: 'blue' },
      { title: 'Tiết kiệm', icon: 'savings', link: '/savings', color: 'green' },
      { title: 'Thẻ', icon: 'credit_card', link: '/cards', color: 'orange' },
      { title: 'Hỗ trợ', icon: 'support_agent', link: '/support', color: 'purple' }
    ]

  return (
    <Layout>
      <div className="min-h-screen bg-linear-to-br from-blue-25 to-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">

          {/* Account Balance Card */}
          <div className="mb-8">
            <div className="bg-white rounded-xl shadow-sm border border-blue-100 p-6">
              <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-6">
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-4">
                    <div className="w-12 h-12 rounded-lg bg-blue-50 flex items-center justify-center">
                      <span className="material-icons-round text-blue-primary text-xl">account_balance</span>
                    </div>
                    <div>
                      <p className="font-semibold text-dark-blue">Tài khoản chính</p>
                      <div className="flex items-center gap-2 mt-1">
                        <select
                          className="bg-transparent text-dark-blue focus:outline-none font-medium"
                          value={selectedAccountId}
                          onChange={handleAccountChange}
                          disabled={accountsLoading || accounts.length === 0}
                        >
                          {accountsLoading && <option>Đang tải...</option>}
                          {!accountsLoading && accounts.length === 0 && (
                            <option value="">Không có tài khoản</option>
                          )}
                          {!accountsLoading &&
                            accounts.map((account) => (
                              <option key={account.accountId} value={account.accountId} className='m-1 border border-blue-100 rounded-xl'>
                                {account.accountNumber}
                              </option>
                            ))}
                        </select>
                        {selectedAccount && (
                          <span className="text-sm text-dark-blue/60 bg-blue-50 px-2 py-1 rounded">
                            {selectedAccount.accountTypeLabel}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                </div>

                <div className="flex flex-col items-start md:items-end">
                  <div className="flex items-center gap-3 mb-2">
                    <span className="text-sm font-medium text-dark-blue/70">Số dư khả dụng</span>

                    <div className="min-h-10 flex items-center">
                      {balanceDisplay}
                    </div>
                    <button
                      type="button"
                      onClick={toggleBalanceVisibility}
                      className="flex h-8 w-8 items-center justify-center rounded-full bg-blue-50 text-blue-primary hover:bg-blue-100 transition-colors duration-200"
                      disabled={balanceLoading || !selectedAccount}
                    >
                      <span className="material-icons-round text-base">
                        {showBalance ? 'visibility' : 'visibility_off'}
                      </span>
                    </button>
                  </div>

                </div>
              </div>
            </div>
          </div>

          {/* Quick Actions Grid */}
          <div className="mb-8">
            <h2 className="text-xl font-bold text-dark-blue mb-6">Thao tác nhanh</h2>
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
              {dashboardCards.map((card) => {
                const accent = ACCENT_CONFIG[card.color]
                return (
                  <Link
                    key={card.title}
                    to={card.link}
                    className="group bg-white rounded-xl border border-blue-100 p-5 transition-all duration-300 hover:shadow-lg hover:-translate-y-1"
                  >
                    <div className="flex flex-col items-center text-center">
                      <div className={`flex h-14 w-14 items-center justify-center rounded-full ${accent.iconBg} mb-3 group-hover:scale-110 transition-transform duration-300`}>
                        <span className={`material-icons-round text-xl ${accent.iconText}`}>
                          {card.icon}
                        </span>
                      </div>
                      <p className="font-medium text-dark-blue">{card.title}</p>
                    </div>
                  </Link>
                )
              })}
            </div>
          </div>

          {/* Recent Activity */}
          <div className="bg-white rounded-xl border border-gray-200 p-6">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold text-gray-900">Giao dịch gần đây</h3>
              <Link
                to="/transactions"
                className="text-sm text-blue-600 font-medium hover:text-blue-800 transition-colors"
              >
                Xem tất cả →
              </Link>
            </div>

            {transactionsLoading ? (
              <div className="text-center py-8">
                <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600 mx-auto"></div>
                <p className="text-gray-500 mt-3">Đang tải...</p>
              </div>
            ) : recentTransactions.length === 0 ? (
              <div className="text-center py-8">
                <p className="text-gray-500">Chưa có giao dịch nào</p>
              </div>
            ) : (
              <div className="space-y-2">
                {/* Header đơn giản */}
                <div className="grid grid-cols-5 gap-4 px-4 py-2 text-sm text-gray-600 font-medium border-b">
                  <div>Thời gian</div>
                  <div>Loại</div>
                  <div>Tài khoản</div>
                  <div>Số tiền</div>
                  <div>Trạng thái</div>
                </div>

                {/* Data rows */}
                {recentTransactions.map((txn) => {
                  const isOutgoing = txn.sourceAccountNumber === selectedAccount?.accountNumber

                  return (
                    <div key={txn.transactionId} className="grid grid-cols-5 gap-4 px-4 py-3 text-sm border-b hover:bg-gray-50">
                      {/* Cột Thời gian (Giữ nguyên) */}
                      <div>
                        <div>{new Date(txn.createdAt).toLocaleDateString('vi-VN')}</div>
                        <div className="text-xs text-gray-500">
                          {new Date(txn.createdAt).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })}
                        </div>
                      </div>

                      {/* Cột Loại - Đã cập nhật */}
                      <div className="flex items-center gap-2">
                        {/* Sử dụng ArrowUpRight cho giao dịch chuyển đi */}
                        {isOutgoing ? (
                          <ArrowUpRight className="text-red-600" size={18} strokeWidth={2.5} />
                        ) : (
                          <ArrowDownRight className="text-green-600" size={18} strokeWidth={2.5} />
                        )}
                        <span>{isOutgoing ? 'Chuyển đi' : 'Nhận về'}</span>
                      </div>

                      {/* Cột Tài khoản (Giữ nguyên) */}
                      <div>
                        <div className="font-medium truncate">
                          {isOutgoing ? txn.destinationAccountNumber : txn.sourceAccountNumber}
                        </div>
                      </div>

                      {/* Cột Số tiền (Giữ nguyên) */}
                      <div className={`font-semibold ${isOutgoing ? 'text-red-600' : 'text-green-600'}`}>
                        {isOutgoing ? '-' : '+'}{formatCurrency(txn.amount)}
                      </div>

                      {/* Cột Trạng thái - Đã cập nhật */}
                      <div>
                        <span className={`inline-flex items-center gap-1 px-2 py-1 rounded text-xs ${txn.status === 'COMPLETED' ? 'bg-green-100 text-green-800' :
                            txn.status === 'PENDING' ? 'bg-yellow-100 text-yellow-800' :
                              'bg-red-100 text-red-800'
                          }`}>
                          {/* Icon tương ứng với trạng thái */}
                          {txn.status === 'COMPLETED' ? (
                            <Check size={12} strokeWidth={3} />
                          ) : txn.status === 'PENDING' ? (
                            <Clock size={12} strokeWidth={2.5} />
                          ) : (
                            <X size={12} strokeWidth={3} />
                          )}
                          {/* Văn bản trạng thái */}
                          {txn.status === 'COMPLETED' ? 'Hoàn thành' :
                            txn.status === 'PENDING' ? 'Đang xử lý' :
                              'Thất bại'}
                        </span>
                      </div>
                    </div>
                  )
                })}
              </div>
            )}
          </div>
        </div>
      </div>
    </Layout>
  )
}

export default Dashboard