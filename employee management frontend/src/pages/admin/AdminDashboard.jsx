import { useEffect, useState } from "react";
import Layout from "../../components/Layout";
import LoadingSpinner from "../../components/LoadingSpinner";
import ErrorAlert from "../../components/ErrorAlert";
import StatCard from "../../components/StatCard";
import StatusBadge from "../../components/StatusBadge";
import * as dashboardService from "../../services/dashboardService";

export default function AdminDashboard() {
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
      const res = await dashboardService.getAdminDashboard();
      setData(res);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <Layout>
      <h3 className="fw-bold mb-4">Admin Dashboard</h3>
      <ErrorAlert message={error} onClose={() => setError(null)} />

      {loading && <LoadingSpinner label="Loading dashboard..." />}

      {!loading && data && (
        <>
          <div className="row">
            <StatCard icon="bi-people" label="Total Employees" value={data.totalEmployees} color="primary" />
            <StatCard icon="bi-person-check" label="Active Employees" value={data.activeEmployees} color="success" />
            <StatCard icon="bi-person-dash" label="Inactive Employees" value={data.inactiveEmployees} color="secondary" />
            <StatCard icon="bi-diagram-3" label="Departments" value={data.totalDepartments} color="info" />
          </div>
          <div className="row">
            <StatCard icon="bi-calendar-check" label="Present Today" value={data.todaysPresent} color="success" />
            <StatCard icon="bi-calendar-x" label="Absent Today" value={data.todaysAbsent} color="danger" />
            <StatCard icon="bi-airplane" label="On Leave Today" value={data.employeesOnLeave} color="warning" />
            <StatCard icon="bi-envelope-paper" label="Pending Leave Requests" value={data.pendingLeaveRequests} color="warning" />
          </div>
          <div className="row">
            <StatCard
              icon="bi-cash-coin"
              label="This Month's Payroll"
              value={`₹${Number(data.monthlyPayroll || 0).toLocaleString()}`}
              color="success"
            />
          </div>

          <div className="row mt-3">
            <div className="col-lg-7 mb-4">
              <div className="card ems-card h-100">
                <div className="card-header bg-white fw-semibold">Recent Employees</div>
                <div className="table-responsive">
                  <table className="table mb-0 align-middle">
                    <thead>
                      <tr>
                        <th>Code</th>
                        <th>Name</th>
                        <th>Department</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {data.recentEmployees.length === 0 && (
                        <tr><td colSpan={4} className="text-center text-muted py-4">No employees yet</td></tr>
                      )}
                      {data.recentEmployees.map((e) => (
                        <tr key={e.id}>
                          <td>{e.employeeCode}</td>
                          <td>{e.firstName} {e.lastName}</td>
                          <td>{e.departmentName}</td>
                          <td><StatusBadge status={e.status} /></td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>

            <div className="col-lg-5 mb-4">
              <div className="card ems-card h-100">
                <div className="card-header bg-white fw-semibold">Recent Leave Requests</div>
                <div className="table-responsive">
                  <table className="table mb-0 align-middle">
                    <thead>
                      <tr>
                        <th>Employee</th>
                        <th>Type</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {data.recentLeaveRequests.length === 0 && (
                        <tr><td colSpan={3} className="text-center text-muted py-4">No leave requests yet</td></tr>
                      )}
                      {data.recentLeaveRequests.map((l) => (
                        <tr key={l.id}>
                          <td>{l.employeeName}</td>
                          <td>{l.leaveType}</td>
                          <td><StatusBadge status={l.status} /></td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
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
