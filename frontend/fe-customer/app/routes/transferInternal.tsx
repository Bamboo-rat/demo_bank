import type { Route } from "./+types/transferInternal";
import TransferInternal from "~/pages/transfer/TransferInternal";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Ngân hàng Kiên Long - Chuyển tiền nội bộ" },
    { name: "description", content: "Trang chuyển tiền nội bộ của Ngân hàng Kiên Long" },
  ];
}

export default function TransferInternalRoute() {
  return <TransferInternal />;
}
