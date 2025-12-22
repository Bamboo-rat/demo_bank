import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'

const DEFAULT_TIMEOUT = 30000
const CUSTOMER_SERVICE_BASE_URL =
  import.meta.env.VITE_API_BASE_CUSTOMERSERVICE_URL || 'http://localhost:8082/api'

type FailedRequest = {
  resolve: (value?: unknown) => void
  reject: (reason?: unknown) => void
}

let isRefreshing = false
let failedQueue: FailedRequest[] = []

const processQueue = (error: unknown, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token)
    }
  })

  failedQueue = []
}

const clearStoredTokens = () => {
  localStorage.removeItem('access_token')
  localStorage.removeItem('refresh_token')
  localStorage.removeItem('user_info')
}

const refreshAccessToken = async () => {
  const refreshToken = localStorage.getItem('refresh_token')

  if (!refreshToken) {
    clearStoredTokens()
    window.location.href = '/'
    throw new Error('Refresh token is missing')
  }

  try {
    const response = await axios.post(
      `${CUSTOMER_SERVICE_BASE_URL}/auth/refresh`,
      { refresh_token: refreshToken }
    )

    const { access_token: accessToken, refresh_token: newRefreshToken } =
      response.data?.data ?? {}

    if (!accessToken || !newRefreshToken) {
      clearStoredTokens()
      window.location.href = '/'
      throw new Error('Không thể làm mới phiên đăng nhập')
    }

    localStorage.setItem('access_token', accessToken)
    localStorage.setItem('refresh_token', newRefreshToken)
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', newRefreshToken)

    return accessToken as string
  } catch (error) {
    clearStoredTokens()
    window.location.href = '/'
    throw error
  }
}

interface AuthenticatedAxiosOptions {
  baseURL: string
  timeout?: number
  publicEndpoints?: string[]
}

type RetryableRequestConfig = InternalAxiosRequestConfig & { _retry?: boolean }

export const createAuthenticatedAxios = (
  options: AuthenticatedAxiosOptions
): AxiosInstance => {
  const { baseURL, timeout = DEFAULT_TIMEOUT, publicEndpoints = [] } = options

  const instance = axios.create({
    baseURL,
    timeout,
    headers: {
      'Content-Type': 'application/json'
    }
  })

  instance.interceptors.request.use(
    (config) => {
      const isPublicEndpoint = publicEndpoints.some((endpoint) =>
        config.url?.includes(endpoint)
      )

      if (!isPublicEndpoint) {
        const token = localStorage.getItem('access_token')
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
      }

      return config
    },
    (error) => Promise.reject(error)
  )

  instance.interceptors.response.use(
    (response) => response,
    async (error) => {
      const { response, config } = error
      const originalRequest = config as RetryableRequestConfig

      const isPublicEndpoint = publicEndpoints.some((endpoint) =>
        originalRequest.url?.includes(endpoint)
      )

      if (response?.status === 401 && !originalRequest._retry && !isPublicEndpoint) {
        if (isRefreshing) {
          return new Promise((resolve, reject) => {
            failedQueue.push({ resolve, reject })
          })
            .then((token) => {
              if (typeof token === 'string') {
                originalRequest.headers.Authorization = `Bearer ${token}`
              }
              return instance(originalRequest)
            })
            .catch((queueError) => Promise.reject(queueError))
        }

        originalRequest._retry = true
        isRefreshing = true

        try {
          const newAccessToken = await refreshAccessToken()

          instance.defaults.headers.common.Authorization = `Bearer ${newAccessToken}`
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`

          processQueue(null, newAccessToken)

          return instance(originalRequest)
        } catch (refreshError) {
          clearStoredTokens()
          processQueue(refreshError, null)
          // Redirect will happen in refreshAccessToken function
          return Promise.reject(refreshError)
        } finally {
          isRefreshing = false
        }
      }

      return Promise.reject(error)
    }
  )

  return instance
}
