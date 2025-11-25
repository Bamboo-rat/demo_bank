import React, { useState, useEffect } from 'react'
import { Link } from 'react-router'
import{ 
  addressService, 
  type Province, 
  type District, 
  type Ward, 
  type CustomerRegisterRequest,
  type AddressRequest 
} from '~/service/addressService'

interface AddressFormData {
  province: string
  district: string
  ward: string
  street: string
}

const Register = () => {
  const [formData, setFormData] = useState({
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
    phoneNumber: '',
  })

  // State for addresses
  const [permanentAddress, setPermanentAddress] = useState<AddressFormData>({
    province: '',
    district: '',
    ward: '',
    street: ''
  })

  const [temporaryAddress, setTemporaryAddress] = useState<AddressFormData>({
    province: '',
    district: '',
    ward: '',
    street: ''
  })

  const [useSameAddress, setUseSameAddress] = useState(false)
  const [provinces, setProvinces] = useState<Province[]>([])
  const [districts, setDistricts] = useState<District[]>([])
  const [wards, setWards] = useState<Ward[]>([])
  const [tempDistricts, setTempDistricts] = useState<District[]>([])
  const [tempWards, setTempWards] = useState<Ward[]>([])

  const [loading, setLoading] = useState(false)
  const [errors, setErrors] = useState<Record<string, string>>({})

  useEffect(() => {
    fetchProvinces()
  }, [])

  useEffect(() => {
    if (permanentAddress.province) {
      fetchDistricts(permanentAddress.province)
    } else {
      setDistricts([])
      setWards([])
    }
  }, [permanentAddress.province])

  useEffect(() => {
    if (permanentAddress.district) {
      fetchWards(permanentAddress.district)
    } else {
      setWards([])
    }
  }, [permanentAddress.district])

  useEffect(() => {
    if (temporaryAddress.province) {
      fetchTempDistricts(temporaryAddress.province)
    } else {
      setTempDistricts([])
      setTempWards([])
    }
  }, [temporaryAddress.province])


  useEffect(() => {
    if (temporaryAddress.district) {
      fetchTempWards(temporaryAddress.district)
    } else {
      setTempWards([])
    }
  }, [temporaryAddress.district])


  useEffect(() => {
    if (useSameAddress) {
      setTemporaryAddress(permanentAddress)
    }
  }, [useSameAddress, permanentAddress])

  const fetchProvinces = async () => {
    const provincesData = await addressService.getProvinces()
    setProvinces(provincesData)
  }

  const fetchDistricts = async (provinceCode: string) => {
    const districtsData = await addressService.getDistricts(provinceCode)
    setDistricts(districtsData)
    setWards([])
    setPermanentAddress(prev => ({ ...prev, district: '', ward: '' }))
  }

  const fetchWards = async (districtCode: string) => {
    const wardsData = await addressService.getWards(districtCode)
    setWards(wardsData)
    setPermanentAddress(prev => ({ ...prev, ward: '' }))
  }

  const fetchTempDistricts = async (provinceCode: string) => {
    const districtsData = await addressService.getDistricts(provinceCode)
    setTempDistricts(districtsData)
    setTempWards([])
    setTemporaryAddress(prev => ({ ...prev, district: '', ward: '' }))
  }

  const fetchTempWards = async (districtCode: string) => {
    const wardsData = await addressService.getWards(districtCode)
    setTempWards(wardsData)
    setTemporaryAddress(prev => ({ ...prev, ward: '' }))
  }

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: value
    }))
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }))
    }
  }

  const handlePermanentAddressChange = (field: keyof AddressFormData, value: string) => {
    setPermanentAddress(prev => ({
      ...prev,
      [field]: value
    }))
  }

  const handleTemporaryAddressChange = (field: keyof AddressFormData, value: string) => {
    setTemporaryAddress(prev => ({
      ...prev,
      [field]: value
    }))
  }

  const buildAddressRequest = (addressData: AddressFormData): AddressRequest => {
    return {
      street: addressData.street,
      ward: addressService.findNameByCode(wards, addressData.ward) || addressData.ward,
      district: addressService.findNameByCode(districts, addressData.district) || addressData.district,
      city: addressService.findNameByCode(provinces, addressData.province) || addressData.province,
      country: 'Việt Nam'
    }
  }

  const validateForm = () => {
    const newErrors: Record<string, string> = {}

    if (!formData.password) newErrors.password = 'Mật khẩu không được để trống'
    else if (formData.password.length < 8) newErrors.password = 'Mật khẩu phải có ít nhất 8 ký tự'

    if (!formData.fullName) newErrors.fullName = 'Họ và tên không được để trống'
    if (!formData.dateOfBirth) newErrors.dateOfBirth = 'Ngày sinh không được để trống'
    if (!formData.gender) newErrors.gender = 'Giới tính không được để trống'
    if (!formData.nationalId) newErrors.nationalId = 'Số CCCD không được để trống'
    if (!formData.issueDateNationalId) newErrors.issueDateNationalId = 'Ngày cấp CCCD không được để trống'
    if (!formData.placeOfIssueNationalId) newErrors.placeOfIssueNationalId = 'Nơi cấp CCCD không được để trống'
    if (!formData.email) newErrors.email = 'Email không được để trống'
    else if (!/\S+@\S+\.\S+/.test(formData.email)) newErrors.email = 'Email không hợp lệ'
    if (!formData.phoneNumber) newErrors.phoneNumber = 'Số điện thoại không được để trống'

    // Address validations
    if (!permanentAddress.province) newErrors['permanentAddress.province'] = 'Tỉnh/Thành phố không được để trống'
    if (!permanentAddress.district) newErrors['permanentAddress.district'] = 'Quận/Huyện không được để trống'
    if (!permanentAddress.ward) newErrors['permanentAddress.ward'] = 'Phường/Xã không được để trống'
    if (!permanentAddress.street) newErrors['permanentAddress.street'] = 'Địa chỉ cụ thể không được để trống'

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!validateForm()) {
      return
    }

    setLoading(true)
    try {

      const submitData: CustomerRegisterRequest = {
        ...formData,
        permanentAddress: buildAddressRequest(permanentAddress),
        temporaryAddress: useSameAddress ? undefined : buildAddressRequest(temporaryAddress)
      }

      console.log('Submit data:', submitData)
      // TODO: Gọi API đăng ký ở đây
      // await registerCustomer(submitData)
      
      alert('Đăng ký thành công!')
    } catch (error) {
      console.error('Error registering:', error)
      alert('Có lỗi xảy ra khi đăng ký')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-linear-to-br from-blue-25 to-blue-50 py-8 px-4">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="text-center mb-8">
          <div className="w-16 h-16 rounded-xl border-4 border-dark-blue bg-white flex items-center justify-center shadow-lg mx-auto mb-4">
            <img 
              src="/app/assets/images/logo-kienlongbank.png" 
              alt="KienLong Bank" 
              className="w-10 h-10 object-contain"
            />
          </div>
          <h1 className="text-3xl font-bold text-dark-blue mb-2">Đăng ký Tài khoản</h1>
          <p className="text-lg text-dark-blue/70">KienLong Bank - Digital Banking</p>
        </div>

        {/* Registration Form */}
        <div className="bg-white rounded-2xl shadow-xl border border-blue-100 p-8">
          <form onSubmit={handleSubmit} className="space-y-8">
            {/* Personal Information Section */}
            <div>
              <h2 className="text-xl font-semibold text-dark-blue mb-6 pb-2 border-b border-blue-100">
                Thông tin cá nhân
              </h2>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Full Name */}
                <div>
                  <label className="block text-sm font-semibold text-dark-blue mb-2">
                    Họ và tên <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    name="fullName"
                    value={formData.fullName}
                    onChange={handleInputChange}
                    className={`w-full px-4 py-3 bg-blue-50 border rounded-xl text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 ${
                      errors.fullName ? 'border-red-500' : 'border-blue-200'
                    }`}
                    placeholder="Nhập họ và tên đầy đủ"
                  />
                  {errors.fullName && <p className="text-red-500 text-sm mt-1">{errors.fullName}</p>}
                </div>

                {/* Date of Birth */}
                <div>
                  <label className="block text-sm font-semibold text-dark-blue mb-2">
                    Ngày sinh <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="date"
                    name="dateOfBirth"
                    value={formData.dateOfBirth}
                    onChange={handleInputChange}
                    className={`w-full px-4 py-3 bg-blue-50 border rounded-xl text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 ${
                      errors.dateOfBirth ? 'border-red-500' : 'border-blue-200'
                    }`}
                  />
                  {errors.dateOfBirth && <p className="text-red-500 text-sm mt-1">{errors.dateOfBirth}</p>}
                </div>

                {/* Gender */}
                <div>
                  <label className="block text-sm font-semibold text-dark-blue mb-2">
                    Giới tính <span className="text-red-500">*</span>
                  </label>
                  <select
                    name="gender"
                    value={formData.gender}
                    onChange={handleInputChange}
                    className={`w-full px-4 py-3 bg-blue-50 border rounded-xl text-dark-blue focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 ${
                      errors.gender ? 'border-red-500' : 'border-blue-200'
                    }`}
                  >
                    <option value="">Chọn giới tính</option>
                    <option value="MALE">Nam</option>
                    <option value="FEMALE">Nữ</option>
                    <option value="OTHER">Khác</option>
                  </select>
                  {errors.gender && <p className="text-red-500 text-sm mt-1">{errors.gender}</p>}
                </div>

                {/* Nationality */}
                <div>
                  <label className="block text-sm font-semibold text-dark-blue mb-2">
                    Quốc tịch <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    name="nationality"
                    value={formData.nationality}
                    onChange={handleInputChange}
                    className="w-full px-4 py-3 bg-blue-50 border border-blue-200 rounded-xl text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200"
                    placeholder="Quốc tịch"
                  />
                </div>
              </div>
            </div>

            {/* ID Information Section */}
            <div>
              <h2 className="text-xl font-semibold text-dark-blue mb-6 pb-2 border-b border-blue-100">
                Thông tin căn cước công dân
              </h2>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* National ID */}
                <div>
                  <label className="block text-sm font-semibold text-dark-blue mb-2">
                    Số CCCD <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    name="nationalId"
                    value={formData.nationalId}
                    onChange={handleInputChange}
                    className={`w-full px-4 py-3 bg-blue-50 border rounded-xl text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 ${
                      errors.nationalId ? 'border-red-500' : 'border-blue-200'
                    }`}
                    placeholder="Nhập số căn cước công dân"
                  />
                  {errors.nationalId && <p className="text-red-500 text-sm mt-1">{errors.nationalId}</p>}
                </div>

                {/* Issue Date */}
                <div>
                  <label className="block text-sm font-semibold text-dark-blue mb-2">
                    Ngày cấp <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="date"
                    name="issueDateNationalId"
                    value={formData.issueDateNationalId}
                    onChange={handleInputChange}
                    className={`w-full px-4 py-3 bg-blue-50 border rounded-xl text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 ${
                      errors.issueDateNationalId ? 'border-red-500' : 'border-blue-200'
                    }`}
                  />
                  {errors.issueDateNationalId && <p className="text-red-500 text-sm mt-1">{errors.issueDateNationalId}</p>}
                </div>

                {/* Place of Issue */}
                <div className="md:col-span-2">
                  <label className="block text-sm font-semibold text-dark-blue mb-2">
                    Nơi cấp <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    name="placeOfIssueNationalId"
                    value={formData.placeOfIssueNationalId}
                    onChange={handleInputChange}
                    className={`w-full px-4 py-3 bg-blue-50 border rounded-xl text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 ${
                      errors.placeOfIssueNationalId ? 'border-red-500' : 'border-blue-200'
                    }`}
                    placeholder="Nhập nơi cấp CCCD"
                  />
                  {errors.placeOfIssueNationalId && <p className="text-red-500 text-sm mt-1">{errors.placeOfIssueNationalId}</p>}
                </div>
              </div>
            </div>

            {/* Contact Information Section */}
            <div>
              <h2 className="text-xl font-semibold text-dark-blue mb-6 pb-2 border-b border-blue-100">
                Thông tin liên hệ
              </h2>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Email */}
                <div>
                  <label className="block text-sm font-semibold text-dark-blue mb-2">
                    Email <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleInputChange}
                    className={`w-full px-4 py-3 bg-blue-50 border rounded-xl text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 ${
                      errors.email ? 'border-red-500' : 'border-blue-200'
                    }`}
                    placeholder="Nhập địa chỉ email"
                  />
                  {errors.email && <p className="text-red-500 text-sm mt-1">{errors.email}</p>}
                </div>

                {/* Phone Number */}
                <div>
                  <label className="block text-sm font-semibold text-dark-blue mb-2">
                    Số điện thoại <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="tel"
                    name="phoneNumber"
                    value={formData.phoneNumber}
                    onChange={handleInputChange}
                    className={`w-full px-4 py-3 bg-blue-50 border rounded-xl text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 ${
                      errors.phoneNumber ? 'border-red-500' : 'border-blue-200'
                    }`}
                    placeholder="Nhập số điện thoại"
                  />
                  {errors.phoneNumber && <p className="text-red-500 text-sm mt-1">{errors.phoneNumber}</p>}
                </div>

                {/* Occupation */}
                <div>
                  <label className="block text-sm font-semibold text-dark-blue mb-2">
                    Nghề nghiệp
                  </label>
                  <input
                    type="text"
                    name="occupation"
                    value={formData.occupation}
                    onChange={handleInputChange}
                    className="w-full px-4 py-3 bg-blue-50 border border-blue-200 rounded-xl text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200"
                    placeholder="Nhập nghề nghiệp"
                  />
                </div>

                {/* Position */}
                <div>
                  <label className="block text-sm font-semibold text-dark-blue mb-2">
                    Chức vụ
                  </label>
                  <input
                    type="text"
                    name="position"
                    value={formData.position}
                    onChange={handleInputChange}
                    className="w-full px-4 py-3 bg-blue-50 border border-blue-200 rounded-xl text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200"
                    placeholder="Nhập chức vụ"
                  />
                </div>
              </div>
            </div>

            {/* Address Information Section */}
            <div>
              <h2 className="text-xl font-semibold text-dark-blue mb-6 pb-2 border-b border-blue-100">
                Thông tin địa chỉ
              </h2>

              {/* Permanent Address */}
              <div className="mb-8">
                <h3 className="text-lg font-semibold text-dark-blue mb-4">Địa chỉ thường trú <span className="text-red-500">*</span></h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {/* Province */}
                  <div>
                    <label className="block text-sm font-semibold text-dark-blue mb-2">Tỉnh/Thành phố</label>
                    <select
                      value={permanentAddress.province}
                      onChange={(e) => handlePermanentAddressChange('province', e.target.value)}
                      className={`w-full px-4 py-3 bg-blue-50 border rounded-xl text-dark-blue focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 ${
                        errors['permanentAddress.province'] ? 'border-red-500' : 'border-blue-200'
                      }`}
                    >
                      <option value="">Chọn Tỉnh/Thành phố</option>
                      {provinces.map(province => (
                        <option key={province.code} value={province.code}>
                          {province.name}
                        </option>
                      ))}
                    </select>
                    {errors['permanentAddress.province'] && <p className="text-red-500 text-sm mt-1">{errors['permanentAddress.province']}</p>}
                  </div>

                  {/* District */}
                  <div>
                    <label className="block text-sm font-semibold text-dark-blue mb-2">Quận/Huyện</label>
                    <select
                      value={permanentAddress.district}
                      onChange={(e) => handlePermanentAddressChange('district', e.target.value)}
                      className={`w-full px-4 py-3 bg-blue-50 border rounded-xl text-dark-blue focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 ${
                        errors['permanentAddress.district'] ? 'border-red-500' : 'border-blue-200'
                      }`}
                      disabled={!permanentAddress.province}
                    >
                      <option value="">Chọn Quận/Huyện</option>
                      {districts.map(district => (
                        <option key={district.code} value={district.code}>
                          {district.name}
                        </option>
                      ))}
                    </select>
                    {errors['permanentAddress.district'] && <p className="text-red-500 text-sm mt-1">{errors['permanentAddress.district']}</p>}
                  </div>

                  {/* Ward */}
                  <div>
                    <label className="block text-sm font-semibold text-dark-blue mb-2">Phường/Xã</label>
                    <select
                      value={permanentAddress.ward}
                      onChange={(e) => handlePermanentAddressChange('ward', e.target.value)}
                      className={`w-full px-4 py-3 bg-blue-50 border rounded-xl text-dark-blue focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 ${
                        errors['permanentAddress.ward'] ? 'border-red-500' : 'border-blue-200'
                      }`}
                      disabled={!permanentAddress.district}
                    >
                      <option value="">Chọn Phường/Xã</option>
                      {wards.map(ward => (
                        <option key={ward.code} value={ward.code}>
                          {ward.name}
                        </option>
                      ))}
                    </select>
                    {errors['permanentAddress.ward'] && <p className="text-red-500 text-sm mt-1">{errors['permanentAddress.ward']}</p>}
                  </div>

                  {/* Street */}
                  <div>
                    <label className="block text-sm font-semibold text-dark-blue mb-2">Địa chỉ cụ thể</label>
                    <input
                      type="text"
                      value={permanentAddress.street}
                      onChange={(e) => handlePermanentAddressChange('street', e.target.value)}
                      className={`w-full px-4 py-3 bg-blue-50 border rounded-xl text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 ${
                        errors['permanentAddress.street'] ? 'border-red-500' : 'border-blue-200'
                      }`}
                      placeholder="Số nhà, tên đường..."
                    />
                    {errors['permanentAddress.street'] && <p className="text-red-500 text-sm mt-1">{errors['permanentAddress.street']}</p>}
                  </div>
                </div>
              </div>

              {/* Temporary Address */}
              <div>
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-lg font-semibold text-dark-blue">Địa chỉ tạm trú</h3>
                  <label className="flex items-center space-x-2 cursor-pointer">
                    <input
                      type="checkbox"
                      checked={useSameAddress}
                      onChange={(e) => setUseSameAddress(e.target.checked)}
                      className="sr-only"
                    />
                    <div className={`w-5 h-5 border-2 rounded ${useSameAddress ? 'bg-blue-primary border-blue-primary' : 'border-blue-300'} transition-all duration-200 flex items-center justify-center`}>
                      {useSameAddress && (
                        <span className="material-icons-round text-white text-sm">
                          check
                        </span>
                      )}
                    </div>
                    <span className="text-sm text-dark-blue">Giống địa chỉ thường trú</span>
                  </label>
                </div>

                {!useSameAddress && (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {/* Temp Province */}
                    <div>
                      <label className="block text-sm font-semibold text-dark-blue mb-2">Tỉnh/Thành phố</label>
                      <select
                        value={temporaryAddress.province}
                        onChange={(e) => handleTemporaryAddressChange('province', e.target.value)}
                        className="w-full px-4 py-3 bg-blue-50 border border-blue-200 rounded-xl text-dark-blue focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200"
                      >
                        <option value="">Chọn Tỉnh/Thành phố</option>
                        {provinces.map(province => (
                          <option key={province.code} value={province.code}>
                            {province.name}
                          </option>
                        ))}
                      </select>
                    </div>

                    {/* Temp District */}
                    <div>
                      <label className="block text-sm font-semibold text-dark-blue mb-2">Quận/Huyện</label>
                      <select
                        value={temporaryAddress.district}
                        onChange={(e) => handleTemporaryAddressChange('district', e.target.value)}
                        className="w-full px-4 py-3 bg-blue-50 border border-blue-200 rounded-xl text-dark-blue focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200"
                        disabled={!temporaryAddress.province}
                      >
                        <option value="">Chọn Quận/Huyện</option>
                        {tempDistricts.map(district => (
                          <option key={district.code} value={district.code}>
                            {district.name}
                          </option>
                        ))}
                      </select>
                    </div>

                    {/* Temp Ward */}
                    <div>
                      <label className="block text-sm font-semibold text-dark-blue mb-2">Phường/Xã</label>
                      <select
                        value={temporaryAddress.ward}
                        onChange={(e) => handleTemporaryAddressChange('ward', e.target.value)}
                        className="w-full px-4 py-3 bg-blue-50 border border-blue-200 rounded-xl text-dark-blue focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200"
                        disabled={!temporaryAddress.district}
                      >
                        <option value="">Chọn Phường/Xã</option>
                        {tempWards.map(ward => (
                          <option key={ward.code} value={ward.code}>
                            {ward.name}
                          </option>
                        ))}
                      </select>
                    </div>

                    {/* Temp Street */}
                    <div>
                      <label className="block text-sm font-semibold text-dark-blue mb-2">Địa chỉ cụ thể</label>
                      <input
                        type="text"
                        value={temporaryAddress.street}
                        onChange={(e) => handleTemporaryAddressChange('street', e.target.value)}
                        className="w-full px-4 py-3 bg-blue-50 border border-blue-200 rounded-xl text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200"
                        placeholder="Số nhà, tên đường..."
                      />
                    </div>
                  </div>
                )}
              </div>
            </div>

            {/* Account Information Section */}
            <div>
              <h2 className="text-xl font-semibold text-dark-blue mb-6 pb-2 border-b border-blue-100">
                Thông tin tài khoản
              </h2>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Password */}
                <div>
                  <label className="block text-sm font-semibold text-dark-blue mb-2">
                    Mật khẩu <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="password"
                    name="password"
                    value={formData.password}
                    onChange={handleInputChange}
                    className={`w-full px-4 py-3 bg-blue-50 border rounded-xl text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200 ${
                      errors.password ? 'border-red-500' : 'border-blue-200'
                    }`}
                    placeholder="Nhập mật khẩu (ít nhất 8 ký tự)"
                  />
                  {errors.password && <p className="text-red-500 text-sm mt-1">{errors.password}</p>}
                </div>

                {/* Confirm Password */}
                <div>
                  <label className="block text-sm font-semibold text-dark-blue mb-2">
                    Xác nhận mật khẩu <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="password"
                    name="confirmPassword"
                    onChange={handleInputChange}
                    className="w-full px-4 py-3 bg-blue-50 border border-blue-200 rounded-xl text-dark-blue placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-primary transition-all duration-200"
                    placeholder="Nhập lại mật khẩu"
                  />
                </div>
              </div>
            </div>

            {/* Submit Button */}
            <div className="flex flex-col sm:flex-row gap-4 justify-between items-center pt-6 border-t border-blue-100">
              <Link 
                to="/login" 
                className="text-blue-primary hover:text-dark-blue transition-colors duration-200 font-semibold flex items-center gap-2"
              >
                <span className="material-icons-round">arrow_back</span>
                Quay lại đăng nhập
              </Link>
              
              <button
                type="submit"
                disabled={loading}
                className="bg-orange-primary hover:bg-orange-dark disabled:bg-gray-400 text-white font-semibold py-4 px-8 rounded-xl shadow-lg hover:shadow-xl transition-all duration-200 transform hover:scale-[1.02] focus:outline-none focus:ring-2 focus:ring-orange-primary focus:ring-opacity-50 min-w-48"
              >
                {loading ? (
                  <span className="flex items-center justify-center gap-2">
                    <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                    Đang xử lý...
                  </span>
                ) : (
                  <span className="flex items-center justify-center gap-2">
                    <span>Đăng ký</span>
                    <span className="material-icons-round">how_to_reg</span>
                  </span>
                )}
              </button>
            </div>
          </form>
        </div>

        {/* Footer */}
        <div className="text-center mt-8">
          <p className="text-sm text-dark-blue/50">
            © 2025 KienLong Bank. Secure • Reliable • Innovative
          </p>
        </div>
      </div>
    </div>
  )
}

export default Register