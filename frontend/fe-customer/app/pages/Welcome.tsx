import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import banner1 from '~/assets/images/banner1.png'
import banner2 from '~/assets/images/banner2.png'
import banner3 from '~/assets/images/banner3.png'
import banner4 from '~/assets/images/banner4.png'
import { authService } from '~/service/authService'

interface ServiceCard {
  title: string
  icon: string
  link: string
  description: string
}

const Welcome = () => {
  const navigate = useNavigate()
  const [activeTab, setActiveTab] = useState<string>('home')

  // Check if user is already logged in
  React.useEffect(() => {
    if (authService.isAuthenticated()) {
      navigate('/dashboard')
    }
  }, [])

  const serviceCards: ServiceCard[] = [
    {
      title: 'Tra cứu',
      icon: 'search',
      link: '#',
      description: 'Tra cứu thông tin tài khoản, giao dịch'
    },
    {
      title: 'Lãi suất - Tỷ giá',
      icon: 'currency_exchange',
      link: '#interest-rate',
      description: 'Xem lãi suất tiền gửi và tỷ giá ngoại tệ'
    },
    {
      title: 'Biểu phí - Biểu mẫu',
      icon: 'description',
      link: '#',
      description: 'Tải biểu phí dịch vụ và biểu mẫu'
    },
    {
      title: 'Hướng dẫn sử dụng',
      icon: 'help_center',
      link: '#',
      description: 'Hướng dẫn sử dụng dịch vụ ngân hàng'
    },
    {
      title: 'Câu hỏi thường gặp',
      icon: 'question_answer',
      link: '#',
      description: 'Giải đáp các thắc mắc thường gặp'
    },
    {
      title: 'Công cụ tính toán',
      icon: 'calculate',
      link: '#calculator',
      description: 'Công cụ tính lãi suất, khoản vay'
    }
  ]

  const handleCardClick = (link: string) => {
    if (link.startsWith('#')) {
      const section = document.querySelector(link)
      section?.scrollIntoView({ behavior: 'smooth' })
    } else {
      navigate(link)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-white">
      {/* Navigation Bar */}
      <nav className="bg-white shadow-md sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center space-x-3">
              <div className="w-12 h-12 rounded-lg bg-blue-primary flex items-center justify-center">
                <span className="material-icons-round text-white text-2xl">account_balance</span>
              </div>
              <div>
                <h1 className="text-xl font-bold text-dark-blue">KienLong Bank</h1>
                <p className="text-xs text-blue-primary">Ngân hàng số</p>
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <Link
                to="/login"
                className="px-6 py-2 text-blue-primary font-semibold hover:bg-blue-50 rounded-lg transition-all duration-200"
              >
                Đăng nhập
              </Link>
              <Link
                to="/register"
                className="px-6 py-2 bg-orange-primary hover:bg-orange-dark text-white font-semibold rounded-lg shadow-md hover:shadow-lg transition-all duration-200 transform hover:scale-105"
              >
                Đăng ký
              </Link>
            </div>
          </div>
        </div>
      </nav>

      {/* Hero Section with Banners */}
      <section className="py-12 px-4">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-8">
            <h2 className="text-4xl font-bold text-dark-blue mb-4">
              Chào mừng đến với KienLong Bank
            </h2>
            <p className="text-lg text-dark-blue/70">
              Giải pháp ngân hàng số hiện đại, an toàn và tiện lợi
            </p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <img src={banner1} alt="Banner 1" className="rounded-xl shadow-lg hover:shadow-2xl transition-shadow duration-300 w-full" />
            <img src={banner2} alt="Banner 2" className="rounded-xl shadow-lg hover:shadow-2xl transition-shadow duration-300 w-full" />
            <img src={banner3} alt="Banner 3" className="rounded-xl shadow-lg hover:shadow-2xl transition-shadow duration-300 w-full" />
            <img src={banner4} alt="Banner 4" className="rounded-xl shadow-lg hover:shadow-2xl transition-shadow duration-300 w-full" />
          </div>
        </div>
      </section>

      {/* Service Cards Section */}
      <section className="py-12 px-4 bg-white">
        <div className="max-w-7xl mx-auto">
          <h3 className="text-3xl font-bold text-dark-blue text-center mb-8">
            Dịch vụ của chúng tôi
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {serviceCards.map((card, index) => (
              <div
                key={index}
                onClick={() => handleCardClick(card.link)}
                className="bg-gradient-to-br from-blue-50 to-white border border-blue-100 rounded-2xl p-6 hover:shadow-xl transition-all duration-300 cursor-pointer transform hover:-translate-y-2 group"
              >
                <div className="flex items-start space-x-4">
                  <div className="w-14 h-14 rounded-xl bg-blue-primary group-hover:bg-orange-primary flex items-center justify-center transition-colors duration-300 flex-shrink-0">
                    <span className="material-icons-round text-white text-3xl">
                      {card.icon}
                    </span>
                  </div>
                  <div className="flex-1">
                    <h4 className="text-lg font-semibold text-dark-blue mb-2 group-hover:text-blue-primary transition-colors duration-200">
                      {card.title}
                    </h4>
                    <p className="text-sm text-dark-blue/70">
                      {card.description}
                    </p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Interest Rate Section */}
      <section id="interest-rate" className="py-12 px-4 bg-gradient-to-br from-blue-50 to-white">
        <div className="max-w-7xl mx-auto">
          <div className="bg-white rounded-2xl shadow-xl p-8">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-3xl font-bold text-dark-blue">
                Lãi suất tiền gửi
              </h3>
              <p className="text-sm text-dark-blue/70">
                Áp dụng từ ngày 11/11/2025
              </p>
            </div>
            
            <div className="mb-8">
              <h4 className="text-xl font-semibold text-blue-primary mb-4">
                Lãi suất tiền gửi không kỳ hạn
              </h4>
              <div className="overflow-x-auto">
                <table className="w-full border-collapse">
                  <thead>
                    <tr className="bg-blue-primary text-white">
                      <th className="py-3 px-4 text-left rounded-tl-lg">Loại tiền gửi</th>
                      <th className="py-3 px-4 text-right rounded-tr-lg">Lãi suất (%/năm)</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr className="border-b border-blue-100 hover:bg-blue-50 transition-colors">
                      <td className="py-3 px-4">Tiền gửi không kỳ hạn</td>
                      <td className="py-3 px-4 text-right font-semibold text-orange-primary">0.50</td>
                    </tr>
                    <tr className="hover:bg-blue-50 transition-colors">
                      <td className="py-3 px-4">Tiền gửi tiết kiệm</td>
                      <td className="py-3 px-4 text-right font-semibold text-orange-primary">0.50</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <div>
              <h4 className="text-xl font-semibold text-blue-primary mb-4">
                Lãi suất tiền gửi có kỳ hạn
              </h4>
              <div className="overflow-x-auto">
                <table className="w-full border-collapse text-sm">
                  <thead>
                    <tr className="bg-blue-primary text-white">
                      <th className="py-3 px-4 text-left rounded-tl-lg">Kỳ hạn</th>
                      <th className="py-3 px-4 text-right">Lãnh lãi cuối kỳ</th>
                      <th className="py-3 px-4 text-right">Lãnh lãi định kỳ (1 tháng)</th>
                      <th className="py-3 px-4 text-right rounded-tr-lg">Lãnh lãi đầu kỳ</th>
                    </tr>
                  </thead>
                  <tbody>
                    {[
                      { term: '1 tháng', end: '3.50', monthly: '-', early: '3.49' },
                      { term: '3 tháng', end: '3.50', monthly: '3.49', early: '3.47' },
                      { term: '6 tháng', end: '5.20', monthly: '5.14', early: '5.07' },
                      { term: '12 tháng', end: '5.30', monthly: '5.18', early: '5.03' },
                      { term: '24 tháng', end: '5.25', monthly: '5.00', early: '4.75' },
                      { term: '36 tháng', end: '5.25', monthly: '4.89', early: '4.54' }
                    ].map((rate, index) => (
                      <tr key={index} className="border-b border-blue-100 hover:bg-blue-50 transition-colors">
                        <td className="py-3 px-4 font-medium">{rate.term}</td>
                        <td className="py-3 px-4 text-right font-semibold text-orange-primary">{rate.end}</td>
                        <td className="py-3 px-4 text-right font-semibold text-blue-primary">{rate.monthly}</td>
                        <td className="py-3 px-4 text-right font-semibold text-dark-blue">{rate.early}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Calculator Section */}
      <section id="calculator" className="py-12 px-4">
        <div className="max-w-4xl mx-auto">
          <div className="bg-white rounded-2xl shadow-xl p-8">
            <h3 className="text-3xl font-bold text-dark-blue mb-6 text-center">
              Công cụ tính toán khoản vay
            </h3>
            <p className="text-center text-dark-blue/70 mb-8">
              Hỗ trợ khách hàng ước tính số tiền có thể vay và khoản phải trả trong tương lai
            </p>
            <div className="bg-blue-50 rounded-xl p-6 text-center">
              <span className="material-icons-round text-blue-primary text-6xl mb-4">calculate</span>
              <p className="text-dark-blue mb-4">
                Vui lòng đăng nhập để sử dụng công cụ tính toán
              </p>
              <Link
                to="/login"
                className="inline-flex items-center gap-2 px-6 py-3 bg-orange-primary hover:bg-orange-dark text-white font-semibold rounded-lg shadow-md hover:shadow-lg transition-all duration-200"
              >
                <span>Đăng nhập ngay</span>
                <span className="material-icons-round">arrow_forward</span>
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-dark-blue text-white py-12 px-4">
        <div className="max-w-7xl mx-auto">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div>
              <h4 className="text-xl font-bold mb-4">KienLong Bank</h4>
              <p className="text-white/70 mb-4">
                Ngân hàng số hiện đại, an toàn và tiện lợi
              </p>
              <div className="flex space-x-4">
                <a href="#" className="w-10 h-10 bg-white/10 rounded-lg flex items-center justify-center hover:bg-orange-primary transition-colors duration-200">
                  <span className="material-icons-round">facebook</span>
                </a>
                <a href="#" className="w-10 h-10 bg-white/10 rounded-lg flex items-center justify-center hover:bg-orange-primary transition-colors duration-200">
                  <span className="material-icons-round">phone</span>
                </a>
              </div>
            </div>
            <div>
              <h5 className="font-semibold mb-4">Liên hệ</h5>
              <ul className="space-y-2 text-white/70">
                <li className="flex items-center gap-2">
                  <span className="material-icons-round text-sm">phone</span>
                  <span>Hotline: 19006929</span>
                </li>
                <li className="flex items-center gap-2">
                  <span className="material-icons-round text-sm">email</span>
                  <span>support@kienlongbank.com</span>
                </li>
              </ul>
            </div>
            <div>
              <h5 className="font-semibold mb-4">Giờ làm việc</h5>
              <p className="text-white/70">
                Thứ 2 - Thứ 6: 8:00 - 17:00<br />
                Thứ 7: 8:00 - 12:00
              </p>
            </div>
          </div>
          <div className="border-t border-white/10 mt-8 pt-8 text-center text-white/50">
            <p>&copy; 2025 KienLong Bank. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  )
}

export default Welcome
