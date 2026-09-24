import { useEffect, useState } from "react";
import Layout from "../../components/Layout";
import LoadingSpinner from "../../components/LoadingSpinner";
import ErrorAlert from "../../components/ErrorAlert";
import EmptyState from "../../components/EmptyState";
import * as notificationService from "../../services/notificationService";

const TYPE_ICONS = {
  LEAVE_APPLIED: "bi-envelope-plus text-warning",
  LEAVE_APPROVED: "bi-check-circle text-success",
  LEAVE_REJECTED: "bi-x-circle text-danger",
  GENERAL: "bi-info-circle text-primary",
};

export default function Notifications() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    load();
  }, []);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const res = await notificationService.getMyNotifications();
      setNotifications(res);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleMarkRead(id) {
    try {
      await notificationService.markNotificationRead(id);
      setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, readStatus: true } : n)));
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <Layout>
      <h3 className="fw-bold mb-4">Notifications</h3>
      <ErrorAlert message={error} onClose={() => setError(null)} />

      {loading && <LoadingSpinner label="Loading notifications..." />}

      {!loading && (
        <div className="card ems-card">
          {notifications.length === 0 && <EmptyState icon="bi-bell" title="You're all caught up" message="No notifications yet." />}
          {notifications.length > 0 && (
            <ul className="list-group list-group-flush">
              {notifications.map((n) => (
                <li
                  key={n.id}
                  className={`list-group-item d-flex justify-content-between align-items-start ${!n.readStatus ? "bg-light" : ""}`}
                >
                  <div className="d-flex">
                    <i className={`bi ${TYPE_ICONS[n.type] || "bi-info-circle"} fs-5 me-3 mt-1`}></i>
                    <div>
                      <div>{n.message}</div>
                      <small className="text-muted">{new Date(n.createdAt).toLocaleString()}</small>
                    </div>
                  </div>
                  {!n.readStatus && (
                    <button className="btn btn-sm btn-outline-primary" onClick={() => handleMarkRead(n.id)}>
                      Mark as read
                    </button>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </Layout>
  );
}
