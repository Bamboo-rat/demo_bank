import { createAuthenticatedAxios } from './authenticatedAxios'

const axiosTransaction = createAuthenticatedAxios({
  baseURL: import.meta.env.VITE_API_BASE_TRANSACTIONSERVICE_URL || 'http://localhost:8084/api'
})

export default axiosTransaction
