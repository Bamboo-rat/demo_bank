import type { Route } from "./+types/home";
import Welcome from "../pages/Welcome";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Ngân hàng Kiên Long" },
    { name: "description", content: "Welcome to KienLong Bank Digital Banking" },
  ];
}

export default function Home() {
  return <Welcome />;
}
