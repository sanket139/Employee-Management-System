import { useEffect, useState, useCallback } from "react";
import Layout from "../../components/Layout";
import LoadingSpinner from "../../components/LoadingSpinner";
import ErrorAlert from "../../components/ErrorAlert";
import EmptyState from "../../components/EmptyState";
import Pagination from "../../components/Pagination";
import StatusBadge from "../../components/StatusBadge";
import ConfirmDialog from "../../components/ConfirmDialog";
import * as leaveService from "../../services/leaveService";

export default function LeaveManagement() {
  const [leaves, setLeaves] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);
  const [pendingOnly, setPendingOnly] = useState(true);
  const [page, setPage] = useState(0);
  const [confirmAction, setConfirmAction] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await leaveService.getAllLeaves({ pendingOnly, page, size: 10 });
      setLeaves(res);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [pendingOnly, page]);

  useEffect(() => {
    load();
  }, [load]);

  async function handleConfirm() {
    if (!confirmAction) return;
    try {
      if (confirmAction.type === "approve") {
        await leaveService.approveLeave(confirmAction.leave.id);
        setSuccessMsg("Leave approved and employee notified.");
      } else {
        await leaveService.rejectLeave(confirmAction.leave.id);
        setSuccessMsg("Leave rejected and employee notified.");
      }
      setConfirmAction(null);
      load();
    } catch (err) {
      setError(err.message);
      setConfirmAction(null);
    }
  }

  return (
    <Layout>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3 className="fw-bold mb-0">Leave Management</h3>
        <div className="form-check form-switch">
          <input
            className="form-check-input"
            type="checkbox"
            id="pendingOnlySwitch"
            checked={pendingOnly}
            onChange={(e) => { setPendingOnly(e.target.checked); setPage(0); }}
          />
          <label className="form-check-label" htmlFor="pendingOnlySwitch">Pending only</label>
        </div>
      </div>

      <ErrorAlert message={error} onClose={() => setError(null)} />
      {successMsg && (
        <div className="alert alert-success alert-dismissible">
          {successMsg}
          <button className="btn-close" onClick={() => setSuccessMsg(null)}></button>
        </div>
      )}

      <div className="card ems-card">
        {loading && <LoadingSpinner label="Loading leave requests..." />}
        {!loading && leaves && leaves.content.length === 0 && (
          <EmptyState icon="bi-envelope-paper" title="No leave requests found" />
        )}
        {!loading && leaves && leaves.content.length > 0 && (
          <div className="table-responsive">
            <table className="table align-middle mb-0">
              <thead>
                <tr><th>Employee</th><th>Type</th><th>Start</th><th>End</th><th>Days</th><th>Reason</th><th>Status</th><th>Actions</th></tr>
              </thead>
              <tbody>
                {leaves.content.map((l) => (
                  <tr key={l.id}>
                    <td>{l.employeeName}</td>
                    <td>{l.leaveType}</td>
                    <td>{l.startDate}</td>
                    <td>{l.endDate}</td>
                    <td>{l.numberOfDays}</td>
                    <td>{l.reason}</td>
                    <td><StatusBadge status={l.status} /></td>
                    <td className="table-actions">
                      {l.status === "PENDING" ? (
                        <>
                          <button className="btn btn-sm btn-outline-success" onClick={() => setConfirmAction({ type: "approve", leave: l })}>
                            <i className="bi bi-check-lg"></i> Approve
                          </button>
                          <button className="btn btn-sm btn-outline-danger" onClick={() => setConfirmAction({ type: "reject", leave: l })}>
                            <i className="bi bi-x-lg"></i> Reject
                          </button>
                        </>
                      ) : (
                        <span className="text-muted small">By {l.approvedBy || "-"}</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {!loading && leaves && (
          <div className="card-footer bg-white">
            <Pagination pageNumber={leaves.pageNumber} totalPages={leaves.totalPages} onPageChange={setPage} />
          </div>
        )}
      </div>

      <ConfirmDialog
        show={!!confirmAction}
        title={confirmAction?.type === "approve" ? "Approve Leave" : "Reject Leave"}
        message={
          confirmAction
            ? `Are you sure you want to ${confirmAction.type} the leave request from ${confirmAction.leave.employeeName}?`
            : ""
        }
        confirmLabel={confirmAction?.type === "approve" ? "Approve" : "Reject"}
        variant={confirmAction?.type === "approve" ? "success" : "danger"}
        onConfirm={handleConfirm}
        onCancel={() => setConfirmAction(null)}
      />
    </Layout>
  );
}
