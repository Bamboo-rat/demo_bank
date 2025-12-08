import React, { useState, useEffect } from 'react'
import Layout from '~/component/layout/Layout'
import { customerService, type CustomerProfile } from '~/service/customerService'

const Profile = () => {
  const [profile, setProfile] = useState<CustomerProfile | null>(null)
  const [loading, setLoading] = useState(true)
  const [isEditing, setIsEditing] = useState(false)
  const [formData, setFormData] = useState<Partial<CustomerProfile>>({})
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    fetchProfile()
  }, [])

  const fetchProfile = async () => {
    try {
      setLoading(true)
      const data = await customerService.getMyProfile()
      setProfile(data)
      setFormData(data)
    } catch (error) {
      console.error('Failed to fetch profile:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleEdit = () => {
    setIsEditing(true)
    setFormData(profile || {})
  }

  const handleCancel = () => {
    setIsEditing(false)
    setFormData(profile || {})
  }

  const handleSave = async () => {
    try {
      setSaving(true)
      await customerService.updateProfile(formData)
      await fetchProfile()
      setIsEditing(false)
    } catch (error) {
      console.error('Failed to update profile:', error)
      alert('Cập nhật thông tin thất bại. Vui lòng thử lại.')
    } finally {
      setSaving(false)
    }
  }

  const handleChange = (field: keyof CustomerProfile, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }))
  }

  const handleAddressChange = (addressType: 'permanentAddress' | 'temporaryAddress', field: string, value: string) => {
    setFormData(prev => ({
      ...prev,
      [addressType]: {
        ...(prev[addressType] || {}),
        [field]: value
      }
    }))
  }

  if (loading) {
    return (
      <Layout>
        <div className="flex items-center justify-center min-h-screen">
          <div className="text-center">
            <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-blue-primary mx-auto mb-4"></div>
            <p className="text-dark-blue/70">Đang tải thông tin...</p>
          </div>
        </div>
      </Layout>
    )
  }

  return (
    <Layout>
      <div className="min-h-screen bg-linear-to-br from-blue-50 to-white py-8">
        <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
          {/* Header */}
          <div className="bg-white rounded-2xl shadow-lg p-8 mb-6">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-6">
                <div className="w-24 h-24 rounded-full bg-linear-to-br from-blue-500 to-blue-400 flex items-center justify-center shadow-lg">
                  <span className="text-white font-bold text-3xl">
                    {profile?.fullName?.substring(0, 2).toUpperCase() || 'KH'}
                  </span>
                </div>
                <div>
                  <h1 className="text-3xl font-bold text-dark-blue mb-2">
                    {profile?.fullName || 'Khách hàng'}
                  </h1>
                  <div className="flex items-center gap-4 text-dark-blue/70">
                    <div className="flex items-center gap-2">
                      <span className="material-icons-round text-blue-primary text-lg">phone</span>
                      <span>{profile?.phoneNumber || 'N/A'}</span>
                    </div>
                    {profile?.email && (
                      <div className="flex items-center gap-2">
                        <span className="material-icons-round text-blue-primary text-lg">email</span>
                        <span>{profile.email}</span>
                      </div>
                    )}
                  </div>
                </div>
              </div>
              {!isEditing && (
                <button
                  onClick={handleEdit}
                  className="flex items-center gap-2 px-6 py-3 bg-blue-primary hover:bg-blue-600 text-white font-semibold rounded-xl transition-all duration-200 shadow-md hover:shadow-lg"
                >
                  <span className="material-icons-round text-xl">edit</span>
                  <span>Chỉnh sửa</span>
                </button>
              )}
            </div>
          </div>

          {/* Profile Information */}
          <div className="bg-white rounded-2xl shadow-lg p-8">
            <h2 className="text-2xl font-bold text-dark-blue mb-6 flex items-center gap-3">
              <span className="material-icons-round text-blue-primary text-3xl">person</span>
              Thông tin cá nhân
            </h2>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Họ và tên */}
              <div>
                <label className="block text-sm font-semibold text-dark-blue mb-2">
                  Họ và tên <span className="text-red-500">*</span>
                </label>
                {isEditing ? (
                  <input
                    type="text"
                    value={formData.fullName || ''}
                    onChange={(e) => handleChange('fullName', e.target.value)}
                    className="w-full px-4 py-3 border border-blue-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-primary"
                  />
                ) : (
                  <p className="px-4 py-3 bg-blue-50 rounded-xl text-dark-blue">
                    {profile?.fullName || 'N/A'}
                  </p>
                )}
              </div>

              {/* Số điện thoại */}
              <div>
                <label className="block text-sm font-semibold text-dark-blue mb-2">
                  Số điện thoại <span className="text-xs">(Không thể thay đổi)</span>
                </label>
                <p className="px-4 py-3 bg-gray-100 rounded-xl text-dark-blue/50">
                  {profile?.phoneNumber || 'N/A'} 
                </p>
              </div>

              {/* Email */}
              <div>
                <label className="block text-sm font-semibold text-dark-blue mb-2">
                  Email
                </label>
                {isEditing ? (
                  <input
                    type="email"
                    value={formData.email || ''}
                    onChange={(e) => handleChange('email', e.target.value)}
                    className="w-full px-4 py-3 border border-blue-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-primary"
                  />
                ) : (
                  <p className="px-4 py-3 bg-blue-50 rounded-xl text-dark-blue">
                    {profile?.email || 'N/A'}
                  </p>
                )}
              </div>

              {/* Ngày sinh */}
              <div>
                <label className="block text-sm font-semibold text-dark-blue mb-2">
                  Ngày sinh
                </label>
                {isEditing ? (
                  <input
                    type="date"
                    value={formData.dateOfBirth || ''}
                    onChange={(e) => handleChange('dateOfBirth', e.target.value)}
                    className="w-full px-4 py-3 border border-blue-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-primary"
                  />
                ) : (
                  <p className="px-4 py-3 bg-blue-50 rounded-xl text-dark-blue">
                    {profile?.dateOfBirth ? new Date(profile.dateOfBirth).toLocaleDateString('vi-VN') : 'N/A'}
                  </p>
                )}
              </div>

              {/* Giới tính */}
              <div>
                <label className="block text-sm font-semibold text-dark-blue mb-2">
                  Giới tính
                </label>
                {isEditing ? (
                  <select
                    value={formData.gender || ''}
                    onChange={(e) => handleChange('gender', e.target.value)}
                    className="w-full px-4 py-3 border border-blue-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-primary"
                  >
                    <option value="">Chọn giới tính</option>
                    <option value="MALE">Nam</option>
                    <option value="FEMALE">Nữ</option>
                    <option value="OTHER">Khác</option>
                  </select>
                ) : (
                  <p className="px-4 py-3 bg-blue-50 rounded-xl text-dark-blue">
                    {profile?.gender === 'MALE' ? 'Nam' : profile?.gender === 'FEMALE' ? 'Nữ' : profile?.gender === 'OTHER' ? 'Khác' : 'N/A'}
                  </p>
                )}
              </div>

              {/* CIF Number */}
              <div>
                <label className="block text-sm font-semibold text-dark-blue mb-2">
                  Số CIF
                </label>
                <p className="px-4 py-3 bg-gray-100 rounded-xl text-dark-blue/50">
                  {profile?.cifNumber || 'Chưa có'}
                </p>
              </div>
            </div>

            {/* Địa chỉ thường trú */}
            <div className="mt-8">
              <h3 className="text-xl font-bold text-dark-blue mb-4 flex items-center gap-2">
                <span className="material-icons-round text-blue-primary">home</span>
                Địa chỉ thường trú
              </h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-sm font-semibold text-dark-blue mb-2">Số nhà, đường</label>
                  {isEditing ? (
                    <input
                      type="text"
                      value={formData.permanentAddress?.street || ''}
                      onChange={(e) => handleAddressChange('permanentAddress', 'street', e.target.value)}
                      className="w-full px-4 py-3 border border-blue-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-primary"
                    />
                  ) : (
                    <p className="px-4 py-3 bg-blue-50 rounded-xl text-dark-blue">
                      {profile?.permanentAddress?.street || 'N/A'}
                    </p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-semibold text-dark-blue mb-2">Phường/Xã</label>
                  {isEditing ? (
                    <input
                      type="text"
                      value={formData.permanentAddress?.ward || ''}
                      onChange={(e) => handleAddressChange('permanentAddress', 'ward', e.target.value)}
                      className="w-full px-4 py-3 border border-blue-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-primary"
                    />
                  ) : (
                    <p className="px-4 py-3 bg-blue-50 rounded-xl text-dark-blue">
                      {profile?.permanentAddress?.ward || 'N/A'}
                    </p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-semibold text-dark-blue mb-2">Quận/Huyện</label>
                  {isEditing ? (
                    <input
                      type="text"
                      value={formData.permanentAddress?.district || ''}
                      onChange={(e) => handleAddressChange('permanentAddress', 'district', e.target.value)}
                      className="w-full px-4 py-3 border border-blue-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-primary"
                    />
                  ) : (
                    <p className="px-4 py-3 bg-blue-50 rounded-xl text-dark-blue">
                      {profile?.permanentAddress?.district || 'N/A'}
                    </p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-semibold text-dark-blue mb-2">Tỉnh/Thành phố</label>
                  {isEditing ? (
                    <input
                      type="text"
                      value={formData.permanentAddress?.city || ''}
                      onChange={(e) => handleAddressChange('permanentAddress', 'city', e.target.value)}
                      className="w-full px-4 py-3 border border-blue-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-primary"
                    />
                  ) : (
                    <p className="px-4 py-3 bg-blue-50 rounded-xl text-dark-blue">
                      {profile?.permanentAddress?.city || 'N/A'}
                    </p>
                  )}
                </div>
              </div>
            </div>

            {/* Địa chỉ tạm trú */}
            <div className="mt-8">
              <h3 className="text-xl font-bold text-dark-blue mb-4 flex items-center gap-2">
                <span className="material-icons-round text-blue-primary">location_on</span>
                Địa chỉ tạm trú
              </h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-sm font-semibold text-dark-blue mb-2">Số nhà, đường</label>
                  {isEditing ? (
                    <input
                      type="text"
                      value={formData.temporaryAddress?.street || ''}
                      onChange={(e) => handleAddressChange('temporaryAddress', 'street', e.target.value)}
                      className="w-full px-4 py-3 border border-blue-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-primary"
                    />
                  ) : (
                    <p className="px-4 py-3 bg-blue-50 rounded-xl text-dark-blue">
                      {profile?.temporaryAddress?.street || 'N/A'}
                    </p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-semibold text-dark-blue mb-2">Phường/Xã</label>
                  {isEditing ? (
                    <input
                      type="text"
                      value={formData.temporaryAddress?.ward || ''}
                      onChange={(e) => handleAddressChange('temporaryAddress', 'ward', e.target.value)}
                      className="w-full px-4 py-3 border border-blue-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-primary"
                    />
                  ) : (
                    <p className="px-4 py-3 bg-blue-50 rounded-xl text-dark-blue">
                      {profile?.temporaryAddress?.ward || 'N/A'}
                    </p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-semibold text-dark-blue mb-2">Quận/Huyện</label>
                  {isEditing ? (
                    <input
                      type="text"
                      value={formData.temporaryAddress?.district || ''}
                      onChange={(e) => handleAddressChange('temporaryAddress', 'district', e.target.value)}
                      className="w-full px-4 py-3 border border-blue-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-primary"
                    />
                  ) : (
                    <p className="px-4 py-3 bg-blue-50 rounded-xl text-dark-blue">
                      {profile?.temporaryAddress?.district || 'N/A'}
                    </p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-semibold text-dark-blue mb-2">Tỉnh/Thành phố</label>
                  {isEditing ? (
                    <input
                      type="text"
                      value={formData.temporaryAddress?.city || ''}
                      onChange={(e) => handleAddressChange('temporaryAddress', 'city', e.target.value)}
                      className="w-full px-4 py-3 border border-blue-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-primary"
                    />
                  ) : (
                    <p className="px-4 py-3 bg-blue-50 rounded-xl text-dark-blue">
                      {profile?.temporaryAddress?.city || 'N/A'}
                    </p>
                  )}
                </div>
              </div>
            </div>

            {/* Action Buttons */}
            {isEditing && (
              <div className="mt-8 flex justify-end gap-4">
                <button
                  onClick={handleCancel}
                  disabled={saving}
                  className="px-6 py-3 bg-gray-200 hover:bg-gray-300 text-dark-blue font-semibold rounded-xl transition-all duration-200 disabled:opacity-50"
                >
                  Hủy
                </button>
                <button
                  onClick={handleSave}
                  disabled={saving}
                  className="flex items-center gap-2 px-6 py-3 bg-blue-primary hover:bg-blue-600 text-white font-semibold rounded-xl transition-all duration-200 shadow-md hover:shadow-lg disabled:opacity-50"
                >
                  {saving ? (
                    <>
                      <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>
                      <span>Đang lưu...</span>
                    </>
                  ) : (
                    <>
                      <span className="material-icons-round text-xl">save</span>
                      <span>Lưu thay đổi</span>
                    </>
                  )}
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </Layout>
  )
}

export default Profile
