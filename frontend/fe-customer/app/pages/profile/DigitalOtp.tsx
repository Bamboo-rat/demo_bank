import React, { useState } from 'react'
import Layout from '~/component/layout/Layout'
import { digitalOtpService, type DigitalOtpStatus } from '~/service/digitalOtpService'
import ConfigDigitalOtp from '~/component/features/ConfigDigitalOtp'
import { useAuth } from '~/context/AuthContext'

const DigitalOtp = () => {
  const { customerProfile: profile, customerId } = useAuth()
  const [status, setStatus] = useState<DigitalOtpStatus | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [modalOpen, setModalOpen] = useState(false)

  React.useEffect(() => {
    void initialize()
  }, [customerId])

  const initialize = async () => {
    if (!customerId) return
    
    setLoading(true)
    setError('')

    try {
      await loadStatus(customerId)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Không thể tải thông tin Digital OTP')
    } finally {
      setLoading(false)
    }
  }

  const loadStatus = async (customerIdValue: string) => {
    try {
      const statusData = await digitalOtpService.getStatus(customerIdValue)
      setStatus(statusData)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Không thể tải trạng thái Digital OTP')
    }
  }

  const handleSuccess = async () => {
    if (!customerId) return
    await loadStatus(customerId)
    setModalOpen(false)
  }

  const statusLabel = status?.locked
    ? 'Đang bị khóa'
    : status?.enrolled
      ? 'Đang hoạt động'
      : 'Chưa kích hoạt'

  const statusMessage = status?.locked
    ? 'Digital OTP đang tạm khóa. Vui lòng liên hệ tổng đài hoặc kích hoạt lại để tiếp tục giao dịch.'
    : status?.enrolled
      ? 'Digital OTP đã sẵn sàng xác thực mọi giao dịch trực tuyến của bạn.'
      : 'Bạn chưa kích hoạt Digital OTP. Vui lòng kích hoạt để thực hiện chuyển khoản an toàn.'

  const statusBadgeClass = status?.locked
    ? 'bg-red-100 text-red-700'
    : status?.enrolled
      ? 'bg-green-100 text-green-700'
      : 'bg-gray-100 text-gray-700'

  return (
    <Layout>
      <div className="max-w-4xl mx-auto p-6">
        <div className="mb-6">
          <p className="text-sm uppercase tracking-wide text-blue-500 font-semibold">Bảo mật giao dịch</p>
          <h1 className="text-3xl font-bold text-gray-900 mt-1">Digital OTP</h1>
          <p className="text-gray-600 mt-2">
            Quản lý khóa ký số và PIN để xác thực mọi giao dịch trực tuyến tại KienlongBank.
          </p>
        </div>

        {error && (
          <div className="mb-6 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-red-700">
            {error}
          </div>
        )}

        {loading ? (
          <div className="rounded-xl border border-blue-100 bg-white px-6 py-10 text-center shadow-sm">
            <p className="text-blue-700 font-medium">Đang tải thông tin Digital OTP...</p>
          </div>
        ) : (
          <div className="space-y-6">
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
              <div>
                <p className="text-sm font-semibold text-gray-500">Trạng thái hiện tại</p>
                <div className="flex items-center gap-3 mt-2">
                  <span className={`px-3 py-1 rounded-full text-sm font-semibold ${statusBadgeClass}`}>
                    {statusLabel}
                  </span>
                </div>
                <p className="text-sm text-gray-600 mt-3 max-w-xl">
                  {statusMessage}
                </p>
              </div>
              <div className="flex gap-3">
                {status?.enrolled && (
                  <button
                    type="button"
                    onClick={() => setModalOpen(true)}
                    className="px-5 py-3 rounded-xl border border-blue-200 text-blue-700 font-semibold hover:bg-blue-50"
                  >
                    Cập nhật cấu hình
                  </button>
                )}
                <button
                  type="button"
                  onClick={() => setModalOpen(true)}
                  className="px-5 py-3 rounded-xl bg-blue-600 text-white font-semibold hover:bg-blue-700"
                >
                  {status?.enrolled ? 'Đổi PIN / khóa' : 'Kích hoạt Digital OTP'}
                </button>
              </div>
            </div>

            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
              <h2 className="text-xl font-semibold text-gray-900">Digital OTP dùng để làm gì?</h2>
              <p className="text-sm text-gray-600 mt-3">
                Digital OTP là mã ký số bảo vệ các giao dịch chuyển khoản trực tuyến. Sau khi kích hoạt, mọi giao dịch quan trọng sẽ yêu cầu chữ ký số này để xác nhận.
              </p>
              <ul className="mt-4 space-y-2 text-sm text-gray-700">
                <li>• Áp dụng cho tất cả các giao dịch chuyển khoản trên KienlongBank E-Banking.</li>
                <li>• Có thể cập nhật lại bất cứ lúc nào nếu bạn muốn tạo PIN mới.</li>
                <li>• Nếu Digital OTP bị khóa, hãy cập nhật lại hoặc liên hệ tổng đài 19006929 để được hỗ trợ.</li>
              </ul>
            </div>
          </div>
        )}

        {profile?.customerId && (
          <ConfigDigitalOtp
            open={modalOpen}
            customerId={profile.customerId}
            mode={status?.enrolled ? 'update' : 'enroll'}
            disableClose={!status?.enrolled}
            onClose={() => setModalOpen(false)}
            onSuccess={() => { void handleSuccess() }}
          />
        )}
      </div>
    </Layout>
  )
}

export default DigitalOtp
