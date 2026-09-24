import { useEffect, useState, useCallback } from "react";
import Layout from "../../components/Layout";
import LoadingSpinner from "../../components/LoadingSpinner";
import ErrorAlert from "../../components/ErrorAlert";
import EmptyState from "../../components/EmptyState";
import Pagination from "../../components/Pagination";
import StatusBadge from "../../components/StatusBadge";
import * as attendanceService from "../../services/attendanceService";
import * as employeeService from "../../services/employeeService";

const emptyForm = {
  employeeId: "",
  attendanceDate: new Date().toISOString().slice(0, 10),
  checkIn: "09:00",
  checkOut: "18:00",
  status: "PRESENT",
  remarks: "",
};

export default function AttendanceManagement() {
  const [records, setRecords] = useState(null);
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  const [filters, setFilters] = useState({ employeeId: "", date: "", status: "", page: 0, size: 10 });
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    employeeService.getEmployees({ size: 200 }).then((res) => setEmployees(res.content)).catch(() => {});
  }, []);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await attendanceService.searchAttendance(filters);
      setRecords(res);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => {
    load();
  }, [load]);

  function openCreate() {
    setEditingId(null);
    setForm(emptyForm);
    setShowForm(true);
  }

  function openEdit(record) {
    setEditingId(record.id);
    setForm({
      employeeId: record.employeeId,
      attendanceDate: record.attendanceDate,
      checkIn: record.checkIn || "",
      checkOut: record.checkOut || "",
      status: record.status,
      remarks: record.remarks || "",
    });
    setShowForm(true);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const payload = { ...form, employeeId: parseInt(form.employeeId, 10) };
      if (editingId) {
        await attendanceService.updateAttendance(editingId, payload);
        setSuccessMsg("Attendance updated.");
      } else {
        await attendanceService.markAttendance(payload);
        setSuccessMsg("Attendance marked.");
      }
      setShowForm(false);
      load();
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Layout>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3 className="fw-bold mb-0">Attendance Management</h3>
        <button className="btn btn-primary" onClick={openCreate}>
          <i className="bi bi-plus-lg me-1"></i> Mark Attendance
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
            <h5>{editingId ? "Update Attendance" : "Mark Attendance"}</h5>
            <form onSubmit={handleSubmit} className="row g-3">
              <div className="col-md-3">
                <label className="form-label">Employee</label>
                <select
                  className="form-select"
                  value={form.employeeId}
                  onChange={(e) => setForm({ ...form, employeeId: e.target.value })}
                  disabled={!!editingId}
                  required
                >
                  <option value="">Select employee</option>
                  {employees.map((e) => (
                    <option key={e.id} value={e.id}>{e.firstName} {e.lastName} ({e.employeeCode})</option>
                  ))}
                </select>
              </div>
              <div className="col-md-2">
                <label className="form-label">Date</label>
                <input
                  type="date"
                  className="form-control"
                  value={form.attendanceDate}
                  onChange={(e) => setForm({ ...form, attendanceDate: e.target.value })}
                  disabled={!!editingId}
                  required
                />
              </div>
              <div className="col-md-2">
                <label className="form-label">Check In</label>
                <input type="time" className="form-control" value={form.checkIn} onChange={(e) => setForm({ ...form, checkIn: e.target.value })} />
              </div>
              <div className="col-md-2">
                <label className="form-label">Check Out</label>
                <input type="time" className="form-control" value={form.checkOut} onChange={(e) => setForm({ ...form, checkOut: e.target.value })} />
              </div>
              <div className="col-md-3">
                <label className="form-label">Status</label>
                <select className="form-select" value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
                  <option value="PRESENT">Present</option>
                  <option value="ABSENT">Absent</option>
                  <option value="HALF_DAY">Half Day</option>
                  <option value="LEAVE">Leave</option>
                </select>
              </div>
              <div className="col-md-8">
                <label className="form-label">Remarks</label>
                <input className="form-control" value={form.remarks} onChange={(e) => setForm({ ...form, remarks: e.target.value })} />
              </div>
              <div className="col-md-4 d-flex align-items-end gap-2">
                <button type="submit" className="btn btn-primary" disabled={submitting}>
                  {submitting ? "Saving..." : editingId ? "Update" : "Mark"}
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
          <div className="col-md-3">
            <input type="date" className="form-control" value={filters.date} onChange={(e) => setFilters((f) => ({ ...f, date: e.target.value, page: 0 }))} />
          </div>
          <div className="col-md-3">
            <select className="form-select" value={filters.status} onChange={(e) => setFilters((f) => ({ ...f, status: e.target.value, page: 0 }))}>
              <option value="">All Status</option>
              <option value="PRESENT">Present</option>
              <option value="ABSENT">Absent</option>
              <option value="HALF_DAY">Half Day</option>
              <option value="LEAVE">Leave</option>
            </select>
          </div>
        </div>
      </div>

      <div className="card ems-card">
        {loading && <LoadingSpinner label="Loading attendance..." />}
        {!loading && records && records.content.length === 0 && (
          <EmptyState icon="bi-calendar-check" title="No attendance records found" />
        )}
        {!loading && records && records.content.length > 0 && (
          <div className="table-responsive">
            <table className="table align-middle mb-0">
              <thead>
                <tr>
                  <th>Employee</th>
                  <th>Date</th>
                  <th>Check In</th>
                  <th>Check Out</th>
                  <th>Status</th>
                  <th>Remarks</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {records.content.map((r) => (
                  <tr key={r.id}>
                    <td>{r.employeeName} ({r.employeeCode})</td>
                    <td>{r.attendanceDate}</td>
                    <td>{r.checkIn || "-"}</td>
                    <td>{r.checkOut || "-"}</td>
                    <td><StatusBadge status={r.status} /></td>
                    <td>{r.remarks || "-"}</td>
                    <td>
                      <button className="btn btn-sm btn-outline-secondary" onClick={() => openEdit(r)}>
                        <i className="bi bi-pencil"></i>
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {!loading && records && (
          <div className="card-footer bg-white">
            <Pagination pageNumber={records.pageNumber} totalPages={records.totalPages} onPageChange={(p) => setFilters((f) => ({ ...f, page: p }))} />
          </div>
        )}
      </div>
    </Layout>
  );
}
