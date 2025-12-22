import { useState, useEffect } from 'react'
import { Link } from 'react-router'
import logo from '~/assets/images/logo-kienlongbank.png'

const banners = [
  {
    id: 1,
    title: 'Ngân hàng số hiện đại',
    subtitle: 'Trải nghiệm ngân hàng mới với công nghệ tiên tiến',
    description: 'Giao dịch nhanh chóng, an toàn và tiện lợi mọi lúc mọi nơi',
    bgColor: 'from-blue-600 to-blue-800',
    icon: 'account_balance'
  },
  {
    id: 2,
    title: 'Vay vốn lãi suất ưu đãi',
    subtitle: 'Giải pháp tài chính linh hoạt cho mọi nhu cầu',
    description: 'Duyệt nhanh trong 24h, không cần thế chấp, lãi suất từ 6.9%/năm',
    bgColor: 'from-purple-600 to-purple-800',
    icon: 'handshake'
  },
  {
    id: 3,
    title: 'Tiết kiệm sinh lời cao',
    subtitle: 'Đầu tư thông minh, an toàn tuyệt đối',
    description: 'Lãi suất hấp dẫn lên đến 8.5%/năm, rút trước hạn không mất phí',
    bgColor: 'from-green-600 to-green-800',
    icon: 'savings'
  },
  {
    id: 4,
    title: 'Chuyển tiền miễn phí',
    subtitle: 'Giao dịch nhanh chóng 24/7',
    description: 'Chuyển khoản nội bộ và liên ngân hàng hoàn toàn miễn phí',
    bgColor: 'from-orange-600 to-orange-800',
    icon: 'swap_horiz'
  },
  {
    id: 5,
    title: 'Bảo mật tuyệt đối',
    subtitle: 'Công nghệ bảo mật chuẩn quốc tế',
    description: 'Mã hóa đa lớp, xác thực sinh trắc học, bảo vệ tài khoản 24/7',
    bgColor: 'from-red-600 to-red-800',
    icon: 'security'
  }
]

const features = [
  {
    icon: 'speed',
    title: 'Giao dịch nhanh',
    description: 'Xử lý giao dịch trong vài giây'
  },
  {
    icon: 'verified_user',
    title: 'Bảo mật cao',
    description: 'Công nghệ mã hóa tiên tiến'
  },
  {
    icon: 'support_agent',
    title: 'Hỗ trợ 24/7',
    description: 'Tư vấn nhiệt tình mọi lúc'
  },
  {
    icon: 'mobile_friendly',
    title: 'Đa nền tảng',
    description: 'Sử dụng trên mọi thiết bị'
  }
]

export default function Welcome() {
  const [currentSlide, setCurrentSlide] = useState(0)

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentSlide((prev) => (prev + 1) % banners.length)
    }, 5000)

    return () => clearInterval(timer)
  }, [])

  const goToSlide = (index: number) => {
    setCurrentSlide(index)
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-md sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center gap-3">
              <img src={logo} alt="KienLongBank" className="h-10 w-auto" />
              <h1 className="text-2xl font-bold text-blue-600">KienLongBank</h1>
            </div>
            
            <div className="flex items-center gap-4">
              <Link
                to="/login"
                className="px-5 py-2 text-blue-600 font-medium hover:text-blue-700 transition-colors"
              >
                Đăng nhập
              </Link>
              <Link
                to="/register"
                className="px-5 py-2 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 transition-colors"
              >
                Đăng ký
              </Link>
            </div>
          </div>
        </div>
      </header>

      {/* Hero Banner Carousel */}
      <div className="relative h-[500px] overflow-hidden">
        {banners.map((banner, index) => (
          <div
            key={banner.id}
            className={`absolute inset-0 transition-all duration-1000 ease-in-out ${
              index === currentSlide
                ? 'opacity-100 translate-x-0'
                : index < currentSlide
                ? 'opacity-0 -translate-x-full'
                : 'opacity-0 translate-x-full'
            }`}
          >
            <div className={`h-full bg-linear-to-r ${banner.bgColor} flex items-center`}>
              <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 w-full">
                <div className="flex items-center justify-between">
                  <div className="max-w-2xl text-white">
                    <h2 className="text-5xl font-bold mb-4">{banner.title}</h2>
                    <p className="text-2xl mb-4 text-white/90">{banner.subtitle}</p>
                    <p className="text-lg mb-8 text-white/80">{banner.description}</p>
                    <div className="flex gap-4">
                      <Link
                        to="/register"
                        className="px-8 py-3 bg-white text-blue-600 font-semibold rounded-lg hover:bg-blue-50 transition-colors"
                      >
                        Đăng ký ngay
                      </Link>
                      <Link
                        to="/login"
                        className="px-8 py-3 bg-white/20 backdrop-blur text-white font-semibold rounded-lg hover:bg-white/30 transition-colors"
                      >
                        Tìm hiểu thêm
                      </Link>
                    </div>
                  </div>
                  <div className="hidden lg:block">
                    <div className="w-64 h-64 bg-white/10 backdrop-blur rounded-full flex items-center justify-center">
                      <span className="material-icons-round text-white" style={{ fontSize: '128px' }}>
                        {banner.icon}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        ))}

        {/* Carousel Indicators */}
        <div className="absolute bottom-8 left-1/2 -translate-x-1/2 flex gap-3">
          {banners.map((_, index) => (
            <button
              key={index}
              onClick={() => goToSlide(index)}
              className={`h-2 rounded-full transition-all duration-300 ${
                index === currentSlide ? 'w-8 bg-white' : 'w-2 bg-white/50'
              }`}
            />
          ))}
        </div>
      </div>

      {/* About Section */}
      <section className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-gray-900 mb-4">
              Về KienLongBank
            </h2>
            <p className="text-xl text-gray-600 max-w-3xl mx-auto">
              Ngân hàng số hàng đầu Việt Nam với sứ mệnh mang đến trải nghiệm tài chính 
              hiện đại, an toàn và tiện lợi cho mọi khách hàng
            </p>
          </div>

          <div className="grid md:grid-cols-2 gap-12 items-center">
            <div>
              <h3 className="text-3xl font-bold text-gray-900 mb-6">
                Tại sao chọn KienLongBank?
              </h3>
              <div className="space-y-4">
                <div className="flex items-start gap-4">
                  <span className="material-icons-round text-blue-600 text-3xl">check_circle</span>
                  <div>
                    <h4 className="font-semibold text-lg text-gray-900">Công nghệ hiện đại</h4>
                    <p className="text-gray-600">Ứng dụng công nghệ AI và Blockchain trong mọi giao dịch</p>
                  </div>
                </div>
                <div className="flex items-start gap-4">
                  <span className="material-icons-round text-blue-600 text-3xl">check_circle</span>
                  <div>
                    <h4 className="font-semibold text-lg text-gray-900">Lãi suất cạnh tranh</h4>
                    <p className="text-gray-600">Mức lãi suất hấp dẫn nhất thị trường cho tiết kiệm và vay vốn</p>
                  </div>
                </div>
                <div className="flex items-start gap-4">
                  <span className="material-icons-round text-blue-600 text-3xl">check_circle</span>
                  <div>
                    <h4 className="font-semibold text-lg text-gray-900">Bảo mật tối đa</h4>
                    <p className="text-gray-600">Hệ thống bảo mật đa lớp, được kiểm định bởi các tổ chức quốc tế</p>
                  </div>
                </div>
                <div className="flex items-start gap-4">
                  <span className="material-icons-round text-blue-600 text-3xl">check_circle</span>
                  <div>
                    <h4 className="font-semibold text-lg text-gray-900">Dịch vụ 24/7</h4>
                    <p className="text-gray-600">Hỗ trợ khách hàng mọi lúc, mọi nơi qua đa kênh</p>
                  </div>
                </div>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-6">
              <div className="bg-blue-50 rounded-2xl p-8 text-center">
                <div className="text-4xl font-bold text-blue-600 mb-2">5M+</div>
                <div className="text-gray-600">Khách hàng</div>
              </div>
              <div className="bg-green-50 rounded-2xl p-8 text-center">
                <div className="text-4xl font-bold text-green-600 mb-2">500+</div>
                <div className="text-gray-600">Chi nhánh</div>
              </div>
              <div className="bg-purple-50 rounded-2xl p-8 text-center">
                <div className="text-4xl font-bold text-purple-600 mb-2">15+</div>
                <div className="text-gray-600">Năm kinh nghiệm</div>
              </div>
              <div className="bg-orange-50 rounded-2xl p-8 text-center">
                <div className="text-4xl font-bold text-orange-600 mb-2">99.9%</div>
                <div className="text-gray-600">Uptime</div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-gray-900 mb-4">
              Ưu điểm vượt trội
            </h2>
            <p className="text-xl text-gray-600">
              Trải nghiệm những tính năng hiện đại nhất
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-8">
            {features.map((feature) => (
              <div
                key={feature.title}
                className="bg-white rounded-2xl p-8 shadow-lg hover:shadow-xl transition-shadow"
              >
                <div className="w-16 h-16 bg-blue-100 rounded-xl flex items-center justify-center mb-6">
                  <span className="material-icons-round text-blue-600 text-3xl">
                    {feature.icon}
                  </span>
                </div>
                <h3 className="text-xl font-bold text-gray-900 mb-3">{feature.title}</h3>
                <p className="text-gray-600">{feature.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 bg-linear-to-r from-blue-600 to-blue-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-4xl font-bold text-white mb-6">
            Sẵn sàng bắt đầu?
          </h2>
          <p className="text-xl text-blue-100 mb-8 max-w-2xl mx-auto">
            Tạo tài khoản ngay hôm nay và trải nghiệm dịch vụ ngân hàng số hiện đại nhất
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link
              to="/register"
              className="px-8 py-4 bg-white text-blue-600 font-bold rounded-lg hover:bg-blue-50 transition-colors text-lg"
            >
              Đăng ký miễn phí
            </Link>
            <Link
              to="/login"
              className="px-8 py-4 bg-blue-500 text-white font-bold rounded-lg hover:bg-blue-400 transition-colors text-lg"
            >
              Đăng nhập ngay
            </Link>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-900 text-gray-300 py-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid md:grid-cols-4 gap-8 mb-8">
            <div>
              <div className="flex items-center gap-2 mb-4">
                <img src={logo} alt="KienLongBank" className="h-8 w-auto" />
                <span className="text-xl font-bold text-white">KienLongBank</span>
              </div>
              <p className="text-sm">
                Ngân hàng số hiện đại, an toàn và tiện lợi
              </p>
            </div>
            <div>
              <h4 className="font-semibold text-white mb-4">Sản phẩm</h4>
              <ul className="space-y-2 text-sm">
                <li>Tài khoản thanh toán</li>
                <li>Tiết kiệm online</li>
                <li>Vay vốn</li>
                <li>Thẻ tín dụng</li>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold text-white mb-4">Hỗ trợ</h4>
              <ul className="space-y-2 text-sm">
                <li>Câu hỏi thường gặp</li>
                <li>Liên hệ</li>
                <li>Bảo mật</li>
                <li>Điều khoản</li>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold text-white mb-4">Liên hệ</h4>
              <ul className="space-y-2 text-sm">
                <li>Hotline: 1900 1234</li>
                <li>Email: support@KienLongBank.vn</li>
                <li>Giờ làm việc: 24/7</li>
              </ul>
            </div>
          </div>
          <div className="border-t border-gray-800 pt-8 text-center text-sm">
            <p>&copy; 2025 KienLongBank. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  )
}