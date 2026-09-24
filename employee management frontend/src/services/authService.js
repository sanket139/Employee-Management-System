import api from "./api";

export function login(username, password) {
  return api.post("/auth/login", { username, password });
}

export function register(payload) {
  return api.post("/auth/register", payload);
}
