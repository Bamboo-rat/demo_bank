import type { Route } from "./+types/home";
import Profile from "~/pages/profile/Profile";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Ngân hàng Kiên Long - Hồ sơ" },
    { name: "description", content: "Trang hồ sơ người dùng của Ngân hàng Kiên Long" },
  ];
}

export default function ProfileRoute() {
  return <Profile />;
}
