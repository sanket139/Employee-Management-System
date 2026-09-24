import { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { homeForRole } from "../../routes/ProtectedRoute";
import ErrorAlert from "../../components/ErrorAlert";

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ username: "", password: "" });
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const user = await login(form.username, form.password);
      const redirectTo = location.state?.from?.pathname || homeForRole(user.role);
      navigate(redirectTo, { replace: true });
    } catch (err) {
      setError(err.message || "Login failed. Please check your credentials.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="auth-wrapper">
      <div className="card auth-card p-4" style={{ width: 420 }}>
        <div className="card-body">
          <div className="text-center mb-4">
            <i className="bi bi-building fs-1 text-primary"></i>
            <h3 className="fw-bold mt-2">Employee Management System</h3>
            <p className="text-muted mb-0">Sign in to continue</p>
          </div>

          <ErrorAlert message={error} onClose={() => setError(null)} />

          <form onSubmit={handleSubmit}>
            <div className="mb-3">
              <label className="form-label">Username</label>
              <input
                type="text"
                className="form-control"
                name="username"
                value={form.username}
                onChange={handleChange}
                required
                autoFocus
              />
            </div>
            <div className="mb-3">
              <label className="form-label">Password</label>
              <input
                type="password"
                className="form-control"
                name="password"
                value={form.password}
                onChange={handleChange}
                required
              />
            </div>
            <button type="submit" className="btn btn-primary w-100" disabled={submitting}>
              {submitting ? "Signing in..." : "Sign In"}
            </button>
          </form>

          <p className="text-center text-muted mt-3 mb-0">
            Don&apos;t have an account? <Link to="/register">Register here</Link>
          </p>

          <div className="alert alert-light border mt-4 mb-0 small">
            <strong>Demo credentials</strong>
            <div>Admin: <code>admin / Admin@123</code></div>
            <div>HR: <code>hr_manager / Hr@12345</code></div>
            <div>Employee: <code>john.doe / Employee@123</code></div>
          </div>
        </div>
      </div>
    </div>
  );
}
