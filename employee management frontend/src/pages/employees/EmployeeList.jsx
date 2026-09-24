import { useEffect, useState, useCallback } from "react";
import { Link, useNavigate } from "react-router-dom";
import Layout from "../../components/Layout";
import LoadingSpinner from "../../components/LoadingSpinner";
import ErrorAlert from "../../components/ErrorAlert";
import EmptyState from "../../components/EmptyState";
import Pagination from "../../components/Pagination";
import ConfirmDialog from "../../components/ConfirmDialog";
import StatusBadge from "../../components/StatusBadge";
import * as employeeService from "../../services/employeeService";
import * as departmentService from "../../services/departmentService";

export default function EmployeeList() {
  const navigate = useNavigate();
  const [employees, setEmployees] = useState(null);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  const [filters, setFilters] = useState({
    search: "",
    department: "",
    designation: "",
    status: "",
    sortBy: "id",
    sortDir: "asc",
    page: 0,
    size: 10,
  });

  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const loadEmployees = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await employeeService.getEmployees(filters);
      setEmployees(res);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => {
    departmentService.getDepartments().then(setDepartments).catch(() => {});
  }, []);

  useEffect(() => {
    loadEmployees();
  }, [loadEmployees]);

  function updateFilter(key, value) {
    setFilters((f) => ({ ...f, [key]: value, page: 0 }));
  }

  async function handleActivateToggle(emp) {
    try {
      if (emp.status === "ACTIVE") {
        await employeeService.deactivateEmployee(emp.id);
        setSuccessMsg(`${emp.firstName} ${emp.lastName} deactivated.`);
      } else {
        await employeeService.activateEmployee(emp.id);
        setSuccessMsg(`${emp.firstName} ${emp.lastName} activated.`);
      }
      loadEmployees();
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    setDeleting(true);
    try {
      await employeeService.deleteEmployee(deleteTarget.id);
      setSuccessMsg(`${deleteTarget.firstName} ${deleteTarget.lastName} was permanently deleted.`);
      setDeleteTarget(null);
      loadEmployees();
    } catch (err) {
      setError(err.message);
      setDeleteTarget(null);
    } finally {
      setDeleting(false);
    }
  }

  return (
    <Layout>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3 className="fw-bold mb-0">Employees</h3>
        <Link to="/employees/add" className="btn btn-primary">
          <i className="bi bi-plus-lg me-1"></i> Add Employee
        </Link>
      </div>

      <ErrorAlert message={error} onClose={() => setError(null)} />
      {successMsg && (
        <div className="alert alert-success alert-dismissible">
          {successMsg}
          <button className="btn-close" onClick={() => setSuccessMsg(null)}></button>
        </div>
      )}

      <div className="card ems-card mb-3">
        <div className="card-body">
          <div className="row g-2">
            <div className="col-md-4">
              <input
                className="form-control"
                placeholder="Search by name, code, or email"
                value={filters.search}
                onChange={(e) => updateFilter("search", e.target.value)}
              />
            </div>
            <div className="col-md-3">
              <select
                className="form-select"
                value={filters.department}
                onChange={(e) => updateFilter("department", e.target.value)}
              >
                <option value="">All Departments</option>
                {departments.map((d) => (
                  <option key={d.id} value={d.id}>{d.departmentName}</option>
                ))}
              </select>
            </div>
            <div className="col-md-2">
              <select
                className="form-select"
                value={filters.status}
                onChange={(e) => updateFilter("status", e.target.value)}
              >
                <option value="">All Status</option>
                <option value="ACTIVE">Active</option>
                <option value="INACTIVE">Inactive</option>
              </select>
            </div>
            <div className="col-md-3">
              <select
                className="form-select"
                value={`${filters.sortBy}-${filters.sortDir}`}
                onChange={(e) => {
                  const [sortBy, sortDir] = e.target.value.split("-");
                  setFilters((f) => ({ ...f, sortBy, sortDir, page: 0 }));
                }}
              >
                <option value="id-asc">Sort: Oldest first</option>
                <option value="id-desc">Sort: Newest first</option>
                <option value="firstName-asc">Sort: Name (A-Z)</option>
                <option value="firstName-desc">Sort: Name (Z-A)</option>
                <option value="salary-desc">Sort: Salary (High-Low)</option>
                <option value="salary-asc">Sort: Salary (Low-High)</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <div className="card ems-card">
        {loading && <LoadingSpinner label="Loading employees..." />}

        {!loading && employees && employees.content.length === 0 && (
          <EmptyState icon="bi-people" title="No employees found" message="Try adjusting your search or filters." />
        )}

        {!loading && employees && employees.content.length > 0 && (
          <div className="table-responsive">
            <table className="table align-middle mb-0">
              <thead>
                <tr>
                  <th>Employee ID</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Department</th>
                  <th>Designation</th>
                  <th>Joining Date</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {employees.content.map((emp) => (
                  <tr key={emp.id}>
                    <td>{emp.employeeCode}</td>
                    <td>{emp.firstName} {emp.lastName}</td>
                    <td>{emp.email}</td>
                    <td>{emp.departmentName}</td>
                    <td>{emp.designation}</td>
                    <td>{emp.joiningDate}</td>
                    <td><StatusBadge status={emp.status} /></td>
                    <td className="table-actions">
                      <button
                        className="btn btn-sm btn-outline-primary"
                        title="View"
                        onClick={() => navigate(`/employees/${emp.id}`)}
                      >
                        <i className="bi bi-eye"></i>
                      </button>
                      <button
                        className="btn btn-sm btn-outline-secondary"
                        title="Edit"
                        onClick={() => navigate(`/employees/${emp.id}/edit`)}
                      >
                        <i className="bi bi-pencil"></i>
                      </button>
                      <button
                        className={`btn btn-sm ${emp.status === "ACTIVE" ? "btn-outline-warning" : "btn-outline-success"}`}
                        title={emp.status === "ACTIVE" ? "Deactivate" : "Activate"}
                        onClick={() => handleActivateToggle(emp)}
                      >
                        <i className={`bi ${emp.status === "ACTIVE" ? "bi-pause-circle" : "bi-play-circle"}`}></i>
                      </button>
                      <button
                        className="btn btn-sm btn-outline-danger"
                        title="Delete"
                        onClick={() => setDeleteTarget(emp)}
                      >
                        <i className="bi bi-trash"></i>
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {!loading && employees && (
          <div className="card-footer bg-white">
            <Pagination
              pageNumber={employees.pageNumber}
              totalPages={employees.totalPages}
              onPageChange={(p) => setFilters((f) => ({ ...f, page: p }))}
            />
          </div>
        )}
      </div>

      <ConfirmDialog
        show={!!deleteTarget}
        title="Delete Employee"
        message={
          deleteTarget
            ? `This will permanently delete ${deleteTarget.firstName} ${deleteTarget.lastName} and all related attendance, leave, and payroll records. This action cannot be undone.`
            : ""
        }
        confirmLabel={deleting ? "Deleting..." : "Yes, delete permanently"}
        variant="danger"
        onConfirm={handleDelete}
        onCancel={() => setDeleteTarget(null)}
      />
    </Layout>
  );
}
