import { useEffect, useState } from "react";
import Layout from "../../components/Layout";
import LoadingSpinner from "../../components/LoadingSpinner";
import ErrorAlert from "../../components/ErrorAlert";
import EmptyState from "../../components/EmptyState";
import StatusBadge from "../../components/StatusBadge";
import StatCard from "../../components/StatCard";
import * as attendanceService from "../../services/attendanceService";
import * as employeeService from "../../services/employeeService";

export default function MyAttendance() {
  const [records, setRecords] = useState([]);
  const [percentage, setPercentage] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    load();
  }, []);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const me = await employeeService.getMyProfile();
      const [history, pct] = await Promise.all([
        attendanceService.getAttendanceByEmployee(me.id),
        attendanceService.getMonthlyPercentage(me.id, new Date().getFullYear(), new Date().getMonth() + 1),
      ]);
      setRecords(history);
      setPercentage(pct);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <Layout>
      <h3 className="fw-bold mb-4">My Attendance</h3>
      <ErrorAlert message={error} onClose={() => setError(null)} />

      {loading && <LoadingSpinner label="Loading attendance history..." />}

      {!loading && (
        <>
          <div className="row mb-2">
            <StatCard icon="bi-graph-up" label="This Month's Attendance %" value={`${percentage ?? 0}%`} color="success" />
            <StatCard icon="bi-list-check" label="Total Records" value={records.length} color="primary" />
          </div>

          <div className="card ems-card">
            {records.length === 0 && <EmptyState icon="bi-calendar-check" title="No attendance history yet" />}
            {records.length > 0 && (
              <div className="table-responsive">
                <table className="table align-middle mb-0">
                  <thead>
                    <tr><th>Date</th><th>Check In</th><th>Check Out</th><th>Status</th><th>Remarks</th></tr>
                  </thead>
                  <tbody>
                    {records.map((r) => (
                      <tr key={r.id}>
                        <td>{r.attendanceDate}</td>
                        <td>{r.checkIn || "-"}</td>
                        <td>{r.checkOut || "-"}</td>
                        <td><StatusBadge status={r.status} /></td>
                        <td>{r.remarks || "-"}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </>
      )}
    </Layout>
  );
}
