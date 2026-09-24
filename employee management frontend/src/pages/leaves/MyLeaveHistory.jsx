import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Layout from "../../components/Layout";
import LoadingSpinner from "../../components/LoadingSpinner";
import ErrorAlert from "../../components/ErrorAlert";
import EmptyState from "../../components/EmptyState";
import StatusBadge from "../../components/StatusBadge";
import * as leaveService from "../../services/leaveService";

export default function MyLeaveHistory() {
  const [leaves, setLeaves] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    load();
  }, []);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const res = await leaveService.getMyLeaves();
      setLeaves(res);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <Layout>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3 className="fw-bold mb-0">My Leave History</h3>
        <Link to="/leaves/apply" className="btn btn-primary">
          <i className="bi bi-plus-lg me-1"></i> Apply for Leave
        </Link>
      </div>

      <ErrorAlert message={error} onClose={() => setError(null)} />

      {loading && <LoadingSpinner label="Loading leave history..." />}

      {!loading && (
        <div className="card ems-card">
          {leaves.length === 0 && <EmptyState icon="bi-envelope-paper" title="No leave requests yet" />}
          {leaves.length > 0 && (
            <div className="table-responsive">
              <table className="table align-middle mb-0">
                <thead>
                  <tr><th>Type</th><th>Start</th><th>End</th><th>Days</th><th>Reason</th><th>Status</th><th>Applied On</th></tr>
                </thead>
                <tbody>
                  {leaves.map((l) => (
                    <tr key={l.id}>
                      <td>{l.leaveType}</td>
                      <td>{l.startDate}</td>
                      <td>{l.endDate}</td>
                      <td>{l.numberOfDays}</td>
                      <td>{l.reason}</td>
                      <td><StatusBadge status={l.status} /></td>
                      <td>{new Date(l.appliedDate).toLocaleDateString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </Layout>
  );
}
