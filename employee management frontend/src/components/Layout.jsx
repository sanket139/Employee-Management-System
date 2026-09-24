import { NavLink, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import * as notificationService from "../services/notificationService";

const NAV_ITEMS = {
  ADMIN: [
    { to: "/admin/dashboard", label: "Dashboard", icon: "bi-speedometer2" },
    { to: "/employees", label: "Employees", icon: "bi-people" },
    { to: "/departments", label: "Departments", icon: "bi-diagram-3" },
    { to: "/attendance", label: "Attendance", icon: "bi-calendar-check" },
    { to: "/leaves/manage", label: "Leave Management", icon: "bi-envelope-paper" },
    { to: "/payroll/manage", label: "Payroll", icon: "bi-cash-coin" },
    { to: "/notifications", label: "Notifications", icon: "bi-bell" },
    { to: "/profile", label: "Profile", icon: "bi-person-circle" },
  ],
  HR: [
    { to: "/hr/dashboard", label: "Dashboard", icon: "bi-speedometer2" },
    { to: "/employees", label: "Employees", icon: "bi-people" },
    { to: "/departments", label: "Departments", icon: "bi-diagram-3" },
    { to: "/attendance", label: "Attendance", icon: "bi-calendar-check" },
    { to: "/leaves/manage", label: "Leave Management", icon: "bi-envelope-paper" },
    { to: "/payroll/manage", label: "Payroll", icon: "bi-cash-coin" },
    { to: "/notifications", label: "Notifications", icon: "bi-bell" },
    { to: "/profile", label: "Profile", icon: "bi-person-circle" },
  ],
  EMPLOYEE: [
    { to: "/employee/dashboard", label: "Dashboard", icon: "bi-speedometer2" },
    { to: "/attendance/my", label: "My Attendance", icon: "bi-calendar-check" },
    { to: "/leaves/apply", label: "Apply Leave", icon: "bi-envelope-plus" },
    { to: "/leaves/my", label: "My Leave History", icon: "bi-envelope-paper" },
    { to: "/payroll/my", label: "My Payroll", icon: "bi-cash-coin" },
    { to: "/notifications", label: "Notifications", icon: "bi-bell" },
    { to: "/profile", label: "Profile", icon: "bi-person-circle" },
  ],
};

export default function Layout({ children }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    let active = true;
    async function loadUnread() {
      try {
        const res = await notificationService.getUnreadCount();
        if (active) setUnreadCount(res.unreadCount);
      } catch {
        // silently ignore - non-critical widget
      }
    }
    loadUnread();
    const interval = setInterval(loadUnread, 30000);
    return () => {
      active = false;
      clearInterval(interval);
    };
  }, []);

  function handleLogout() {
    logout();
    navigate("/login");
  }

  const items = NAV_ITEMS[user?.role] || [];

  return (
    <div className="d-flex">
      <aside className="ems-sidebar d-none d-md-flex flex-column py-3">
        <div className="px-3 mb-4 d-flex align-items-center">
          <i className="bi bi-building fs-3 me-2"></i>
          <div>
            <div className="fw-bold">EMS</div>
            <small className="text-white-50">Employee Mgmt System</small>
          </div>
        </div>
        <nav className="nav flex-column flex-grow-1">
          {items.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => `nav-link d-flex align-items-center ${isActive ? "active" : ""}`}
            >
              <i className={`bi ${item.icon} me-2`}></i>
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="px-3 pt-3 border-top border-secondary">
          <button className="btn btn-outline-light btn-sm w-100" onClick={handleLogout}>
            <i className="bi bi-box-arrow-right me-1"></i> Logout
          </button>
        </div>
      </aside>

      <div className="ems-content d-flex flex-column">
        <header className="ems-topbar d-flex align-items-center justify-content-between px-4 py-3">
          <div className="d-md-none fw-bold">
            <i className="bi bi-building me-2"></i>EMS
          </div>
          <div className="d-none d-md-block text-muted">
            Welcome back, <strong>{user?.username}</strong>
          </div>
          <div className="d-flex align-items-center gap-3">
            <NavLink to="/notifications" className="position-relative text-dark">
              <i className="bi bi-bell fs-5"></i>
              {unreadCount > 0 && (
                <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
                  {unreadCount}
                </span>
              )}
            </NavLink>
            <span className="badge bg-primary text-uppercase">{user?.role}</span>
            <button className="btn btn-sm btn-outline-danger d-md-none" onClick={handleLogout}>
              Logout
            </button>
          </div>
        </header>
        <main className="p-4 flex-grow-1">{children}</main>
      </div>
    </div>
  );
}
