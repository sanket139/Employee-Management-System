import { useEffect, useState } from "react";
import * as departmentService from "../services/departmentService";

const emptyForm = {
  firstName: "",
  lastName: "",
  email: "",
  phone: "",
  address: "",
  dateOfBirth: "",
  gender: "Male",
  joiningDate: "",
  designation: "",
  salary: "",
  departmentId: "",
  username: "",
  password: "",
};

export default function EmployeeForm({ initialValues, onSubmit, submitting, submitLabel, showCredentials = true }) {
  const [form, setForm] = useState({ ...emptyForm, ...initialValues });
  const [departments, setDepartments] = useState([]);

  useEffect(() => {
    departmentService.getDepartments().then(setDepartments).catch(() => {});
  }, []);

  useEffect(() => {
    if (initialValues) {
      setForm((f) => ({ ...f, ...initialValues }));
    }
  }, [initialValues]);

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  function handleSubmit(e) {
    e.preventDefault();
    const payload = {
      ...form,
      salary: parseFloat(form.salary),
      departmentId: parseInt(form.departmentId, 10),
    };
    if (!payload.username) delete payload.username;
    if (!payload.password) delete payload.password;
    onSubmit(payload);
  }

  return (
    <form onSubmit={handleSubmit}>
      <div className="row">
        <div className="col-md-6 mb-3">
          <label className="form-label">First Name</label>
          <input className="form-control" name="firstName" value={form.firstName} onChange={handleChange} required />
        </div>
        <div className="col-md-6 mb-3">
          <label className="form-label">Last Name</label>
          <input className="form-control" name="lastName" value={form.lastName} onChange={handleChange} required />
        </div>
      </div>

      <div className="row">
        <div className="col-md-6 mb-3">
          <label className="form-label">Email</label>
          <input type="email" className="form-control" name="email" value={form.email} onChange={handleChange} required />
        </div>
        <div className="col-md-6 mb-3">
          <label className="form-label">Phone</label>
          <input className="form-control" name="phone" value={form.phone} onChange={handleChange} required />
        </div>
      </div>

      <div className="mb-3">
        <label className="form-label">Address</label>
        <input className="form-control" name="address" value={form.address} onChange={handleChange} />
      </div>

      <div className="row">
        <div className="col-md-4 mb-3">
          <label className="form-label">Date of Birth</label>
          <input type="date" className="form-control" name="dateOfBirth" value={form.dateOfBirth} onChange={handleChange} required />
        </div>
        <div className="col-md-4 mb-3">
          <label className="form-label">Gender</label>
          <select className="form-select" name="gender" value={form.gender} onChange={handleChange}>
            <option value="Male">Male</option>
            <option value="Female">Female</option>
            <option value="Other">Other</option>
          </select>
        </div>
        <div className="col-md-4 mb-3">
          <label className="form-label">Joining Date</label>
          <input type="date" className="form-control" name="joiningDate" value={form.joiningDate} onChange={handleChange} required />
        </div>
      </div>

      <div className="row">
        <div className="col-md-4 mb-3">
          <label className="form-label">Designation</label>
          <input className="form-control" name="designation" value={form.designation} onChange={handleChange} required />
        </div>
        <div className="col-md-4 mb-3">
          <label className="form-label">Salary</label>
          <input type="number" min="1" step="0.01" className="form-control" name="salary" value={form.salary} onChange={handleChange} required />
        </div>
        <div className="col-md-4 mb-3">
          <label className="form-label">Department</label>
          <select className="form-select" name="departmentId" value={form.departmentId} onChange={handleChange} required>
            <option value="">Select department</option>
            {departments.map((d) => (
              <option key={d.id} value={d.id}>{d.departmentName}</option>
            ))}
          </select>
        </div>
      </div>

      {showCredentials && (
        <>
          <hr />
          <p className="text-muted small mb-2">
            Optionally create a login account for this employee (leave blank to skip; you can add one later).
          </p>
          <div className="row">
            <div className="col-md-6 mb-3">
              <label className="form-label">Username</label>
              <input className="form-control" name="username" value={form.username} onChange={handleChange} />
            </div>
            <div className="col-md-6 mb-3">
              <label className="form-label">Password</label>
              <input type="password" className="form-control" name="password" value={form.password} onChange={handleChange} placeholder="Defaults to Employee@123" />
            </div>
          </div>
        </>
      )}

      <button type="submit" className="btn btn-primary" disabled={submitting}>
        {submitting ? "Saving..." : submitLabel}
      </button>
    </form>
  );
}
