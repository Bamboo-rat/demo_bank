import type { Route } from "./+types/home";
import Dashboard from "~/pages/Dashboard";
import { useEffect } from "react";
import { useNavigate } from "react-router";
import { authService } from "~/service/authService";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Ngân hàng Kiên Long - Dashboard" },
    { name: "description", content: "KienLong Bank Digital Banking Dashboard" },
  ];
}

export default function DashboardRoute() {
  const navigate = useNavigate();

  useEffect(() => {
    if (!authService.isAuthenticated()) {
      navigate('/login');
    }
  }, [navigate]);

  return <Dashboard />;
}
