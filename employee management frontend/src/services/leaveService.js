import api from "./api";

export function applyLeave(payload) {
  return api.post("/leaves", payload);
}

export function getMyLeaves() {
  return api.get("/leaves/my");
}

export function getAllLeaves(params) {
  return api.get("/leaves", params);
}

export function approveLeave(id) {
  return api.put(`/leaves/${id}/approve`);
}

export function rejectLeave(id) {
  return api.put(`/leaves/${id}/reject`);
}
