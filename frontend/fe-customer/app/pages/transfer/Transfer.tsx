import React from 'react'
import { Link } from 'react-router'
import Layout from '~/component/layout/Layout'

const Transfer = () => {

    const transferCards: Array<{
        title: string
        icon: string
        link: string
    }> = [
            { title: 'Chuyển tiền nội bộ', icon: '', link: '/transfer/internal' },
            { title: 'Chuyển liên ngân hàng', icon: '', link: '/transfer/interbank' },
            { title: 'Chuyển tiền nhanh 24/7', icon: '', link: '/transfer/fast247' }
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

                <h4>Danh bạ thụ hưởng</h4>
                <div className="mt-4">
                    {/* Nội dung danh bạ thụ hưởng sẽ được hiển thị ở đây */}
                    <div className="text-center py-8"></div>
                </div>
            </div>
        </Layout>
    )
}

export default Transfer
