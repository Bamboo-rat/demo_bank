export interface Province {
  code: string
  name: string
}

export interface District {
  code: string
  name: string
}

export interface Ward {
  code: string
  name: string
}

export interface AddressRequest {
  street: string
  ward: string
  district: string
  city: string
  country: string
}

export interface CustomerRegisterRequest {
  password: string
  fullName: string
  dateOfBirth: string
  gender: string
  nationality: string
  nationalId: string
  issueDateNationalId: string
  placeOfIssueNationalId: string
  occupation?: string
  position?: string
  email: string
  phoneNumber: string
  permanentAddress: AddressRequest
  temporaryAddress?: AddressRequest
}

class AddressService {
  private baseUrl = 'https://provinces.open-api.vn/api'

  async getProvinces(): Promise<Province[]> {
    try {
      const response = await fetch(`${this.baseUrl}/?depth=1`)
      const data = await response.json()
      return data
    } catch (error) {
      console.error('Error fetching provinces:', error)
      return []
    }
  }

  async getDistricts(provinceCode: string): Promise<District[]> {
    try {
      const response = await fetch(`${this.baseUrl}/p/${provinceCode}?depth=2`)
      const data = await response.json()
      return data.districts || []
    } catch (error) {
      console.error('Error fetching districts:', error)
      return []
    }
  }

  async getWards(districtCode: string): Promise<Ward[]> {
    try {
      const response = await fetch(`${this.baseUrl}/d/${districtCode}?depth=2`)
      const data = await response.json()
      return data.wards || []
    } catch (error) {
      console.error('Error fetching wards:', error)
      return []
    }
  }

  findNameByCode(items: Array<{code: string, name: string}>, code: string): string {
    return items.find(item => item.code === code)?.name || ''
  }
}

export const addressService = new AddressService()