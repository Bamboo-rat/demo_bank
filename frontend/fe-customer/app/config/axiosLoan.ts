import { createAuthenticatedAxios } from './authenticatedAxios'

const LOAN_SERVICE_BASE_URL =
  import.meta.env.VITE_API_BASE_LOAN_URL || 'http://localhost:8086/api'

const axiosLoan = createAuthenticatedAxios({
  baseURL: LOAN_SERVICE_BASE_URL,
  timeout: 30000
})

export default axiosLoan
