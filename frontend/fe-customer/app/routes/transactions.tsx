import type { Route } from "./+types/transactions";
import TransactionHistory from "~/pages/transfer/TransactionHistory";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Ngân hàng Kiên Long - Lịch sử giao dịch" },
    { name: "description", content: "Lịch sử giao dịch của Ngân hàng Kiên Long" },
  ];
}

export default function TransactionsRoute() {
  return <TransactionHistory />;
}
