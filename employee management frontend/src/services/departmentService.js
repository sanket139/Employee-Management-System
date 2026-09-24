import api from "./api";

export function getDepartments() {
  return api.get("/departments");
}

export function getDepartment(id) {
  return api.get(`/departments/${id}`);
}

export function createDepartment(payload) {
  return api.post("/departments", payload);
}

export function updateDepartment(id, payload) {
  return api.put(`/departments/${id}`, payload);
}

export function activateDepartment(id) {
  return api.put(`/departments/${id}/activate`);
}

export function deactivateDepartment(id) {
  return api.put(`/departments/${id}/deactivate`);
}

export function deleteDepartment(id) {
  return api.delete(`/departments/${id}`);
}
