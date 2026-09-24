import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Layout from "../../components/Layout";
import ErrorAlert from "../../components/ErrorAlert";
import LoadingSpinner from "../../components/LoadingSpinner";
import EmployeeForm from "../../components/EmployeeForm";
import * as employeeService from "../../services/employeeService";

export default function EditEmployee() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [employee, setEmployee] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    load();
  }, [id]);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const res = await employeeService.getEmployee(id);
      setEmployee(res);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleSubmit(payload) {
    setSubmitting(true);
    setError(null);
    try {
      await employeeService.updateEmployee(id, payload);
      navigate(`/employees/${id}`, { replace: true });
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Layout>
      <h3 className="fw-bold mb-4">Edit Employee</h3>
      <ErrorAlert message={error} onClose={() => setError(null)} />

      {loading && <LoadingSpinner label="Loading employee..." />}

      {!loading && employee && (
        <div className="card ems-card">
          <div className="card-body">
            <EmployeeForm
              initialValues={{
                firstName: employee.firstName,
                lastName: employee.lastName,
                email: employee.email,
                phone: employee.phone,
                address: employee.address || "",
                dateOfBirth: employee.dateOfBirth,
                gender: employee.gender,
                joiningDate: employee.joiningDate,
                designation: employee.designation,
                salary: employee.salary,
                departmentId: employee.departmentId,
              }}
              onSubmit={handleSubmit}
              submitting={submitting}
              submitLabel="Save Changes"
              showCredentials={false}
            />
          </div>
        </div>
      )}
    </Layout>
  );
}
