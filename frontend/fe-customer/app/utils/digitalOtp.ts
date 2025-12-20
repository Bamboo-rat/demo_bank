const OTP_TIME_STEP_MS = 30_000
const OTP_DIGITS = 6

export const DIGITAL_OTP_STORAGE_KEYS = {
  secret: 'digitalOtpSecret',
  salt: 'digitalOtpSalt'
} as const

export const arrayBufferToBase64 = (buffer: ArrayBuffer): string => {
  const bytes = new Uint8Array(buffer)
  let binary = ''
  bytes.forEach(byte => {
    binary += String.fromCharCode(byte)
  })
  return window.btoa(binary)
}

export const base64ToArrayBuffer = (base64: string): ArrayBuffer => {
  const binary = window.atob(base64)
  const length = binary.length
  const bytes = new Uint8Array(length)
  for (let i = 0; i < length; i += 1) {
    bytes[i] = binary.charCodeAt(i)
  }
  return bytes.buffer
}

export const hashPinWithSalt = async (pin: string, salt: Uint8Array | string): Promise<string> => {
  const encoder = new TextEncoder()
  const pinBytes = encoder.encode(pin)
  const saltBytes = typeof salt === 'string' ? new Uint8Array(base64ToArrayBuffer(salt)) : salt
  const combined = new Uint8Array(pinBytes.length + saltBytes.length)
  combined.set(pinBytes)
  combined.set(saltBytes, pinBytes.length)
  const digest = await window.crypto.subtle.digest('SHA-256', combined)
  return arrayBufferToBase64(digest)
}

export const generateDigitalOtpSecret = (): string => {
  const randomBytes = window.crypto.getRandomValues(new Uint8Array(32))
  return arrayBufferToBase64(randomBytes.buffer)
}

export const persistDigitalOtpSecret = (secretBase64: string) => {
  localStorage.setItem(DIGITAL_OTP_STORAGE_KEYS.secret, secretBase64)
}

export const persistDigitalOtpSalt = (saltBase64: string) => {
  localStorage.setItem(DIGITAL_OTP_STORAGE_KEYS.salt, saltBase64)
}

export const getStoredOtpSecret = (): string | null => (
  localStorage.getItem(DIGITAL_OTP_STORAGE_KEYS.secret)
)

export const getStoredSalt = (): string | null => (
  localStorage.getItem(DIGITAL_OTP_STORAGE_KEYS.salt)
)

export const clearDigitalOtpMaterial = () => {
  localStorage.removeItem(DIGITAL_OTP_STORAGE_KEYS.secret)
  localStorage.removeItem(DIGITAL_OTP_STORAGE_KEYS.salt)
}

const importHmacKeyFromSecret = async (secretBase64: string): Promise<CryptoKey> => {
  const keyBuffer = base64ToArrayBuffer(secretBase64)
  return window.crypto.subtle.importKey(
    'raw',
    keyBuffer,
    {
      name: 'HMAC',
      hash: 'SHA-256'
    },
    false,
    ['sign']
  )
}

export const computeTotpToken = async (secretBase64: string, message: string): Promise<string> => {
  if (!window.crypto?.subtle) {
    throw new Error('Trình duyệt không hỗ trợ Digital OTP. Vui lòng cập nhật trình duyệt.')
  }

  const key = await importHmacKeyFromSecret(secretBase64)
  const encoder = new TextEncoder()
  const signature = await window.crypto.subtle.sign('HMAC', key, encoder.encode(message))
  const data = new Uint8Array(signature)
  const offset = data[data.length - 1] & 0x0f
  const binary =
    ((data[offset] & 0x7f) << 24) |
    ((data[offset + 1] & 0xff) << 16) |
    ((data[offset + 2] & 0xff) << 8) |
    (data[offset + 3] & 0xff)
  const otp = binary % 10 ** OTP_DIGITS
  return otp.toString().padStart(OTP_DIGITS, '0')
}

export const getCurrentTimeSlice = (): number => (
  Math.floor(Date.now() / OTP_TIME_STEP_MS)
)

export const getTimeRemainingInSlice = (): number => (
  Math.ceil((OTP_TIME_STEP_MS - (Date.now() % OTP_TIME_STEP_MS)) / 1000)
)

export const DIGITAL_OTP_TIME_STEP_SECONDS = OTP_TIME_STEP_MS / 1000
