import type { Route } from "./+types/home";
import Login from "~/component/common/Login";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Ngân hàng Kiên Long - Đăng nhập" },
    { name: "description", content: "Welcome to KienLong Bank Digital Banking" },
  ];
}

export default function LoginRoute() {
  return <Login />;
}
