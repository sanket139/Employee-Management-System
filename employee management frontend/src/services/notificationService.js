import api from "./api";

export function getMyNotifications() {
  return api.get("/notifications");
}

export function getUnreadCount() {
  return api.get("/notifications/unread-count");
}

export function markNotificationRead(id) {
  return api.put(`/notifications/${id}/read`);
}
