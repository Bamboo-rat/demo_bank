import { createAuthenticatedAxios } from './authenticatedAxios'

const SAVINGS_SERVICE_BASE_URL =
  import.meta.env.VITE_API_BASE_SAVINGS_URL || 'http://localhost:8083/api'

const axiosSavings = createAuthenticatedAxios({
  baseURL: SAVINGS_SERVICE_BASE_URL,
  timeout: 30000
})

export default axiosSavings
