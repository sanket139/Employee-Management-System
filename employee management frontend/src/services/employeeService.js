import api from "./api";

export function getEmployees(params) {
  return api.get("/employees", params);
}

export function getEmployee(id) {
  return api.get(`/employees/${id}`);
}

export function getMyProfile() {
  return api.get("/employees/me");
}

export function createEmployee(payload) {
  return api.post("/employees", payload);
}

export function updateEmployee(id, payload) {
  return api.put(`/employees/${id}`, payload);
}

export function activateEmployee(id) {
  return api.put(`/employees/${id}/activate`);
}

export function deactivateEmployee(id) {
  return api.put(`/employees/${id}/deactivate`);
}

export function deleteEmployee(id) {
  return api.delete(`/employees/${id}`);
}
