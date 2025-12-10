import type { Route } from "./+types/home";
import Register from "~/component/common/Register";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Ngân hàng Kiên Long - Đăng ký" },
    { name: "description", content: "Welcome to KienLongBank Digital Banking" },
  ];
}

export default function RegisterRoute() {
  return <Register />;
}
