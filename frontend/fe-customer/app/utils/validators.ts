// Validation utilities for forms

export const validators = {
  // Số điện thoại Việt Nam: 10 số, bắt đầu 0
  phoneNumber: (value: string): string | null => {
    if (!value) return 'Số điện thoại không được để trống'
    const cleaned = value.replace(/\s+/g, '')
    if (!/^0\d{9}$/.test(cleaned)) {
      return 'Số điện thoại phải có 10 số và bắt đầu bằng 0'
    }
    return null
  },

  // Email format
  email: (value: string): string | null => {
    if (!value) return 'Email không được để trống'
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
      return 'Email không hợp lệ'
    }
    return null
  },

  // CCCD/CMND: 9 số (CMND cũ) hoặc 12 số (CCCD mới)
  nationalId: (value: string): string | null => {
    if (!value) return 'Số CCCD/CMND không được để trống'
    const cleaned = value.replace(/\s+/g, '')
    if (!/^(\d{9}|\d{12})$/.test(cleaned)) {
      return 'CCCD phải có 12 số hoặc CMND có 9 số'
    }
    return null
  },

  // Password: 8-15 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt
  password: (value: string): string | null => {
    if (!value) return 'Mật khẩu không được để trống'
    if (value.length < 8) {
      return 'Mật khẩu tối thiểu 8 ký tự'
    }
    if (value.length > 15) {
      return 'Mật khẩu tối đa 15 ký tự'
    }
    if (!/[A-Z]/.test(value)) {
      return 'Mật khẩu phải có ít nhất 1 chữ in hoa'
    }
    if (!/[a-z]/.test(value)) {
      return 'Mật khẩu phải có ít nhất 1 chữ thường'
    }
    if (!/\d/.test(value)) {
      return 'Mật khẩu phải có ít nhất 1 chữ số'
    }
    if (!/[!@#$%^&*(),.?":{}|<>]/.test(value)) {
      return 'Mật khẩu phải có ít nhất 1 ký tự đặc biệt'
    }
    return null
  },

  // Ngày sinh: phải >= 18 tuổi
  dateOfBirth: (value: string): string | null => {
    if (!value) return 'Ngày sinh không được để trống'
    const birthDate = new Date(value)
    const today = new Date()
    const age = today.getFullYear() - birthDate.getFullYear()
    const monthDiff = today.getMonth() - birthDate.getMonth()
    const dayDiff = today.getDate() - birthDate.getDate()
    
    const actualAge = monthDiff < 0 || (monthDiff === 0 && dayDiff < 0) ? age - 1 : age
    
    if (actualAge < 18) {
      return 'Bạn phải đủ 18 tuổi để đăng ký'
    }
    if (actualAge > 100) {
      return 'Ngày sinh không hợp lệ'
    }
    return null
  },

  // OTP: 6 chữ số
  otp: (value: string): string | null => {
    if (!value) return 'Mã OTP không được để trống'
    if (!/^\d{6}$/.test(value)) {
      return 'Mã OTP phải có 6 chữ số'
    }
    return null
  },

  // Required field
  required: (value: string, fieldName: string = 'Trường này'): string | null => {
    if (!value || value.trim() === '') {
      return `${fieldName} không được để trống`
    }
    return null
  },

  // Full name: chỉ chữ cái và khoảng trắng
  fullName: (value: string): string | null => {
    if (!value) return 'Họ và tên không được để trống'
    if (value.trim().length < 2) {
      return 'Họ và tên quá ngắn'
    }
    if (!/^[a-zA-ZÀ-ỹ\s]+$/.test(value)) {
      return 'Họ và tên chỉ được chứa chữ cái'
    }
    return null
  }
}

// Format phone number: 0123456789 -> 0123 456 789
export const formatPhoneNumber = (value: string): string => {
  const cleaned = value.replace(/\D/g, '')
  const match = cleaned.match(/^(\d{4})(\d{3})(\d{3})$/)
  if (match) {
    return `${match[1]} ${match[2]} ${match[3]}`
  }
  return cleaned
}

// Format national ID: 123456789012 -> 123 456 789 012
export const formatNationalId = (value: string): string => {
  const cleaned = value.replace(/\D/g, '')
  if (cleaned.length === 9) {
    const match = cleaned.match(/^(\d{3})(\d{3})(\d{3})$/)
    if (match) return `${match[1]} ${match[2]} ${match[3]}`
  }
  if (cleaned.length === 12) {
    const match = cleaned.match(/^(\d{3})(\d{3})(\d{3})(\d{3})$/)
    if (match) return `${match[1]} ${match[2]} ${match[3]} ${match[4]}`
  }
  return cleaned
}
