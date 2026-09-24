import { useEffect, useState } from "react";
import Layout from "../../components/Layout";
import LoadingSpinner from "../../components/LoadingSpinner";
import ErrorAlert from "../../components/ErrorAlert";
import StatusBadge from "../../components/StatusBadge";
import { useAuth } from "../../context/AuthContext";
import * as employeeService from "../../services/employeeService";

export default function Profile() {
  const { user } = useAuth();
  const [employee, setEmployee] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    load();
  }, []);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const res = await employeeService.getMyProfile();
      setEmployee(res);
    } catch (err) {
      if (err.status === 404) {
        setNotFound(true);
      } else {
        setError(err.message);
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <Layout>
      <h3 className="fw-bold mb-4">My Profile</h3>
      <ErrorAlert message={error} onClose={() => setError(null)} />

      {loading && <LoadingSpinner label="Loading profile..." />}

      {!loading && notFound && (
        <div className="card ems-card">
          <div className="card-body">
            <p className="mb-2"><strong>Username:</strong> {user?.username}</p>
            <p className="mb-2"><strong>Email:</strong> {user?.email}</p>
            <p className="mb-0"><strong>Role:</strong> {user?.role}</p>
            <div className="alert alert-info mt-3 mb-0">
              No employee record is linked to this account yet.
            </div>
          </div>
        </div>
      )}

      {!loading && employee && (
        <div className="card ems-card">
          <div className="card-body">
            <div className="d-flex align-items-center mb-3">
              <div
                className="rounded-circle bg-primary-subtle d-flex align-items-center justify-content-center me-3"
                style={{ width: 64, height: 64 }}
              >
                <i className="bi bi-person fs-2 text-primary"></i>
              </div>
              <div>
                <h5 className="mb-1">{employee.firstName} {employee.lastName}</h5>
                <StatusBadge status={employee.status} />
              </div>
            </div>
            <hr />
            <div className="row g-3">
              <div className="col-md-4"><strong>Employee Code:</strong> {employee.employeeCode}</div>
              <div className="col-md-4"><strong>Designation:</strong> {employee.designation}</div>
              <div className="col-md-4"><strong>Department:</strong> {employee.departmentName}</div>
              <div className="col-md-4"><strong>Email:</strong> {employee.email}</div>
              <div className="col-md-4"><strong>Phone:</strong> {employee.phone}</div>
              <div className="col-md-4"><strong>Joining Date:</strong> {employee.joiningDate}</div>
              <div className="col-md-4"><strong>Gender:</strong> {employee.gender}</div>
              <div className="col-md-4"><strong>Date of Birth:</strong> {employee.dateOfBirth}</div>
              <div className="col-md-4"><strong>Username:</strong> {employee.username}</div>
              <div className="col-md-8"><strong>Address:</strong> {employee.address || "-"}</div>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}
