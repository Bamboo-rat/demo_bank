import React, { useState, useEffect } from 'react'
import { Link } from 'react-router'
import Layout from '~/component/layout/Layout'
import BeneficiaryList from '~/component/features/beneficiary/BeneficiaryList'
import { beneficiaryService } from '~/service/beneficiaryService'
import type { Beneficiary } from '~/type/beneficiary'
import { useAuth } from '~/context/AuthContext'

const Transfer = () => {
    const { customerId } = useAuth()
    const [beneficiaries, setBeneficiaries] = useState<Beneficiary[]>([])
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')

    useEffect(() => {
        if (customerId) {
            loadBeneficiaries()
        }
    }, [customerId])

    const loadBeneficiaries = async () => {
        if (!customerId) return
        setLoading(true)
        setError('')
        try {
            const data = await beneficiaryService.getAllBeneficiaries(customerId)
            setBeneficiaries(data)
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Không thể tải danh sách')
        } finally {
            setLoading(false)
        }
    }

    const transferCards: Array<{
        title: string
        icon: string
        link: string
    }> = [
            { title: 'Chuyển tiền nội bộ', icon: 'compare_arrows', link: '/transfer/internal' },
            { title: 'Chuyển liên ngân hàng', icon: 'account_balance', link: '/transfer/interbank' },
            { title: 'Chuyển tiền nhanh 24/7', icon: 'bolt', link: '/transfer/fast247' }
        ]

    return (
        <Layout>
            <div className="p-6">
                <h1 className="text-2xl font-semibold mb-4">Hình thức giao dịch</h1>
                <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
                    {transferCards.map((card) => {
                        return (
                            <Link
                                key={card.title}
                                to={card.link}
                                className="group bg-white rounded-xl border border-blue-100 p-5 transition-all duration-300 hover:shadow-lg hover:-translate-y-1"
                            >
                                <div className="flex flex-col items-center text-center">
                                    <div className="flex h-14 w-14 items-center justify-center rounded-full mb-3 group-hover:scale-110 transition-transform duration-300">
                                        <span className="material-icons-round text-xl">
                                            {card.icon}
                                        </span>
                                    </div>
                                    <p className="font-medium text-dark-blue">{card.title}</p>
                                </div>
                            </Link>
                        )
                    })}
                </div>

                {/* Beneficiaries Section */}
                <div className="mt-8">
                    <div className="flex justify-between items-center mb-4">
                        <h2 className="text-xl font-semibold text-gray-900">Danh bạ thụ hưởng</h2>
                        <p className="text-sm text-gray-500">
                            Lưu tự động sau khi chuyển tiền
                        </p>
                    </div>

                    {error && (
                        <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-700">
                            {error}
                        </div>
                    )}

                    <BeneficiaryList
                        beneficiaries={beneficiaries}
                        onDelete={() => {}}
                        loading={loading}
                    />
                </div>
            </div>
        </Layout>
    )
}

export default Transfer
