import api from "./api";

export function markAttendance(payload) {
  return api.post("/attendance", payload);
}

export function updateAttendance(id, payload) {
  return api.put(`/attendance/${id}`, payload);
}

export function searchAttendance(params) {
  return api.get("/attendance", params);
}

export function getAttendanceByEmployee(employeeId) {
  return api.get(`/attendance/employee/${employeeId}`);
}

export function getMonthlyPercentage(employeeId, year, month) {
  return api.get(`/attendance/employee/${employeeId}/percentage`, { year, month });
}
