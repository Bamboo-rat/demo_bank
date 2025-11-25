import React, { useState } from 'react'
import { Link } from 'react-router'

const Login = () => {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [rememberMe, setRememberMe] = useState(false)
  const [showPassword, setShowPassword] = useState(false)

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    console.log('Đăng nhập:', { username, password, rememberMe })
  }

  return (
    <div className="min-h-screen bg-linear-to-br from-blue-25 to-blue-50 flex items-center justify-center p-4">
      <div className="max-w-md w-full">
        {/* Logo và Header */}
        <div className="text-center mb-8">
          <div className="w-20 h-20 rounded-2xl border-4 border-dark-blue bg-white flex items-center justify-center shadow-lg mx-auto mb-4">
            <img 
              src="/app/assets/images/logo-kienlongbank.png" 
              alt="KienLong Bank" 
              className="w-12 h-12 object-contain"
            />
          </div>
          <h1 className="text-3xl font-bold text-dark-blue mb-2">KienLong Bank</h1>
          <p className="text-lg text-dark-blue/70">Ngân hàng trực tuyến</p>
        </div>

        {/* Login Form */}
        <div className="bg-white rounded-2xl shadow-xl border border-blue-100 p-8">
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Username Field */}
            <div>
              <label htmlFor="username" className="block text-sm font-semibold text-dark-blue mb-2">
                Tên đăng nhập
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <span className="material-icons-round text-blue-primary text-xl">
                    person
                  </span>
                </div>
                <input
                  id="username"
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  className="w-full pl-12 pr-4 py-4 bg-blue-50 border border-blue-200 rounded-xl text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary focus:border-transparent transition-all duration-200"
                  placeholder="Nhập tên đăng nhập"
                  required
                />
              </div>
            </div>

            {/* Password Field */}
            <div>
              <label htmlFor="password" className="block text-sm font-semibold text-dark-blue mb-2">
                Mật khẩu
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <span className="material-icons-round text-blue-primary text-xl">
                    lock
                  </span>
                </div>
                <input
                  id="password"
                  type={showPassword ? "text" : "password"}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full pl-12 pr-12 py-4 bg-blue-50 border border-blue-200 rounded-xl text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary focus:border-transparent transition-all duration-200"
                  placeholder="Nhập mật khẩu"
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute inset-y-0 right-0 pr-3 flex items-center"
                >
                  <span className="material-icons-round text-blue-primary text-xl hover:text-dark-blue transition-colors duration-200">
                    {showPassword ? 'visibility_off' : 'visibility'}
                  </span>
                </button>
              </div>
            </div>

            {/* Remember Me & Forgot Password */}
            <div className="flex items-center justify-between">
              <label className="flex items-center space-x-2 cursor-pointer">
                <div className="relative">
                  <input
                    type="checkbox"
                    checked={rememberMe}
                    onChange={(e) => setRememberMe(e.target.checked)}
                    className="sr-only"
                  />
                  <div className={`w-5 h-5 border-2 rounded ${rememberMe ? 'bg-blue-primary border-blue-primary' : 'border-blue-300'} transition-all duration-200 flex items-center justify-center`}>
                    {rememberMe && (
                      <span className="material-icons-round text-white text-sm">
                        check
                      </span>
                    )}
                  </div>
                </div>
                <span className="text-sm text-dark-blue">Ghi nhớ đăng nhập</span>
              </label>
              
              <Link 
                to="/forgot-password" 
                className="text-sm text-blue-primary hover:text-dark-blue transition-colors duration-200 font-medium"
              >
                Quên mật khẩu?
              </Link>
            </div>

            {/* Login Button */}
            <button
              type="submit"
              className="w-full bg-orange-primary hover:bg-orange-dark text-white font-semibold py-4 px-6 rounded-xl shadow-lg hover:shadow-xl transition-all duration-200 transform hover:scale-[1.02] focus:outline-none focus:ring-2 focus:ring-orange-primary focus:ring-opacity-50"
            >
              <span className="flex items-center justify-center gap-2">
                <span>Đăng nhập</span>
                <span className="material-icons-round text-xl">arrow_forward</span>
              </span>
            </button>
          </form>
          <div>
            <p className="text-center text-sm text-dark-blue/70 mt-6">
              Chưa có tài khoản?{' '}
              <Link 
                to="/register" 
                className="text-blue-primary hover:text-dark-blue font-medium transition-colors duration-200"
              >
                Đăng ký ngay
              </Link>
            </p>
          </div>
        </div>

        {/* Footer Links */}
        <div className="text-center mt-6 space-y-3">
          <div className="flex justify-center space-x-6 text-sm">
            <Link to="/support" className="text-dark-blue/70 hover:text-dark-blue transition-colors duration-200">
              Hỗ trợ
            </Link>
            <Link to="/privacy" className="text-dark-blue/70 hover:text-dark-blue transition-colors duration-200">
              Bảo mật
            </Link>
            <Link to="/terms" className="text-dark-blue/70 hover:text-dark-blue transition-colors duration-200">
              Điều khoản
            </Link>
          </div>
          
          <div className="pt-4 border-t border-blue-200">
            <p className="text-xs text-dark-blue/50">
              © 2025 KienLong Bank. Secure • Reliable • Innovative
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Login