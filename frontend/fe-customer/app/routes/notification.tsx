import type { Route } from "./+types/notification";
import Notification from "~/pages/notification/Notification";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Ngân hàng Kiên Long - Thông báo" },
    { name: "description", content: "Notification page of KienLongBank" },
  ];
}

export default function NotificationRoute() {
  return <Notification />;
}
