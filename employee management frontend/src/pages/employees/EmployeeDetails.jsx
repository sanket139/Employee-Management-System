import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import Layout from "../../components/Layout";
import ErrorAlert from "../../components/ErrorAlert";
import LoadingSpinner from "../../components/LoadingSpinner";
import StatusBadge from "../../components/StatusBadge";
import ConfirmDialog from "../../components/ConfirmDialog";
import * as employeeService from "../../services/employeeService";
import * as attendanceService from "../../services/attendanceService";
import * as payrollService from "../../services/payrollService";

export default function EmployeeDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [employee, setEmployee] = useState(null);
  const [attendance, setAttendance] = useState([]);
  const [payroll, setPayroll] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    load();
  }, [id]);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const [emp, att, pay] = await Promise.all([
        employeeService.getEmployee(id),
        attendanceService.getAttendanceByEmployee(id).catch(() => []),
        payrollService.getPayrollByEmployee(id).catch(() => []),
      ]);
      setEmployee(emp);
      setAttendance(att.slice(0, 5));
      setPayroll(pay.slice(0, 5));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete() {
    setDeleting(true);
    try {
      await employeeService.deleteEmployee(id);
      navigate("/employees", { replace: true });
    } catch (err) {
      setError(err.message);
      setShowDeleteDialog(false);
    } finally {
      setDeleting(false);
    }
  }

  return (
    <Layout>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3 className="fw-bold mb-0">Employee Details</h3>
        <Link to="/employees" className="btn btn-outline-secondary">
          <i className="bi bi-arrow-left me-1"></i> Back to List
        </Link>
      </div>

      <ErrorAlert message={error} onClose={() => setError(null)} />

      {loading && <LoadingSpinner label="Loading employee details..." />}

      {!loading && employee && (
        <>
          <div className="card ems-card mb-4">
            <div className="card-body">
              <div className="d-flex justify-content-between align-items-start flex-wrap gap-3">
                <div>
                  <h4>{employee.firstName} {employee.lastName}</h4>
                  <p className="text-muted mb-1">{employee.designation} &middot; {employee.departmentName}</p>
                  <StatusBadge status={employee.status} />
                </div>
                <div className="d-flex gap-2">
                  <button className="btn btn-outline-secondary" onClick={() => navigate(`/employees/${id}/edit`)}>
                    <i className="bi bi-pencil me-1"></i> Edit
                  </button>
                  <button className="btn btn-outline-danger" onClick={() => setShowDeleteDialog(true)}>
                    <i className="bi bi-trash me-1"></i> Delete
                  </button>
                </div>
              </div>

              <hr />

              <div className="row g-3">
                <div className="col-md-4"><strong>Employee Code:</strong> {employee.employeeCode}</div>
                <div className="col-md-4"><strong>Email:</strong> {employee.email}</div>
                <div className="col-md-4"><strong>Phone:</strong> {employee.phone}</div>
                <div className="col-md-4"><strong>Gender:</strong> {employee.gender}</div>
                <div className="col-md-4"><strong>Date of Birth:</strong> {employee.dateOfBirth}</div>
                <div className="col-md-4"><strong>Joining Date:</strong> {employee.joiningDate}</div>
                <div className="col-md-4"><strong>Salary:</strong> ₹{Number(employee.salary).toLocaleString()}</div>
                <div className="col-md-4"><strong>Login Username:</strong> {employee.username || "Not linked"}</div>
                <div className="col-md-4"><strong>Address:</strong> {employee.address || "-"}</div>
              </div>
            </div>
          </div>

          <div className="row">
            <div className="col-lg-6 mb-4">
              <div className="card ems-card h-100">
                <div className="card-header bg-white fw-semibold">Recent Attendance</div>
                <div className="table-responsive">
                  <table className="table mb-0">
                    <thead><tr><th>Date</th><th>Status</th></tr></thead>
                    <tbody>
                      {attendance.length === 0 && <tr><td colSpan={2} className="text-center text-muted py-3">No records</td></tr>}
                      {attendance.map((a) => (
                        <tr key={a.id}><td>{a.attendanceDate}</td><td><StatusBadge status={a.status} /></td></tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
            <div className="col-lg-6 mb-4">
              <div className="card ems-card h-100">
                <div className="card-header bg-white fw-semibold">Recent Payroll</div>
                <div className="table-responsive">
                  <table className="table mb-0">
                    <thead><tr><th>Period</th><th>Net Salary</th><th>Status</th></tr></thead>
                    <tbody>
                      {payroll.length === 0 && <tr><td colSpan={3} className="text-center text-muted py-3">No records</td></tr>}
                      {payroll.map((p) => (
                        <tr key={p.id}>
                          <td>{p.month}/{p.year}</td>
                          <td>₹{Number(p.netSalary).toLocaleString()}</td>
                          <td><StatusBadge status={p.paymentStatus} /></td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </>
      )}

      <ConfirmDialog
        show={showDeleteDialog}
        title="Delete Employee"
        message="This will permanently delete this employee and all related attendance, leave, and payroll records. This action cannot be undone."
        confirmLabel={deleting ? "Deleting..." : "Yes, delete permanently"}
        onConfirm={handleDelete}
        onCancel={() => setShowDeleteDialog(false)}
      />
    </Layout>
  );
}
