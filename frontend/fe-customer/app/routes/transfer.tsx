import type { Route } from "./+types/transfer";
import Transfer from "~/pages/transfer/Transfer";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Ngân hàng Kiên Long - Chuyển tiền" },
    { name: "description", content: "Trang chuyển tiền của Ngân hàng Kiên Long" },
  ];
}

export default function TransferRoute() {
  return <Transfer />;
}
