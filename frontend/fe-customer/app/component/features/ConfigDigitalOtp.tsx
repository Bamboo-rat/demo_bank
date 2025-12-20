import React, { useEffect, useMemo, useState } from 'react'
import { digitalOtpService, type DigitalOtpEnrollmentPayload } from '~/service/digitalOtpService'
import {
  arrayBufferToBase64,
  generateDigitalOtpSecret,
  hashPinWithSalt,
  persistDigitalOtpSalt,
  persistDigitalOtpSecret
} from '~/utils/digitalOtp'

interface ConfigDigitalOtpProps {
  open: boolean
  customerId: string
  mode?: 'enroll' | 'update'
  disableClose?: boolean
  onClose: () => void
  onSuccess?: () => void
}

const DEFAULT_PIN_LENGTH = 6

const ConfigDigitalOtp: React.FC<ConfigDigitalOtpProps> = ({
  open,
  customerId,
  mode = 'enroll',
  disableClose = false,
  onClose,
  onSuccess
}) => {
  const [pin, setPin] = useState('')
  const [confirmPin, setConfirmPin] = useState('')
  const [otpSecret, setOtpSecret] = useState('')
  const [secretGenerating, setSecretGenerating] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string>('')
  const [successMessage, setSuccessMessage] = useState('')
  const [secretReady, setSecretReady] = useState(false)

  const heading = useMemo(() => (
    mode === 'enroll' ? 'Kích hoạt Digital OTP' : 'Cập nhật Digital OTP'
  ), [mode])

  useEffect(() => {
    if (!open) {
      return
    }

    setError('')
    setSuccessMessage('')
    setPin('')
    setConfirmPin('')
    setSecretReady(false)
    setOtpSecret('')

    handleGenerateSecret()
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open])

  const handleGenerateSecret = () => {
    try {
      setSecretGenerating(true)
      setError('')
      const secret = generateDigitalOtpSecret()
      persistDigitalOtpSecret(secret)
      setOtpSecret(secret)
      setSecretReady(true)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Không thể tạo khóa Digital OTP mới')
    } finally {
      setSecretGenerating(false)
    }
  }

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()

    if (!otpSecret) {
      setError('Vui lòng tạo khóa Digital OTP trước khi lưu')
      return
    }

    if (pin.length !== DEFAULT_PIN_LENGTH) {
      setError(`PIN phải gồm ${DEFAULT_PIN_LENGTH} chữ số`)
      return
    }

    if (pin !== confirmPin) {
      setError('PIN xác nhận không khớp')
      return
    }

    setSubmitting(true)
    setError('')

    try {
      const salt = window.crypto.getRandomValues(new Uint8Array(16))
      const saltBase64 = arrayBufferToBase64(salt.buffer)
      const pinHash = await hashPinWithSalt(pin, salt)
      persistDigitalOtpSalt(saltBase64)

      const payload: DigitalOtpEnrollmentPayload = {
        customerId,
        digitalOtpSecret: otpSecret,
        digitalPinHash: pinHash,
        salt: saltBase64
      }

      const action = mode === 'update' ? digitalOtpService.update : digitalOtpService.enroll
      await action(payload)

      setSuccessMessage(
        mode === 'enroll'
          ? 'Kích hoạt Digital OTP thành công.'
          : 'Cập nhật Digital OTP thành công.'
      )

      onSuccess?.()

      if (!disableClose) {
        setTimeout(() => {
          onClose()
          setSuccessMessage('')
        }, 1200)
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Không thể lưu cấu hình Digital OTP')
    } finally {
      setSubmitting(false)
    }
  }

  if (!open) {
    return null
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-gray-900/40 backdrop-blur-sm p-4">
      <div className="w-full max-w-2xl bg-white rounded-2xl shadow-2xl border border-blue-100 relative">
        {!disableClose && (
          <button
            type="button"
            onClick={onClose}
            className="absolute top-4 right-4 text-gray-500 hover:text-gray-700"
            aria-label="Đóng"
          >
            ✕
          </button>
        )}

        <div className="px-8 py-6">
          <div className="mb-6">
            <p className="text-sm uppercase tracking-wide text-blue-500 font-semibold">
              Digital OTP
            </p>
            <h2 className="text-2xl font-bold text-gray-900 mt-1">{heading}</h2>
            <p className="text-gray-600 mt-2">
              Digital OTP giúp bạn ký số cho mọi giao dịch quan trọng. Vui lòng tạo PIN và lưu khóa bí mật an toàn.
            </p>
          </div>

          {error && (
            <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-red-700">
              {error}
            </div>
          )}

          {successMessage && (
            <div className="mb-4 rounded-lg border border-green-200 bg-green-50 px-4 py-3 text-green-700">
              {successMessage}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="rounded-xl border border-blue-100 bg-blue-50 px-4 py-3 space-y-2">
              <div className="flex items-center justify-between gap-4">
                <div>
                  <p className="text-sm font-semibold text-blue-900">Khóa Digital OTP 30s</p>
                  <p className="text-sm text-blue-800/80">
                    Khóa bí mật được sinh ngẫu nhiên để tạo mã 6 số mới mỗi 30 giây và chỉ lưu trên trình duyệt của bạn.
                  </p>
                </div>
                <button
                  type="button"
                  disabled={secretGenerating}
                  onClick={handleGenerateSecret}
                  className="px-4 py-2 text-sm font-medium rounded-lg border border-blue-500 text-blue-600 hover:bg-blue-500 hover:text-white disabled:opacity-60"
                >
                  {secretGenerating ? 'Đang tạo...' : 'Tạo lại khóa'}
                </button>
              </div>
              {secretReady && (
                <p className="text-sm text-green-600 font-semibold">
                  Khóa mới đã sẵn sàng. Bạn có thể dùng PIN để sinh mã Digital OTP 6 số.
                </p>
              )}
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">PIN 6 số</label>
                <input
                  type="password"
                  inputMode="numeric"
                  pattern="[0-9]*"
                  value={pin}
                  onChange={(event) => setPin(event.target.value.replace(/\D/g, '').slice(0, DEFAULT_PIN_LENGTH))}
                  placeholder="••••••"
                  className="w-full rounded-lg border border-gray-300 px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 tracking-widest text-center text-lg"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Nhập lại PIN</label>
                <input
                  type="password"
                  inputMode="numeric"
                  pattern="[0-9]*"
                  value={confirmPin}
                  onChange={(event) => setConfirmPin(event.target.value.replace(/\D/g, '').slice(0, DEFAULT_PIN_LENGTH))}
                  placeholder="••••••"
                  className="w-full rounded-lg border border-gray-300 px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 tracking-widest text-center text-lg"
                  required
                />
              </div>
            </div>

            <div className="rounded-lg bg-yellow-50 border border-yellow-200 px-4 py-3 text-sm text-yellow-800">
              <p className="font-semibold">Lưu ý</p>
              <ul className="mt-1 list-disc pl-5 space-y-1">
                <li>PIN được mã hóa hoàn toàn, hệ thống không lưu giá trị gốc.</li>
                <li>Mỗi lần cập nhật sẽ tạo khóa bí mật mới và vô hiệu hóa mã cũ.</li>
              </ul>
            </div>

            <div className="flex justify-end gap-3 pt-2">
              {!disableClose && (
                <button
                  type="button"
                  onClick={onClose}
                  className="px-5 py-3 rounded-lg border border-gray-300 text-gray-700 hover:bg-gray-50"
                >
                  Hủy
                </button>
              )}
              <button
                type="submit"
                disabled={submitting || !otpSecret || secretGenerating}
                className="px-6 py-3 rounded-lg bg-blue-600 text-white font-semibold hover:bg-blue-700 disabled:bg-gray-400"
              >
                {submitting ? 'Đang lưu...' : (mode === 'enroll' ? 'Kích hoạt Digital OTP' : 'Cập nhật cấu hình')}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}

export default ConfigDigitalOtp
