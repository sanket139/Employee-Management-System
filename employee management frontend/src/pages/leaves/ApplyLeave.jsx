import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Layout from "../../components/Layout";
import ErrorAlert from "../../components/ErrorAlert";
import * as leaveService from "../../services/leaveService";

const emptyForm = { leaveType: "CASUAL", startDate: "", endDate: "", reason: "" };

export default function ApplyLeave() {
  const navigate = useNavigate();
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const numberOfDays =
    form.startDate && form.endDate && form.endDate >= form.startDate
      ? Math.round((new Date(form.endDate) - new Date(form.startDate)) / (1000 * 60 * 60 * 24)) + 1
      : null;

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    setSuccess(false);
    try {
      await leaveService.applyLeave(form);
      setSuccess(true);
      setForm(emptyForm);
      setTimeout(() => navigate("/leaves/my"), 1200);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Layout>
      <h3 className="fw-bold mb-4">Apply for Leave</h3>
      <ErrorAlert message={error} onClose={() => setError(null)} />
      {success && <div className="alert alert-success">Leave request submitted successfully!</div>}

      <div className="card ems-card" style={{ maxWidth: 600 }}>
        <div className="card-body">
          <form onSubmit={handleSubmit}>
            <div className="mb-3">
              <label className="form-label">Leave Type</label>
              <select className="form-select" value={form.leaveType} onChange={(e) => setForm({ ...form, leaveType: e.target.value })}>
                <option value="CASUAL">Casual</option>
                <option value="SICK">Sick</option>
                <option value="EARNED">Earned</option>
                <option value="UNPAID">Unpaid</option>
              </select>
            </div>
            <div className="row">
              <div className="col-md-6 mb-3">
                <label className="form-label">Start Date</label>
                <input type="date" className="form-control" value={form.startDate} min={new Date().toISOString().slice(0, 10)}
                  onChange={(e) => setForm({ ...form, startDate: e.target.value })} required />
              </div>
              <div className="col-md-6 mb-3">
                <label className="form-label">End Date</label>
                <input type="date" className="form-control" value={form.endDate} min={form.startDate || new Date().toISOString().slice(0, 10)}
                  onChange={(e) => setForm({ ...form, endDate: e.target.value })} required />
              </div>
            </div>
            {numberOfDays && <p className="text-muted">Total: <strong>{numberOfDays}</strong> day(s)</p>}
            <div className="mb-3">
              <label className="form-label">Reason</label>
              <textarea className="form-control" rows={3} value={form.reason} onChange={(e) => setForm({ ...form, reason: e.target.value })} required />
            </div>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? "Submitting..." : "Submit Leave Request"}
            </button>
          </form>
        </div>
      </div>
    </Layout>
  );
}
