import type { Route } from "./+types/transferFast";
import TransferFast from "~/pages/transfer/TransferFast";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Ngân hàng Kiên Long - Chuyển tiền nhanh" },
    { name: "description", content: "Trang chuyển tiền nhanh của Ngân hàng Kiên Long" },
  ];
}

export default function TransferFastRoute() {
  return <TransferFast />;
}
