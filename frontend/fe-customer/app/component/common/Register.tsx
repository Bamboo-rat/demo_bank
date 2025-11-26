import React, { useEffect, useState } from 'react'
import { Link } from 'react-router'
import {
  addressService,
  type Province,
  type District,
  type Ward,
  type AddressRequest
} from '~/service/addressService'
import {
  registrationService,
  type RegistrationSessionResponse,
  type CustomerResponse
} from '~/service/registrationService'

interface AddressFormData {
  province: string
  district: string
  ward: string
  street: string
}

const steps = [
  { title: 'Xác thực SĐT' },
  { title: 'Thông tin cá nhân' },
  { title: 'Thông tin CCCD' },
  { title: 'Hoàn tất' }
]

const initialFormState = {
  password: '',
  fullName: '',
  dateOfBirth: '',
  gender: '',
  nationality: 'Việt Nam',
  nationalId: '',
  issueDateNationalId: '',
  placeOfIssueNationalId: '',
  occupation: '',
  position: '',
  email: '',
  phoneNumber: ''
}

const initialAddressState: AddressFormData = {
  province: '',
  district: '',
  ward: '',
  street: ''
}

const Register = () => {
  const [currentStep, setCurrentStep] = useState(0)
  const [formData, setFormData] = useState(initialFormState)
  const [permanentAddress, setPermanentAddress] = useState<AddressFormData>({ ...initialAddressState })
  const [temporaryAddress, setTemporaryAddress] = useState<AddressFormData>({ ...initialAddressState })
  const [useSameAddress, setUseSameAddress] = useState(false)

  const [provinces, setProvinces] = useState<Province[]>([])
  const [districts, setDistricts] = useState<District[]>([])
  const [wards, setWards] = useState<Ward[]>([])
  const [tempDistricts, setTempDistricts] = useState<District[]>([])
  const [tempWards, setTempWards] = useState<Ward[]>([])

  const [loading, setLoading] = useState(false)
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [apiMessage, setApiMessage] = useState<string | null>(null)
  const [apiError, setApiError] = useState<string | null>(null)

  const [phoneNumber, setPhoneNumber] = useState('')
  const [otp, setOtp] = useState('')
  const [otpSent, setOtpSent] = useState(false)
  const [otpCountdown, setOtpCountdown] = useState<number | null>(null)

  const [session, setSession] = useState<RegistrationSessionResponse | null>(null)
  const [customerResult, setCustomerResult] = useState<CustomerResponse | null>(null)

  useEffect(() => {
    void loadProvinces()
  }, [])

  useEffect(() => {
    if (permanentAddress.province) {
      void loadDistricts(permanentAddress.province)
    } else {
      setDistricts([])
      setWards([])
      setPermanentAddress(prev => ({ ...prev, district: '', ward: '' }))
    }
  }, [permanentAddress.province])

  useEffect(() => {
    if (permanentAddress.district) {
      void loadWards(permanentAddress.district)
    } else {
      setWards([])
      setPermanentAddress(prev => ({ ...prev, ward: '' }))
    }
  }, [permanentAddress.district])

  useEffect(() => {
    if (temporaryAddress.province) {
      void loadTempDistricts(temporaryAddress.province)
    } else {
      setTempDistricts([])
      setTempWards([])
      setTemporaryAddress(prev => ({ ...prev, district: '', ward: '' }))
    }
  }, [temporaryAddress.province])

  useEffect(() => {
    if (temporaryAddress.district) {
      void loadTempWards(temporaryAddress.district)
    } else {
      setTempWards([])
      setTemporaryAddress(prev => ({ ...prev, ward: '' }))
    }
  }, [temporaryAddress.district])

  useEffect(() => {
    if (useSameAddress) {
      setTemporaryAddress({ ...permanentAddress })
    }
  }, [useSameAddress, permanentAddress])

  useEffect(() => {
    if (otpCountdown === null || otpCountdown <= 0) {
      return
    }
    const timer = setInterval(() => {
      setOtpCountdown(prev => {
        if (!prev || prev <= 1) {
          return null
        }
        return prev - 1
      })
    }, 1000)
    return () => clearInterval(timer)
  }, [otpCountdown])

  const loadProvinces = async () => {
    const data = await addressService.getProvinces()
    setProvinces(data)
  }

  const loadDistricts = async (provinceCode: string) => {
    const data = await addressService.getDistricts(provinceCode)
    setDistricts(data)
  }

  const loadWards = async (districtCode: string) => {
    const data = await addressService.getWards(districtCode)
    setWards(data)
  }

  const loadTempDistricts = async (provinceCode: string) => {
    const data = await addressService.getDistricts(provinceCode)
    setTempDistricts(data)
  }

  const loadTempWards = async (districtCode: string) => {
    const data = await addressService.getWards(districtCode)
    setTempWards(data)
  }

  const resetFeedback = () => {
    setApiMessage(null)
    setApiError(null)
  }

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }))
    }
  }

  const handlePermanentAddressChange = (field: keyof AddressFormData, value: string) => {
    setPermanentAddress(prev => ({ ...prev, [field]: value }))
    const key = `permanentAddress.${field}`
    if (errors[key]) {
      setErrors(prev => ({ ...prev, [key]: '' }))
    }
  }

  const handleTemporaryAddressChange = (field: keyof AddressFormData, value: string) => {
    setTemporaryAddress(prev => ({ ...prev, [field]: value }))
  }

  const buildAddressRequest = (
    addressData: AddressFormData,
    lookupDistricts: District[],
    lookupWards: Ward[]
  ): AddressRequest => ({
    street: addressData.street,
    ward: addressService.findNameByCode(lookupWards, addressData.ward) || addressData.ward,
    district: addressService.findNameByCode(lookupDistricts, addressData.district) || addressData.district,
    city: addressService.findNameByCode(provinces, addressData.province) || addressData.province,
    country: 'Việt Nam'
  })

  const validateStep = (stepIndex: number) => {
    const newErrors: Record<string, string> = {}
    if (stepIndex === 1) {
      if (!formData.fullName) newErrors.fullName = 'Họ và tên không được để trống'
      if (!formData.dateOfBirth) newErrors.dateOfBirth = 'Ngày sinh không được để trống'
      if (!formData.gender) newErrors.gender = 'Giới tính không được để trống'
      if (!formData.email) newErrors.email = 'Email không được để trống'
      else if (!/\S+@\S+\.\S+/.test(formData.email)) newErrors.email = 'Email không hợp lệ'
      if (!formData.password) newErrors.password = 'Vui lòng tạo mật khẩu'
      else if (formData.password.length < 8) newErrors.password = 'Mật khẩu tối thiểu 8 ký tự'
    }

    if (stepIndex === 2) {
      if (!formData.nationalId) newErrors.nationalId = 'Số CCCD không được để trống'
      if (!formData.issueDateNationalId) newErrors.issueDateNationalId = 'Ngày cấp không được bỏ trống'
      if (!formData.placeOfIssueNationalId) newErrors.placeOfIssueNationalId = 'Nơi cấp không được bỏ trống'
      if (!permanentAddress.province) newErrors['permanentAddress.province'] = 'Chọn Tỉnh/Thành phố'
      if (!permanentAddress.district) newErrors['permanentAddress.district'] = 'Chọn Quận/Huyện'
      if (!permanentAddress.ward) newErrors['permanentAddress.ward'] = 'Chọn Phường/Xã'
      if (!permanentAddress.street) newErrors['permanentAddress.street'] = 'Nhập địa chỉ cụ thể'
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSendOtp = async () => {
    resetFeedback()
    if (!phoneNumber) {
      setErrors({ phoneNumber: 'Vui lòng nhập số điện thoại' })
      return
    }
    try {
      setLoading(true)
      const result = await registrationService.startRegistration(phoneNumber)
      setOtpSent(true)
      setOtpCountdown(result.data.otpTtlSeconds)
      setApiMessage(result.message ?? 'Đã gửi mã OTP đến số điện thoại của bạn')
    } catch (error) {
      setApiError(error instanceof Error ? error.message : 'Không thể gửi OTP')
    } finally {
      setLoading(false)
    }
  }

  const handleVerifyOtp = async () => {
    resetFeedback()
    if (!otp) {
      setErrors({ otp: 'Vui lòng nhập mã OTP' })
      return
    }
    try {
      setLoading(true)
      const result = await registrationService.verifyOtp(phoneNumber, otp)
      setSession(result.data)
      setFormData(prev => ({ ...prev, phoneNumber }))
      setCurrentStep(1)
      setApiMessage(result.message ?? 'Xác thực OTP thành công')
      setErrors({})
    } catch (error) {
      setApiError(error instanceof Error ? error.message : 'OTP không chính xác')
    } finally {
      setLoading(false)
    }
  }

  const handleSaveProfile = async () => {
    if (!session) return
    if (!validateStep(1)) {
      return
    }
    resetFeedback()
    try {
      setLoading(true)
      const result = await registrationService.saveProfile({
        sessionId: session.sessionId,
        phoneNumber: session.phoneNumber,
        password: formData.password,
        fullName: formData.fullName,
        dateOfBirth: formData.dateOfBirth,
        gender: formData.gender,
        nationality: formData.nationality,
        email: formData.email,
        occupation: formData.occupation,
        position: formData.position
      })
      setSession(result.data)
      setCurrentStep(2)
      setApiMessage(result.message ?? 'Đã lưu thông tin cá nhân')
    } catch (error) {
      setApiError(error instanceof Error ? error.message : 'Không thể lưu thông tin cá nhân')
    } finally {
      setLoading(false)
    }
  }

  const handleSaveIdentity = async () => {
    if (!session) return
    if (!validateStep(2)) {
      return
    }
    resetFeedback()
    try {
      setLoading(true)
      const permanent = buildAddressRequest(permanentAddress, districts, wards)
      const temporary = useSameAddress
        ? permanent
        : temporaryAddress.province
          ? buildAddressRequest(temporaryAddress, tempDistricts, tempWards)
          : undefined

      const result = await registrationService.saveIdentity({
        sessionId: session.sessionId,
        phoneNumber: session.phoneNumber,
        nationalId: formData.nationalId,
        issueDateNationalId: formData.issueDateNationalId,
        placeOfIssueNationalId: formData.placeOfIssueNationalId,
        permanentAddress: permanent,
        temporaryAddress: temporary,
        documentFrontImage: null,
        documentBackImage: null,
        selfieImage: null
      })
      setSession(result.data)
      setCurrentStep(3)
      setApiMessage(result.message ?? 'Đã lưu thông tin định danh')
    } catch (error) {
      setApiError(error instanceof Error ? error.message : 'Không thể lưu thông tin định danh')
    } finally {
      setLoading(false)
    }
  }

  const handleComplete = async () => {
    if (!session) return
    resetFeedback()
    try {
      setLoading(true)
      const result = await registrationService.complete({
        sessionId: session.sessionId,
        phoneNumber: session.phoneNumber
      })
      setCustomerResult(result.data)
      setApiMessage(result.message ?? 'Đăng ký thành công!')
    } catch (error) {
      setApiError(error instanceof Error ? error.message : 'Không thể hoàn tất đăng ký')
    } finally {
      setLoading(false)
    }
  }

  const renderStepContent = () => {
    if (currentStep === 0) {
      return (
        <div className="space-y-4">
          <div className="text-center mb-4">
            <div className="w-12 h-12 bg-blue-50 rounded-full flex items-center justify-center mx-auto mb-2">
              <span className="material-icons-round text-blue-primary text-xl">phone_iphone</span>
            </div>
            <h3 className="text-base font-semibold text-dark-blue mb-1">Xác thực số điện thoại</h3>
            <p className="text-dark-blue/60 text-xs">Nhập số điện thoại để nhận mã OTP</p>
          </div>

          <div>
            <label className="block text-sm font-semibold text-dark-blue mb-1">
              Số điện thoại <span className="text-red-500">*</span>
            </label>
            <input
              type="tel"
              value={phoneNumber}
              onChange={event => {
                setPhoneNumber(event.target.value)
                if (errors.phoneNumber) {
                  setErrors(prev => ({ ...prev, phoneNumber: '' }))
                }
              }}
              className={`w-full px-3 py-2 bg-blue-50 border rounded-lg text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 text-sm ${
                errors.phoneNumber ? 'border-red-500' : 'border-blue-200'
              }`}
              placeholder="Nhập số điện thoại"
            />
            {errors.phoneNumber && <p className="text-red-500 text-xs mt-1">{errors.phoneNumber}</p>}
          </div>

          {otpSent && (
            <div>
              <label className="block text-sm font-semibold text-dark-blue mb-1">
                Mã OTP <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                value={otp}
                onChange={event => {
                  setOtp(event.target.value)
                  if (errors.otp) {
                    setErrors(prev => ({ ...prev, otp: '' }))
                  }
                }}
                className={`w-full px-3 py-2 bg-blue-50 border rounded-lg text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 text-sm ${
                  errors.otp ? 'border-red-500' : 'border-blue-200'
                }`}
                placeholder="Nhập mã OTP 6 số"
                maxLength={6}
              />
              {errors.otp && <p className="text-red-500 text-xs mt-1">{errors.otp}</p>}
              {otpCountdown !== null && (
                <p className="text-xs text-dark-blue/60 mt-1">
                  Mã hết hạn sau <span className="font-semibold text-orange-primary">{otpCountdown}s</span>
                </p>
              )}
            </div>
          )}

          <div className="flex flex-col sm:flex-row gap-2 pt-2">
            <button
              type="button"
              onClick={handleSendOtp}
              disabled={loading || (otpCountdown !== null && otpCountdown > 0)}
              className="flex-1 bg-blue-primary hover:bg-blue-600 text-white font-semibold py-2 px-3 rounded-lg transition-all duration-200 disabled:bg-blue-200 flex items-center justify-center gap-1 text-sm"
            >
              {loading ? (
                <>
                  <div className="w-3 h-3 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                  Đang xử lý...
                </>
              ) : (
                <>
                  <span className="material-icons-round text-base">send</span>
                  {otpSent ? 'Gửi lại OTP' : 'Gửi mã OTP'}
                </>
              )}
            </button>
            {otpSent && (
              <button
                type="button"
                onClick={handleVerifyOtp}
                disabled={loading}
                className="flex-1 bg-orange-primary hover:bg-orange-600 text-white font-semibold py-2 px-3 rounded-lg transition-all duration-200 disabled:bg-orange-200 flex items-center justify-center gap-1 text-sm"
              >
                <span className="material-icons-round text-base">verified</span>
                Xác thực
              </button>
            )}
          </div>
        </div>
      )
    }

    if (currentStep === 1) {
      return (
        <div className="space-y-4">
          <div className="text-center mb-4">
            <div className="w-12 h-12 bg-blue-50 rounded-full flex items-center justify-center mx-auto mb-2">
              <span className="material-icons-round text-blue-primary text-xl">person</span>
            </div>
            <h3 className="text-base font-semibold text-dark-blue mb-1">Thông tin cá nhân</h3>
            <p className="text-dark-blue/60 text-xs">Nhập thông tin cơ bản của bạn</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div>
              <label className="block text-sm font-semibold text-dark-blue mb-1">
                Họ và tên <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                name="fullName"
                value={formData.fullName}
                onChange={handleChange}
                className={`w-full px-3 py-2 bg-blue-50 border rounded-lg text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 text-sm ${
                  errors.fullName ? 'border-red-500' : 'border-blue-200'
                }`}
                placeholder="Nguyễn Văn A"
              />
              {errors.fullName && <p className="text-red-500 text-xs mt-1">{errors.fullName}</p>}
            </div>

            <div>
              <label className="block text-sm font-semibold text-dark-blue mb-1">
                Ngày sinh <span className="text-red-500">*</span>
              </label>
              <input
                type="date"
                name="dateOfBirth"
                value={formData.dateOfBirth}
                onChange={handleChange}
                className={`w-full px-3 py-2 bg-blue-50 border rounded-lg text-dark-blue focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 text-sm ${
                  errors.dateOfBirth ? 'border-red-500' : 'border-blue-200'
                }`}
              />
              {errors.dateOfBirth && <p className="text-red-500 text-xs mt-1">{errors.dateOfBirth}</p>}
            </div>

            <div>
              <label className="block text-sm font-semibold text-dark-blue mb-1">
                Giới tính <span className="text-red-500">*</span>
              </label>
              <select
                name="gender"
                value={formData.gender}
                onChange={handleChange}
                className={`w-full px-3 py-2 bg-blue-50 border rounded-lg text-dark-blue focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 text-sm ${
                  errors.gender ? 'border-red-500' : 'border-blue-200'
                }`}
              >
                <option value="">Chọn giới tính</option>
                <option value="MALE">Nam</option>
                <option value="FEMALE">Nữ</option>
                <option value="OTHER">Khác</option>
              </select>
              {errors.gender && <p className="text-red-500 text-xs mt-1">{errors.gender}</p>}
            </div>

            <div>
              <label className="block text-sm font-semibold text-dark-blue mb-1">
                Quốc tịch
              </label>
              <input
                type="text"
                name="nationality"
                value={formData.nationality}
                onChange={handleChange}
                className="w-full px-3 py-2 bg-blue-50 border border-blue-200 rounded-lg text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 text-sm"
                placeholder="Việt Nam"
              />
            </div>

            <div>
              <label className="block text-sm font-semibold text-dark-blue mb-1">
                Email <span className="text-red-500">*</span>
              </label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                className={`w-full px-3 py-2 bg-blue-50 border rounded-lg text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 text-sm ${
                  errors.email ? 'border-red-500' : 'border-blue-200'
                }`}
                placeholder="name@example.com"
              />
              {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email}</p>}
            </div>

            <div>
              <label className="block text-sm font-semibold text-dark-blue mb-1">
                Mật khẩu <span className="text-red-500">*</span>
              </label>
              <input
                type="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                className={`w-full px-3 py-2 bg-blue-50 border rounded-lg text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 text-sm ${
                  errors.password ? 'border-red-500' : 'border-blue-200'
                }`}
                placeholder="Tối thiểu 8 ký tự"
              />
              {errors.password && <p className="text-red-500 text-xs mt-1">{errors.password}</p>}
            </div>

            <div>
              <label className="block text-sm font-semibold text-dark-blue mb-1">
                Nghề nghiệp
              </label>
              <input
                type="text"
                name="occupation"
                value={formData.occupation}
                onChange={handleChange}
                className="w-full px-3 py-2 bg-blue-50 border border-blue-200 rounded-lg text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 text-sm"
                placeholder="Nhân viên văn phòng"
              />
            </div>

            <div>
              <label className="block text-sm font-semibold text-dark-blue mb-1">
                Chức vụ
              </label>
              <input
                type="text"
                name="position"
                value={formData.position}
                onChange={handleChange}
                className="w-full px-3 py-2 bg-blue-50 border border-blue-200 rounded-lg text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 text-sm"
                placeholder="Chuyên viên"
              />
            </div>
          </div>

          <div className="bg-blue-25 rounded-lg p-3 border border-blue-200">
            <div className="flex items-center gap-2">
              <span className="material-icons-round text-blue-primary text-base">verified_user</span>
              <div>
                <p className="text-xs font-semibold text-dark-blue">Số điện thoại đã xác thực</p>
                <p className="text-sm font-bold text-blue-primary">{session?.phoneNumber ?? phoneNumber}</p>
              </div>
            </div>
          </div>
        </div>
      )
    }

    if (currentStep === 2) {
      return (
        <div className="space-y-4">
          <div className="text-center mb-4">
            <div className="w-12 h-12 bg-blue-50 rounded-full flex items-center justify-center mx-auto mb-2">
              <span className="material-icons-round text-blue-primary text-xl">badge</span>
            </div>
            <h3 className="text-base font-semibold text-dark-blue mb-1">Thông tin CCCD</h3>
            <p className="text-dark-blue/60 text-xs">Nhập thông tin căn cước công dân</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-3 mb-4">
            <div>
              <label className="block text-sm font-semibold text-dark-blue mb-1">
                Số CCCD <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                name="nationalId"
                value={formData.nationalId}
                onChange={handleChange}
                className={`w-full px-3 py-2 bg-blue-50 border rounded-lg text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 text-sm ${
                  errors.nationalId ? 'border-red-500' : 'border-blue-200'
                }`}
                placeholder="012345678901"
              />
              {errors.nationalId && <p className="text-red-500 text-xs mt-1">{errors.nationalId}</p>}
            </div>

            <div>
              <label className="block text-sm font-semibold text-dark-blue mb-1">
                Ngày cấp <span className="text-red-500">*</span>
              </label>
              <input
                type="date"
                name="issueDateNationalId"
                value={formData.issueDateNationalId}
                onChange={handleChange}
                className={`w-full px-3 py-2 bg-blue-50 border rounded-lg text-dark-blue focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 text-sm ${
                  errors.issueDateNationalId ? 'border-red-500' : 'border-blue-200'
                }`}
              />
              {errors.issueDateNationalId && <p className="text-red-500 text-xs mt-1">{errors.issueDateNationalId}</p>}
            </div>

            <div className="md:col-span-2">
              <label className="block text-sm font-semibold text-dark-blue mb-1">
                Nơi cấp <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                name="placeOfIssueNationalId"
                value={formData.placeOfIssueNationalId}
                onChange={handleChange}
                className={`w-full px-3 py-2 bg-blue-50 border rounded-lg text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 text-sm ${
                  errors.placeOfIssueNationalId ? 'border-red-500' : 'border-blue-200'
                }`}
                placeholder="Công an thành phố Hà Nội"
              />
              {errors.placeOfIssueNationalId && <p className="text-red-500 text-xs mt-1">{errors.placeOfIssueNationalId}</p>}
            </div>
          </div>

          <div className="space-y-3">
            <div>
              <h3 className="text-sm font-semibold text-dark-blue mb-2">Địa chỉ thường trú <span className="text-red-500">*</span></h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-2">
                <div>
                  <label className="block text-xs font-semibold text-dark-blue mb-1">Tỉnh/Thành phố</label>
                  <select
                    value={permanentAddress.province}
                    onChange={event => handlePermanentAddressChange('province', event.target.value)}
                    className={`w-full px-3 py-2 bg-blue-50 border rounded-lg text-dark-blue focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 text-sm ${
                      errors['permanentAddress.province'] ? 'border-red-500' : 'border-blue-200'
                    }`}
                  >
                    <option value="">Chọn Tỉnh/Thành</option>
                    {provinces.map(province => (
                      <option key={province.code} value={province.code}>
                        {province.name}
                      </option>
                    ))}
                  </select>
                  {errors['permanentAddress.province'] && (
                    <p className="text-red-500 text-xs mt-1">{errors['permanentAddress.province']}</p>
                  )}
                </div>
                <div>
                  <label className="block text-xs font-semibold text-dark-blue mb-1">Quận/Huyện</label>
                  <select
                    value={permanentAddress.district}
                    onChange={event => handlePermanentAddressChange('district', event.target.value)}
                    disabled={!permanentAddress.province}
                    className={`w-full px-3 py-2 bg-blue-50 border rounded-lg text-dark-blue focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 text-sm ${
                      errors['permanentAddress.district'] ? 'border-red-500' : 'border-blue-200'
                    }`}
                  >
                    <option value="">Chọn Quận/Huyện</option>
                    {districts.map(district => (
                      <option key={district.code} value={district.code}>
                        {district.name}
                      </option>
                    ))}
                  </select>
                  {errors['permanentAddress.district'] && (
                    <p className="text-red-500 text-xs mt-1">{errors['permanentAddress.district']}</p>
                  )}
                </div>
                <div>
                  <label className="block text-xs font-semibold text-dark-blue mb-1">Phường/Xã</label>
                  <select
                    value={permanentAddress.ward}
                    onChange={event => handlePermanentAddressChange('ward', event.target.value)}
                    disabled={!permanentAddress.district}
                    className={`w-full px-3 py-2 bg-blue-50 border rounded-lg text-dark-blue focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 text-sm ${
                      errors['permanentAddress.ward'] ? 'border-red-500' : 'border-blue-200'
                    }`}
                  >
                    <option value="">Chọn Phường/Xã</option>
                    {wards.map(ward => (
                      <option key={ward.code} value={ward.code}>
                        {ward.name}
                      </option>
                    ))}
                  </select>
                  {errors['permanentAddress.ward'] && (
                    <p className="text-red-500 text-xs mt-1">{errors['permanentAddress.ward']}</p>
                  )}
                </div>
                <div>
                  <label className="block text-xs font-semibold text-dark-blue mb-1">Địa chỉ cụ thể</label>
                  <input
                    type="text"
                    value={permanentAddress.street}
                    onChange={event => handlePermanentAddressChange('street', event.target.value)}
                    className={`w-full px-3 py-2 bg-blue-50 border rounded-lg text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 text-sm ${
                      errors['permanentAddress.street'] ? 'border-red-500' : 'border-blue-200'
                    }`}
                    placeholder="Số nhà, tên đường..."
                  />
                  {errors['permanentAddress.street'] && (
                    <p className="text-red-500 text-xs mt-1">{errors['permanentAddress.street']}</p>
                  )}
                </div>
              </div>
            </div>

            <div>
              <div className="flex items-center justify-between mb-2">
                <h3 className="text-sm font-semibold text-dark-blue">Địa chỉ tạm trú</h3>
                <label className="flex items-center space-x-1 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={useSameAddress}
                    onChange={event => setUseSameAddress(event.target.checked)}
                    className="sr-only"
                  />
                  <div className={`w-4 h-4 border-2 rounded ${useSameAddress ? 'bg-blue-primary border-blue-primary' : 'border-blue-300'} transition-all duration-200 flex items-center justify-center`}>
                    {useSameAddress && <span className="material-icons-round text-white text-xs">check</span>}
                  </div>
                  <span className="text-xs text-dark-blue">Giống địa chỉ thường trú</span>
                </label>
              </div>
              {!useSameAddress && (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-2">
                  <div>
                    <label className="block text-xs font-semibold text-dark-blue mb-1">Tỉnh/Thành phố</label>
                    <select
                      value={temporaryAddress.province}
                      onChange={event => handleTemporaryAddressChange('province', event.target.value)}
                      className="w-full px-3 py-2 bg-blue-50 border border-blue-200 rounded-lg text-dark-blue focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 text-sm"
                    >
                      <option value="">Chọn Tỉnh/Thành</option>
                      {provinces.map(province => (
                        <option key={province.code} value={province.code}>
                          {province.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-dark-blue mb-1">Quận/Huyện</label>
                    <select
                      value={temporaryAddress.district}
                      onChange={event => handleTemporaryAddressChange('district', event.target.value)}
                      disabled={!temporaryAddress.province}
                      className="w-full px-3 py-2 bg-blue-50 border border-blue-200 rounded-lg text-dark-blue focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 text-sm"
                    >
                      <option value="">Chọn Quận/Huyện</option>
                      {tempDistricts.map(district => (
                        <option key={district.code} value={district.code}>
                          {district.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-dark-blue mb-1">Phường/Xã</label>
                    <select
                      value={temporaryAddress.ward}
                      onChange={event => handleTemporaryAddressChange('ward', event.target.value)}
                      disabled={!temporaryAddress.district}
                      className="w-full px-3 py-2 bg-blue-50 border border-blue-200 rounded-lg text-dark-blue focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 text-sm"
                    >
                      <option value="">Chọn Phường/Xã</option>
                      {tempWards.map(ward => (
                        <option key={ward.code} value={ward.code}>
                          {ward.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-dark-blue mb-1">Địa chỉ cụ thể</label>
                    <input
                      type="text"
                      value={temporaryAddress.street}
                      onChange={event => handleTemporaryAddressChange('street', event.target.value)}
                      className="w-full px-3 py-2 bg-blue-50 border border-blue-200 rounded-lg text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 text-sm"
                      placeholder="Số nhà, tên đường..."
                    />
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      )
    }

    return (
      <div className="space-y-4">
        <div className="text-center mb-4">
          <div className="w-12 h-12 bg-blue-50 rounded-full flex items-center justify-center mx-auto mb-2">
            <span className="material-icons-round text-blue-primary text-xl">task_alt</span>
          </div>
          <h3 className="text-base font-semibold text-dark-blue mb-1">Hoàn tất đăng ký</h3>
          <p className="text-dark-blue/60 text-xs">Kiểm tra thông tin đăng ký</p>
        </div>

        <div className="bg-blue-50 border border-blue-100 rounded-lg p-4 space-y-3">
          <h3 className="text-sm font-semibold text-dark-blue">Thông tin đăng ký</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-2 text-xs">
            <div>
              <p className="font-semibold text-dark-blue">Họ và tên</p>
              <p className="text-dark-blue/80">{formData.fullName}</p>
            </div>
            <div>
              <p className="font-semibold text-dark-blue">Số điện thoại</p>
              <p className="text-dark-blue/80">{session?.phoneNumber}</p>
            </div>
            <div>
              <p className="font-semibold text-dark-blue">Email</p>
              <p className="text-dark-blue/80">{formData.email}</p>
            </div>
            <div>
              <p className="font-semibold text-dark-blue">CCCD</p>
              <p className="text-dark-blue/80">{formData.nationalId}</p>
            </div>
            <div>
              <p className="font-semibold text-dark-blue">Ngày sinh</p>
              <p className="text-dark-blue/80">{formData.dateOfBirth}</p>
            </div>
            <div>
              <p className="font-semibold text-dark-blue">Nơi cấp CCCD</p>
              <p className="text-dark-blue/80">{formData.placeOfIssueNationalId}</p>
            </div>
          </div>
        </div>

        {customerResult && (
          <div className="bg-green-50 border border-green-200 rounded-lg p-4">
            <div className="flex items-center gap-2">
              <span className="material-icons-round text-green-500 text-lg">check_circle</span>
              <div>
                <h3 className="text-sm font-semibold text-green-700 mb-1">Đăng ký thành công!</h3>
                <p className="text-xs text-green-700/80">Mã KH: <span className="font-semibold">{customerResult.customerId}</span></p>
              </div>
            </div>
          </div>
        )}

        {!customerResult && (
          <div className="bg-blue-25 rounded-lg p-3 border border-blue-200">
            <div className="flex items-start gap-2">
              <span className="material-icons-round text-blue-primary text-sm">info</span>
              <div>
                <p className="text-xs font-semibold text-dark-blue mb-1">Thông tin quan trọng</p>
                <p className="text-xs text-dark-blue/60">
                  Kiểm tra kỹ thông tin trước khi hoàn tất. Tài khoản sẽ được kích hoạt trong 24h.
                </p>
              </div>
            </div>
          </div>
        )}
      </div>
    )
  }

  const renderActions = () => {
    if (currentStep === 0) {
      return (
        <div className="flex justify-between items-center">
          <Link to="/login" className="text-blue-primary font-semibold flex items-center gap-1 hover:text-blue-600 transition-colors text-sm">
            <span className="material-icons-round text-base">arrow_back</span>
            Quay lại đăng nhập
          </Link>
        </div>
      )
    }

    if (currentStep === 1) {
      return (
        <div className="flex flex-col sm:flex-row gap-2 justify-end">
          <button
            type="button"
            onClick={() => setCurrentStep(0)}
            className="px-4 py-2 rounded-lg border border-blue-200 text-dark-blue font-semibold hover:bg-blue-50 transition-all duration-200 flex items-center justify-center gap-1 text-sm"
          >
            <span className="material-icons-round text-base">arrow_back</span>
            Quay lại
          </button>
          <button
            type="button"
            onClick={handleSaveProfile}
            disabled={loading}
            className="px-4 py-2 rounded-lg bg-orange-primary hover:bg-orange-600 text-white font-semibold transition-all duration-200 disabled:bg-orange-200 flex items-center justify-center gap-1 text-sm"
          >
            {loading ? (
              <>
                <div className="w-3 h-3 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                Đang xử lý...
              </>
            ) : (
              <>
                Tiếp tục
                <span className="material-icons-round text-base">arrow_forward</span>
              </>
            )}
          </button>
        </div>
      )
    }

    if (currentStep === 2) {
      return (
        <div className="flex flex-col sm:flex-row gap-2 justify-end">
          <button
            type="button"
            onClick={() => setCurrentStep(1)}
            className="px-4 py-2 rounded-lg border border-blue-200 text-dark-blue font-semibold hover:bg-blue-50 transition-all duration-200 flex items-center justify-center gap-1 text-sm"
          >
            <span className="material-icons-round text-base">arrow_back</span>
            Quay lại
          </button>
          <button
            type="button"
            onClick={handleSaveIdentity}
            disabled={loading}
            className="px-4 py-2 rounded-lg bg-orange-primary hover:bg-orange-600 text-white font-semibold transition-all duration-200 disabled:bg-orange-200 flex items-center justify-center gap-1 text-sm"
          >
            {loading ? (
              <>
                <div className="w-3 h-3 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                Đang xử lý...
              </>
            ) : (
              <>
                Tiếp tục
                <span className="material-icons-round text-base">arrow_forward</span>
              </>
            )}
          </button>
        </div>
      )
    }

    return (
      <div className="flex flex-col sm:flex-row gap-2 justify-end">
        <button
          type="button"
          onClick={() => setCurrentStep(2)}
          className="px-4 py-2 rounded-lg border border-blue-200 text-dark-blue font-semibold hover:bg-blue-50 transition-all duration-200 flex items-center justify-center gap-1 text-sm"
        >
          <span className="material-icons-round text-base">arrow_back</span>
          Quay lại
        </button>
        <button
          type="button"
          onClick={handleComplete}
          disabled={loading || !!customerResult}
          className="px-4 py-2 rounded-lg bg-green-500 hover:bg-green-600 text-white font-semibold transition-all duration-200 disabled:bg-green-200 flex items-center justify-center gap-1 text-sm"
        >
          {loading ? (
            <>
              <div className="w-3 h-3 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
              Đang xử lý...
            </>
          ) : customerResult ? (
            <>
              <span className="material-icons-round text-base">check</span>
              Đã hoàn thành
            </>
          ) : (
            <>
              <span className="material-icons-round text-base">done_all</span>
              Hoàn tất
            </>
          )}
        </button>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-linear-to-br from-blue-25 to-blue-50 py-6 px-4">
      <div className="max-w-2xl mx-auto">
        <div className="text-center mb-6">
          <div className="w-16 h-16 rounded-xl border-3 border-dark-blue bg-white flex items-center justify-center shadow-md mx-auto mb-3">
            <img src="/app/assets/images/logo-kienlongbank.png" alt="KienLong Bank" className="w-10 h-10 object-contain" />
          </div>
          <h1 className="text-2xl font-bold text-dark-blue mb-1">Đăng ký tài khoản số</h1>
          <p className="text-dark-blue/70 text-sm">Trải nghiệm ngân hàng số hiện đại</p>
        </div>

        <div className="bg-white rounded-xl shadow-lg border border-blue-100 p-5 space-y-6">
          {/* Progress Steps */}
          <div className="grid grid-cols-4 gap-1">
            {steps.map((step, index) => {
              const isActive = index === currentStep
              const isCompleted = index < currentStep
              return (
                <div
                  key={step.title}
                  className={`text-center p-2 rounded-lg border transition-all duration-200 ${
                    isActive
                      ? 'border-orange-primary bg-orange-primary/10'
                      : isCompleted
                        ? 'border-green-400 bg-green-50'
                        : 'border-blue-100 bg-blue-25'
                  }`}
                >
                  <div className="flex flex-col items-center gap-1">
                    <div
                      className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-semibold transition-all duration-200 ${
                        isCompleted
                          ? 'bg-green-500 text-white'
                          : isActive
                            ? 'bg-orange-primary text-white'
                            : 'bg-white text-dark-blue border border-blue-200'
                      }`}
                    >
                      {isCompleted ? (
                        <span className="material-icons-round text-xs">check</span>
                      ) : (
                        index + 1
                      )}
                    </div>
                    <p className={`text-xs font-medium ${
                      isActive ? 'text-orange-primary' : 
                      isCompleted ? 'text-green-600' : 'text-dark-blue/60'
                    }`}>
                      {step.title}
                    </p>
                  </div>
                </div>
              )
            })}
          </div>

          {/* Feedback Messages */}
          {apiMessage && (
            <div className="p-3 border border-green-200 bg-green-50 text-green-700 rounded-lg text-xs flex items-center gap-2">
              <span className="material-icons-round text-sm">check_circle</span>
              {apiMessage}
            </div>
          )}
          {apiError && (
            <div className="p-3 border border-red-200 bg-red-50 text-red-700 rounded-lg text-xs flex items-center gap-2">
              <span className="material-icons-round text-sm">error</span>
              {apiError}
            </div>
          )}

          {/* Step Content */}
          <div className="space-y-4">
            {renderStepContent()}
          </div>

          {/* Action Buttons */}
          <div className="pt-4 border-t border-blue-100">
            {renderActions()}
          </div>
        </div>

        <div className="text-center mt-6 text-xs text-dark-blue/50">
          © 2025 KienLong Bank. Secure • Reliable • Innovative
        </div>
      </div>
    </div>
  )
}

export default Register