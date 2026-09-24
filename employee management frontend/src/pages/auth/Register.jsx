import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import ErrorAlert from "../../components/ErrorAlert";

const initialForm = {
  firstName: "",
  lastName: "",
  username: "",
  email: "",
  password: "",
  role: "EMPLOYEE",
};

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState(initialForm);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await register(form);
      setSuccess(true);
      setTimeout(() => navigate("/login"), 1500);
    } catch (err) {
      setError(err.message || "Registration failed.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="auth-wrapper">
      <div className="card auth-card p-4" style={{ width: 460 }}>
        <div className="card-body">
          <div className="text-center mb-4">
            <i className="bi bi-person-plus fs-1 text-primary"></i>
            <h3 className="fw-bold mt-2">Create an Account</h3>
            <p className="text-muted mb-0">
              Note: Admin accounts cannot be created here for security reasons.
            </p>
          </div>

          <ErrorAlert message={error} onClose={() => setError(null)} />
          {success && (
            <div className="alert alert-success">Registration successful! Redirecting to login...</div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="row">
              <div className="col-6 mb-3">
                <label className="form-label">First Name</label>
                <input className="form-control" name="firstName" value={form.firstName} onChange={handleChange} required />
              </div>
              <div className="col-6 mb-3">
                <label className="form-label">Last Name</label>
                <input className="form-control" name="lastName" value={form.lastName} onChange={handleChange} required />
              </div>
            </div>
            <div className="mb-3">
              <label className="form-label">Username</label>
              <input className="form-control" name="username" value={form.username} onChange={handleChange} required minLength={3} />
            </div>
            <div className="mb-3">
              <label className="form-label">Email</label>
              <input type="email" className="form-control" name="email" value={form.email} onChange={handleChange} required />
            </div>
            <div className="mb-3">
              <label className="form-label">Password</label>
              <input type="password" className="form-control" name="password" value={form.password} onChange={handleChange} required minLength={6} />
            </div>
            <div className="mb-3">
              <label className="form-label">Role</label>
              <select className="form-select" name="role" value={form.role} onChange={handleChange}>
                <option value="EMPLOYEE">Employee</option>
                <option value="HR">HR</option>
              </select>
            </div>
            <button type="submit" className="btn btn-primary w-100" disabled={submitting}>
              {submitting ? "Registering..." : "Register"}
            </button>
          </form>

          <p className="text-center text-muted mt-3 mb-0">
            Already have an account? <Link to="/login">Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
