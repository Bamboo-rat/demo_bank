import type { Route } from "./+types/accountList";
import AccountList from "~/pages/account/AccountList";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Ngân hàng Kiên Long - Danh sách tài khoản" },
    { name: "description", content: "Trang danh sách tài khoản của Ngân hàng Kiên Long" },
  ];
}

export default function AccountListRoute() {
  return <AccountList />;
}
