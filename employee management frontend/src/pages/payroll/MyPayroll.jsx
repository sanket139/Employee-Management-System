import { useEffect, useState } from "react";
import Layout from "../../components/Layout";
import LoadingSpinner from "../../components/LoadingSpinner";
import ErrorAlert from "../../components/ErrorAlert";
import EmptyState from "../../components/EmptyState";
import StatusBadge from "../../components/StatusBadge";
import * as payrollService from "../../services/payrollService";
import * as employeeService from "../../services/employeeService";

export default function MyPayroll() {
  const [payroll, setPayroll] = useState([]);
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
      const res = await payrollService.getPayrollByEmployee(me.id);
      setPayroll(res);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <Layout>
      <h3 className="fw-bold mb-4">My Payroll</h3>
      <ErrorAlert message={error} onClose={() => setError(null)} />

      {loading && <LoadingSpinner label="Loading payroll history..." />}

      {!loading && (
        <div className="card ems-card">
          {payroll.length === 0 && <EmptyState icon="bi-cash-coin" title="No payroll records yet" />}
          {payroll.length > 0 && (
            <div className="table-responsive">
              <table className="table align-middle mb-0">
                <thead>
                  <tr><th>Period</th><th>Basic</th><th>Allowance</th><th>Deduction</th><th>Net Salary</th><th>Status</th><th>Payment Date</th></tr>
                </thead>
                <tbody>
                  {payroll.map((p) => (
                    <tr key={p.id}>
                      <td>{p.month}/{p.year}</td>
                      <td>₹{Number(p.basicSalary).toLocaleString()}</td>
                      <td>₹{Number(p.allowance).toLocaleString()}</td>
                      <td>₹{Number(p.deduction).toLocaleString()}</td>
                      <td><strong>₹{Number(p.netSalary).toLocaleString()}</strong></td>
                      <td><StatusBadge status={p.paymentStatus} /></td>
                      <td>{p.paymentDate || "-"}</td>
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
