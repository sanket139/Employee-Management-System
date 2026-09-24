import { useEffect, useState } from "react";
import Layout from "../../components/Layout";
import LoadingSpinner from "../../components/LoadingSpinner";
import ErrorAlert from "../../components/ErrorAlert";
import StatCard from "../../components/StatCard";
import * as dashboardService from "../../services/dashboardService";

export default function HrDashboard() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    load();
  }, []);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const res = await dashboardService.getHrDashboard();
      setData(res);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <Layout>
      <h3 className="fw-bold mb-4">HR Dashboard</h3>
      <ErrorAlert message={error} onClose={() => setError(null)} />

      {loading && <LoadingSpinner label="Loading dashboard..." />}

      {!loading && data && (
        <>
          <div className="row">
            <StatCard icon="bi-people" label="Total Employees" value={data.totalEmployees} color="primary" />
            <StatCard icon="bi-person-check" label="Active Employees" value={data.activeEmployees} color="success" />
            <StatCard icon="bi-calendar-check" label="Present Today" value={data.todaysAttendance} color="success" />
            <StatCard icon="bi-envelope-paper" label="Pending Leave Requests" value={data.pendingLeaveRequests} color="warning" />
          </div>
          <div className="row">
            <StatCard icon="bi-airplane" label="Employees On Leave Today" value={data.employeesOnLeave} color="info" />
          </div>

          <div className="card ems-card mb-4">
            <div className="card-header bg-white fw-semibold">Department-wise Employee Count</div>
            <div className="card-body">
              {Object.keys(data.departmentWiseCount).length === 0 && (
                <p className="text-muted mb-0">No departments yet.</p>
              )}
              {Object.entries(data.departmentWiseCount).map(([name, count]) => (
                <div key={name} className="d-flex justify-content-between border-bottom py-2">
                  <span>{name}</span>
                  <strong>{count}</strong>
                </div>
              ))}
            </div>
          </div>
        </>
      )}
    </Layout>
  );
}
