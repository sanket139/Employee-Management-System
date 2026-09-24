import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Layout from "../../components/Layout";
import ErrorAlert from "../../components/ErrorAlert";
import EmployeeForm from "../../components/EmployeeForm";
import * as employeeService from "../../services/employeeService";

export default function AddEmployee() {
  const navigate = useNavigate();
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(payload) {
    setSubmitting(true);
    setError(null);
    try {
      const created = await employeeService.createEmployee(payload);
      navigate(`/employees/${created.id}`, { replace: true });
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Layout>
      <h3 className="fw-bold mb-4">Add Employee</h3>
      <ErrorAlert message={error} onClose={() => setError(null)} />
      <div className="card ems-card">
        <div className="card-body">
          <EmployeeForm onSubmit={handleSubmit} submitting={submitting} submitLabel="Create Employee" />
        </div>
      </div>
    </Layout>
  );
}
