const BASE_URL = "http://localhost:8080/api";

function getToken() {
  return localStorage.getItem("ems_token");
}

function buildHeaders(extra = {}, hasBody = false) {
  const headers = { Accept: "application/json", ...extra };
  if (hasBody) {
    headers["Content-Type"] = "application/json";
  }
  const token = getToken();
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }
  return headers;
}

async function handleResponse(response) {
  let data = null;
  const text = await response.text();
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = { message: text };
    }
  }

  if (!response.ok) {
    if (response.status === 401) {
      localStorage.removeItem("ems_token");
      localStorage.removeItem("ems_user");
      if (!window.location.pathname.startsWith("/login")) {
        window.location.href = "/login";
      }
    }
    const message =
      (data && (data.message || data.error)) ||
      `Request failed with status ${response.status}`;
    const error = new Error(message);
    error.status = response.status;
    error.details = data;
    throw error;
  }

  return data;
}

function buildQuery(params) {
  if (!params) return "";
  const query = Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null && v !== "")
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
    .join("&");
  return query ? `?${query}` : "";
}

async function get(path, params) {
  try {
    const response = await fetch(`${BASE_URL}${path}${buildQuery(params)}`, {
      method: "GET",
      headers: buildHeaders(),
    });
    return await handleResponse(response);
  } catch (err) {
    if (err instanceof TypeError) {
      throw new Error("Network error - please check that the backend server is running.");
    }
    throw err;
  }
}

async function post(path, body) {
  try {
    const response = await fetch(`${BASE_URL}${path}`, {
      method: "POST",
      headers: buildHeaders({}, true),
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
    return await handleResponse(response);
  } catch (err) {
    if (err instanceof TypeError) {
      throw new Error("Network error - please check that the backend server is running.");
    }
    throw err;
  }
}

async function put(path, body) {
  try {
    const response = await fetch(`${BASE_URL}${path}`, {
      method: "PUT",
      headers: buildHeaders({}, true),
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
    return await handleResponse(response);
  } catch (err) {
    if (err instanceof TypeError) {
      throw new Error("Network error - please check that the backend server is running.");
    }
    throw err;
  }
}

async function del(path) {
  try {
    const response = await fetch(`${BASE_URL}${path}`, {
      method: "DELETE",
      headers: buildHeaders(),
    });
    return await handleResponse(response);
  } catch (err) {
    if (err instanceof TypeError) {
      throw new Error("Network error - please check that the backend server is running.");
    }
    throw err;
  }
}

const api = { get, post, put, delete: del, BASE_URL };

export default api;
