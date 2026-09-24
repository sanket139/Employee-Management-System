import api from "./api";

export function createPayroll(payload) {
  return api.post("/payroll", payload);
}

export function updatePayroll(id, payload) {
  return api.put(`/payroll/${id}`, payload);
}

export function markPayrollPaid(id) {
  return api.put(`/payroll/${id}/mark-paid`);
}

export function searchPayroll(params) {
  return api.get("/payroll", params);
}

export function getPayrollByEmployee(employeeId) {
  return api.get(`/payroll/employee/${employeeId}`);
}
