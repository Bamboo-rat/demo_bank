import type { Route } from "./+types/transferInterbank";
import TransferInterbank from "~/pages/transfer/TransferInterbank";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Ngân hàng Kiên Long - Chuyển tiền liên ngân hàng" },
    { name: "description", content: "Trang chuyển tiền liên ngân hàng của Ngân hàng Kiên Long" },
  ];
}

export default function TransferInterbankRoute() {
  return <TransferInterbank />;
}
