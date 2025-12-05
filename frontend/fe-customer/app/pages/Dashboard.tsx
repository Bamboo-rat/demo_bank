import React from 'react'
import { useNavigate, Link } from 'react-router'
import { authService } from '~/service/authService'
import Layout from '~/component/layout/Layout'

const Dashboard = () => {
  const navigate = useNavigate()
  const [loading, setLoading] = React.useState(false)

  const handleLogout = async () => {
    setLoading(true)
    try {
      await authService.logout()
      navigate('/')
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      setLoading(false)
    }
  }

  const dashboardCards = [
    {
      title: 'Tài khoản',
      icon: 'account_balance_wallet',
      value: '0 VNĐ',
      subtitle: 'Số dư khả dụng',
      color: 'blue'
    },
    {
      title: 'Giao dịch',
      icon: 'receipt_long',
      value: '0',
      subtitle: 'Giao dịch hôm nay',
      color: 'green'
    },
    {
      title: 'Thẻ',
      icon: 'credit_card',
      value: '0',
      subtitle: 'Thẻ đang hoạt động',
      color: 'orange'
    },
    {
      title: 'Tiết kiệm',
      icon: 'savings',
      value: '0 VNĐ',
      subtitle: 'Tổng tiết kiệm',
      color: 'purple'
    }
  ]

  const quickActions = [
    { title: 'Chuyển tiền', icon: 'send', link: '#' },
    { title: 'Nạp tiền', icon: 'add_circle', link: '#' },
    { title: 'Thanh toán', icon: 'payment', link: '#' },
    { title: 'Quét mã QR', icon: 'qr_code_scanner', link: '#' }
  ]

  return (
    <Layout>
      <div className="min-h-screen bg-gradient-to-br from-blue-50 to-white">
        {/* Header */}
        <div className="bg-white shadow-md">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
            <div className="flex justify-between items-center">
              <div className="flex items-center space-x-3">
                <div className="w-12 h-12 rounded-lg bg-blue-primary flex items-center justify-center">
                  <span className="material-icons-round text-white text-2xl">account_balance</span>
                </div>
                <div>
                  <h1 className="text-xl font-bold text-dark-blue">Dashboard</h1>
                  <p className="text-sm text-blue-primary">KienLong Bank</p>
                </div>
              </div>
              <button
                onClick={handleLogout}
                disabled={loading}
                className="flex items-center gap-2 px-4 py-2 bg-red-500 hover:bg-red-600 text-white font-semibold rounded-lg transition-all duration-200 disabled:opacity-50"
              >
                <span className="material-icons-round text-xl">logout</span>
                <span>Đăng xuất</span>
              </button>
            </div>
          </div>
        </div>

        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {/* Welcome Section */}
          <div className="mb-8">
            <h2 className="text-3xl font-bold text-dark-blue mb-2">
              Xin chào! 👋
            </h2>
            <p className="text-dark-blue/70">
              Chào mừng bạn quay trở lại với KienLong Bank
            </p>
          </div>

          {/* Dashboard Cards */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            {dashboardCards.map((card, index) => (
              <div
                key={index}
                className="bg-white rounded-2xl shadow-lg p-6 hover:shadow-xl transition-all duration-300 transform hover:-translate-y-1"
              >
                <div className="flex items-start justify-between mb-4">
                  <div className={`w-12 h-12 rounded-xl bg-${card.color}-100 flex items-center justify-center`}>
                    <span className={`material-icons-round text-${card.color}-500 text-2xl`}>
                      {card.icon}
                    </span>
                  </div>
                </div>
                <h3 className="text-2xl font-bold text-dark-blue mb-1">{card.value}</h3>
                <p className="text-sm text-dark-blue/70 mb-1">{card.title}</p>
                <p className="text-xs text-dark-blue/50">{card.subtitle}</p>
              </div>
            ))}
          </div>

          {/* Quick Actions */}
          <div className="bg-white rounded-2xl shadow-lg p-8 mb-8">
            <h3 className="text-2xl font-bold text-dark-blue mb-6">
              Thao tác nhanh
            </h3>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              {quickActions.map((action, index) => (
                <Link
                  key={index}
                  to={action.link}
                  className="flex flex-col items-center justify-center p-6 bg-gradient-to-br from-blue-50 to-white border border-blue-100 rounded-xl hover:shadow-lg transition-all duration-300 group"
                >
                  <div className="w-16 h-16 rounded-full bg-blue-primary group-hover:bg-orange-primary flex items-center justify-center mb-3 transition-colors duration-300">
                    <span className="material-icons-round text-white text-3xl">
                      {action.icon}
                    </span>
                  </div>
                  <p className="text-sm font-semibold text-dark-blue text-center">
                    {action.title}
                  </p>
                </Link>
              ))}
            </div>
          </div>

          {/* Recent Transactions */}
          <div className="bg-white rounded-2xl shadow-lg p-8">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-2xl font-bold text-dark-blue">
                Giao dịch gần đây
              </h3>
              <Link
                to="#"
                className="text-blue-primary hover:text-dark-blue font-semibold text-sm flex items-center gap-1"
              >
                <span>Xem tất cả</span>
                <span className="material-icons-round text-sm">arrow_forward</span>
              </Link>
            </div>
            <div className="text-center py-12">
              <span className="material-icons-round text-dark-blue/30 text-6xl mb-4">receipt_long</span>
              <p className="text-dark-blue/70">
                Chưa có giao dịch nào
              </p>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  )
}

export default Dashboard
