import api from "./api";

export function getAdminDashboard() {
  return api.get("/dashboard/admin");
}

export function getHrDashboard() {
  return api.get("/dashboard/hr");
}

export function getEmployeeDashboard() {
  return api.get("/dashboard/employee");
}
