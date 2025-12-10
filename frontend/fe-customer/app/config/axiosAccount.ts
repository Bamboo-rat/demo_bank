import { createAuthenticatedAxios } from './authenticatedAxios'

const axiosAccount = createAuthenticatedAxios({
  baseURL: import.meta.env.VITE_API_BASE_ACCOUNTSERVICE_URL || 'http://localhost:8083/api'
})

export default axiosAccount
