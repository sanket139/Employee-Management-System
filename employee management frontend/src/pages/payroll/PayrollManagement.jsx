import { useEffect, useState, useCallback } from "react";
import Layout from "../../components/Layout";
import LoadingSpinner from "../../components/LoadingSpinner";
import ErrorAlert from "../../components/ErrorAlert";
import EmptyState from "../../components/EmptyState";
import Pagination from "../../components/Pagination";
import StatusBadge from "../../components/StatusBadge";
import * as payrollService from "../../services/payrollService";
import * as employeeService from "../../services/employeeService";

const emptyForm = { employeeId: "", month: new Date().getMonth() + 1, year: new Date().getFullYear(), basicSalary: "", allowance: "0", deduction: "0" };

export default function PayrollManagement() {
  const [payroll, setPayroll] = useState(null);
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  const [filters, setFilters] = useState({ employeeId: "", month: "", year: "", page: 0, size: 10 });
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [submitting, setSubmitting] = useState(false);

  const netPreview =
    form.basicSalary !== "" && !isNaN(form.basicSalary)
      ? (parseFloat(form.basicSalary || 0) + parseFloat(form.allowance || 0) - parseFloat(form.deduction || 0)).toFixed(2)
      : null;

  useEffect(() => {
    employeeService.getEmployees({ size: 200 }).then((res) => setEmployees(res.content)).catch(() => {});
  }, []);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await payrollService.searchPayroll(filters);
      setPayroll(res);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => {
    load();
  }, [load]);

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const payload = {
        employeeId: parseInt(form.employeeId, 10),
        month: parseInt(form.month, 10),
        year: parseInt(form.year, 10),
        basicSalary: parseFloat(form.basicSalary),
        allowance: parseFloat(form.allowance || 0),
        deduction: parseFloat(form.deduction || 0),
      };
      await payrollService.createPayroll(payload);
      setSuccessMsg("Payroll created. Net salary calculated by the backend.");
      setShowForm(false);
      setForm(emptyForm);
      load();
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function handleMarkPaid(id) {
    try {
      await payrollService.markPayrollPaid(id);
      setSuccessMsg("Payroll marked as paid.");
      load();
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <Layout>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3 className="fw-bold mb-0">Payroll Management</h3>
        <button className="btn btn-primary" onClick={() => setShowForm(true)}>
          <i className="bi bi-plus-lg me-1"></i> Create Payroll
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
            <h5>New Payroll Record</h5>
            <form onSubmit={handleSubmit} className="row g-3">
              <div className="col-md-4">
                <label className="form-label">Employee</label>
                <select className="form-select" value={form.employeeId} onChange={(e) => setForm({ ...form, employeeId: e.target.value })} required>
                  <option value="">Select employee</option>
                  {employees.map((e) => (
                    <option key={e.id} value={e.id}>{e.firstName} {e.lastName} ({e.employeeCode})</option>
                  ))}
                </select>
              </div>
              <div className="col-md-2">
                <label className="form-label">Month</label>
                <input type="number" min="1" max="12" className="form-control" value={form.month} onChange={(e) => setForm({ ...form, month: e.target.value })} required />
              </div>
              <div className="col-md-2">
                <label className="form-label">Year</label>
                <input type="number" min="2000" className="form-control" value={form.year} onChange={(e) => setForm({ ...form, year: e.target.value })} required />
              </div>
              <div className="col-md-4">
                <label className="form-label">Basic Salary</label>
                <input type="number" min="0" step="0.01" className="form-control" value={form.basicSalary} onChange={(e) => setForm({ ...form, basicSalary: e.target.value })} required />
              </div>
              <div className="col-md-4">
                <label className="form-label">Allowance</label>
                <input type="number" min="0" step="0.01" className="form-control" value={form.allowance} onChange={(e) => setForm({ ...form, allowance: e.target.value })} />
              </div>
              <div className="col-md-4">
                <label className="form-label">Deduction</label>
                <input type="number" min="0" step="0.01" className="form-control" value={form.deduction} onChange={(e) => setForm({ ...form, deduction: e.target.value })} />
              </div>
              <div className="col-md-4 d-flex align-items-end">
                {netPreview !== null && (
                  <p className="mb-0"><strong>Net Salary (calculated by backend):</strong> ₹{netPreview}</p>
                )}
              </div>
              <div className="col-12 d-flex gap-2">
                <button type="submit" className="btn btn-primary" disabled={submitting}>
                  {submitting ? "Saving..." : "Create Payroll"}
                </button>
                <button type="button" className="btn btn-outline-secondary" onClick={() => setShowForm(false)}>
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <div className="card ems-card mb-3">
        <div className="card-body row g-2">
          <div className="col-md-4">
            <select className="form-select" value={filters.employeeId} onChange={(e) => setFilters((f) => ({ ...f, employeeId: e.target.value, page: 0 }))}>
              <option value="">All Employees</option>
              {employees.map((e) => (
                <option key={e.id} value={e.id}>{e.firstName} {e.lastName}</option>
              ))}
            </select>
          </div>
          <div className="col-md-2">
            <input type="number" min="1" max="12" className="form-control" placeholder="Month" value={filters.month} onChange={(e) => setFilters((f) => ({ ...f, month: e.target.value, page: 0 }))} />
          </div>
          <div className="col-md-2">
            <input type="number" className="form-control" placeholder="Year" value={filters.year} onChange={(e) => setFilters((f) => ({ ...f, year: e.target.value, page: 0 }))} />
          </div>
        </div>
      </div>

      <div className="card ems-card">
        {loading && <LoadingSpinner label="Loading payroll..." />}
        {!loading && payroll && payroll.content.length === 0 && <EmptyState icon="bi-cash-coin" title="No payroll records found" />}
        {!loading && payroll && payroll.content.length > 0 && (
          <div className="table-responsive">
            <table className="table align-middle mb-0">
              <thead>
                <tr><th>Employee</th><th>Period</th><th>Basic</th><th>Allowance</th><th>Deduction</th><th>Net Salary</th><th>Status</th><th>Actions</th></tr>
              </thead>
              <tbody>
                {payroll.content.map((p) => (
                  <tr key={p.id}>
                    <td>{p.employeeName}</td>
                    <td>{p.month}/{p.year}</td>
                    <td>₹{Number(p.basicSalary).toLocaleString()}</td>
                    <td>₹{Number(p.allowance).toLocaleString()}</td>
                    <td>₹{Number(p.deduction).toLocaleString()}</td>
                    <td><strong>₹{Number(p.netSalary).toLocaleString()}</strong></td>
                    <td><StatusBadge status={p.paymentStatus} /></td>
                    <td>
                      {p.paymentStatus === "PENDING" && (
                        <button className="btn btn-sm btn-outline-success" onClick={() => handleMarkPaid(p.id)}>
                          Mark Paid
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {!loading && payroll && (
          <div className="card-footer bg-white">
            <Pagination pageNumber={payroll.pageNumber} totalPages={payroll.totalPages} onPageChange={(p) => setFilters((f) => ({ ...f, page: p }))} />
          </div>
        )}
      </div>
    </Layout>
  );
}
