import { useEffect, useState } from "react";
import Layout from "../../components/Layout";
import LoadingSpinner from "../../components/LoadingSpinner";
import ErrorAlert from "../../components/ErrorAlert";
import StatCard from "../../components/StatCard";
import StatusBadge from "../../components/StatusBadge";
import * as dashboardService from "../../services/dashboardService";

export default function EmployeeDashboard() {
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
      const res = await dashboardService.getEmployeeDashboard();
      setData(res);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <Layout>
      <h3 className="fw-bold mb-4">My Dashboard</h3>
      <ErrorAlert message={error} onClose={() => setError(null)} />

      {loading && <LoadingSpinner label="Loading dashboard..." />}

      {!loading && data && (
        <>
          <div className="card ems-card mb-4">
            <div className="card-body d-flex align-items-center">
              <div
                className="rounded-circle bg-primary-subtle d-flex align-items-center justify-content-center me-3"
                style={{ width: 64, height: 64 }}
              >
                <i className="bi bi-person fs-2 text-primary"></i>
              </div>
              <div>
                <h5 className="mb-1">{data.profile.firstName} {data.profile.lastName}</h5>
                <div className="text-muted">
                  {data.profile.designation} &middot; {data.profile.departmentName} &middot; {data.profile.employeeCode}
                </div>
              </div>
            </div>
          </div>

          <div className="row">
            <div className="col-sm-6 col-lg-3 mb-3">
              <div className="card ems-stat-card h-100">
                <div className="card-body">
                  <div className="text-muted small">Today's Attendance</div>
                  <div className="fs-5 fw-semibold mt-1"><StatusBadge status={data.todaysAttendanceStatus} /></div>
                </div>
              </div>
            </div>
            <StatCard icon="bi-graph-up" label="Monthly Attendance %" value={`${data.monthlyAttendancePercentage}%`} color="success" />
            <StatCard icon="bi-hourglass-split" label="Pending Leaves" value={data.pendingLeaves} color="warning" />
            <StatCard icon="bi-bell" label="Unread Notifications" value={data.unreadNotifications} color="danger" />
          </div>

          <div className="row">
            <StatCard icon="bi-check-circle" label="Approved Leaves" value={data.approvedLeaves} color="success" />
            <StatCard icon="bi-x-circle" label="Rejected Leaves" value={data.rejectedLeaves} color="secondary" />
          </div>

          <div className="card ems-card mb-4">
            <div className="card-header bg-white fw-semibold">Latest Payroll</div>
            <div className="card-body">
              {!data.latestPayroll && <p className="text-muted mb-0">No payroll records yet.</p>}
              {data.latestPayroll && (
                <div className="row">
                  <div className="col-md-3"><strong>Period:</strong> {data.latestPayroll.month}/{data.latestPayroll.year}</div>
                  <div className="col-md-3"><strong>Net Salary:</strong> ₹{Number(data.latestPayroll.netSalary).toLocaleString()}</div>
                  <div className="col-md-3"><strong>Status:</strong> <StatusBadge status={data.latestPayroll.paymentStatus} /></div>
                </div>
              )}
            </div>
          </div>
        </>
      )}
    </Layout>
  );
}
