import { useEffect, useState } from "react";
import Layout from "../../components/Layout";
import LoadingSpinner from "../../components/LoadingSpinner";
import ErrorAlert from "../../components/ErrorAlert";
import EmptyState from "../../components/EmptyState";
import ConfirmDialog from "../../components/ConfirmDialog";
import StatusBadge from "../../components/StatusBadge";
import * as departmentService from "../../services/departmentService";
import { useAuth } from "../../context/AuthContext";

const emptyForm = { departmentName: "", description: "" };

export default function Departments() {
  const { user } = useAuth();
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  const [showForm, setShowForm] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [submitting, setSubmitting] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);

  useEffect(() => {
    load();
  }, []);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const res = await departmentService.getDepartments();
      setDepartments(res);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  function openCreate() {
    setEditing(null);
    setForm(emptyForm);
    setShowForm(true);
  }

  function openEdit(dept) {
    setEditing(dept);
    setForm({ departmentName: dept.departmentName, description: dept.description || "" });
    setShowForm(true);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      if (editing) {
        await departmentService.updateDepartment(editing.id, form);
        setSuccessMsg("Department updated successfully.");
      } else {
        await departmentService.createDepartment(form);
        setSuccessMsg("Department created successfully.");
      }
      setShowForm(false);
      load();
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function toggleStatus(dept) {
    try {
      if (dept.status === "ACTIVE") {
        await departmentService.deactivateDepartment(dept.id);
      } else {
        await departmentService.activateDepartment(dept.id);
      }
      load();
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleDelete() {
    try {
      await departmentService.deleteDepartment(deleteTarget.id);
      setSuccessMsg("Department deleted.");
      setDeleteTarget(null);
      load();
    } catch (err) {
      setError(err.message);
      setDeleteTarget(null);
    }
  }

  return (
    <Layout>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3 className="fw-bold mb-0">Departments</h3>
        <button className="btn btn-primary" onClick={openCreate}>
          <i className="bi bi-plus-lg me-1"></i> Add Department
        </button>
      </div>

      <ErrorAlert message={error} onClose={() => setError(null)} />
      {successMsg && (
        <div className="alert alert-success alert-dismissible">
          {successMsg}
          <button className="btn-close" onClick={() => setSuccessMsg(null)}></button>
        </div>
      )}

      {showForm && (
        <div className="card ems-card mb-4">
          <div className="card-body">
            <h5>{editing ? "Edit Department" : "New Department"}</h5>
            <form onSubmit={handleSubmit} className="row g-3 align-items-end">
              <div className="col-md-4">
                <label className="form-label">Department Name</label>
                <input
                  className="form-control"
                  value={form.departmentName}
                  onChange={(e) => setForm({ ...form, departmentName: e.target.value })}
                  required
                />
              </div>
              <div className="col-md-5">
                <label className="form-label">Description</label>
                <input
                  className="form-control"
                  value={form.description}
                  onChange={(e) => setForm({ ...form, description: e.target.value })}
                />
              </div>
              <div className="col-md-3 d-flex gap-2">
                <button type="submit" className="btn btn-primary" disabled={submitting}>
                  {submitting ? "Saving..." : editing ? "Update" : "Create"}
                </button>
                <button type="button" className="btn btn-outline-secondary" onClick={() => setShowForm(false)}>
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <div className="card ems-card">
        {loading && <LoadingSpinner label="Loading departments..." />}
        {!loading && departments.length === 0 && (
          <EmptyState icon="bi-diagram-3" title="No departments yet" message="Create your first department to get started." />
        )}
        {!loading && departments.length > 0 && (
          <div className="table-responsive">
            <table className="table align-middle mb-0">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Description</th>
                  <th>Employees</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {departments.map((d) => (
                  <tr key={d.id}>
                    <td>{d.departmentName}</td>
                    <td>{d.description}</td>
                    <td>{d.employeeCount}</td>
                    <td><StatusBadge status={d.status} /></td>
                    <td className="table-actions">
                      <button className="btn btn-sm btn-outline-secondary" onClick={() => openEdit(d)}>
                        <i className="bi bi-pencil"></i>
                      </button>
                      <button
                        className={`btn btn-sm ${d.status === "ACTIVE" ? "btn-outline-warning" : "btn-outline-success"}`}
                        onClick={() => toggleStatus(d)}
                      >
                        <i className={`bi ${d.status === "ACTIVE" ? "bi-pause-circle" : "bi-play-circle"}`}></i>
                      </button>
                      {user?.role === "ADMIN" && (
                        <button className="btn btn-sm btn-outline-danger" onClick={() => setDeleteTarget(d)}>
                          <i className="bi bi-trash"></i>
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <ConfirmDialog
        show={!!deleteTarget}
        title="Delete Department"
        message={deleteTarget ? `Delete "${deleteTarget.departmentName}"? This is only possible when no employees are assigned to it.` : ""}
        onConfirm={handleDelete}
        onCancel={() => setDeleteTarget(null)}
      />
    </Layout>
  );
}
