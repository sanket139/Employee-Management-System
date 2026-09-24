import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./context/AuthContext";
import ProtectedRoute, { homeForRole } from "./routes/ProtectedRoute";

import Login from "./pages/auth/Login";
import Register from "./pages/auth/Register";

import AdminDashboard from "./pages/admin/AdminDashboard";
import HrDashboard from "./pages/hr/HrDashboard";
import EmployeeDashboard from "./pages/employee/EmployeeDashboard";

import EmployeeList from "./pages/employees/EmployeeList";
import AddEmployee from "./pages/employees/AddEmployee";
import EditEmployee from "./pages/employees/EditEmployee";
import EmployeeDetails from "./pages/employees/EmployeeDetails";

import Departments from "./pages/departments/Departments";

import AttendanceManagement from "./pages/attendance/AttendanceManagement";
import MyAttendance from "./pages/attendance/MyAttendance";

import ApplyLeave from "./pages/leaves/ApplyLeave";
import MyLeaveHistory from "./pages/leaves/MyLeaveHistory";
import LeaveManagement from "./pages/leaves/LeaveManagement";

import PayrollManagement from "./pages/payroll/PayrollManagement";
import MyPayroll from "./pages/payroll/MyPayroll";

import Notifications from "./pages/notifications/Notifications";
import Profile from "./pages/profile/Profile";

function RootRedirect() {
  const { user } = useAuth();
  return <Navigate to={user ? homeForRole(user.role) : "/login"} replace />;
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Admin only */}
          <Route element={<ProtectedRoute allowedRoles={["ADMIN"]} />}>
            <Route path="/admin/dashboard" element={<AdminDashboard />} />
          </Route>

          {/* HR only */}
          <Route element={<ProtectedRoute allowedRoles={["HR"]} />}>
            <Route path="/hr/dashboard" element={<HrDashboard />} />
          </Route>

          {/* Employee only */}
          <Route element={<ProtectedRoute allowedRoles={["EMPLOYEE"]} />}>
            <Route path="/employee/dashboard" element={<EmployeeDashboard />} />
            <Route path="/attendance/my" element={<MyAttendance />} />
            <Route path="/leaves/apply" element={<ApplyLeave />} />
            <Route path="/leaves/my" element={<MyLeaveHistory />} />
            <Route path="/payroll/my" element={<MyPayroll />} />
          </Route>

          {/* Admin + HR */}
          <Route element={<ProtectedRoute allowedRoles={["ADMIN", "HR"]} />}>
            <Route path="/employees" element={<EmployeeList />} />
            <Route path="/employees/add" element={<AddEmployee />} />
            <Route path="/employees/:id" element={<EmployeeDetails />} />
            <Route path="/employees/:id/edit" element={<EditEmployee />} />
            <Route path="/departments" element={<Departments />} />
            <Route path="/attendance" element={<AttendanceManagement />} />
            <Route path="/leaves/manage" element={<LeaveManagement />} />
            <Route path="/payroll/manage" element={<PayrollManagement />} />
          </Route>

          {/* Any authenticated user */}
          <Route element={<ProtectedRoute allowedRoles={["ADMIN", "HR", "EMPLOYEE"]} />}>
            <Route path="/notifications" element={<Notifications />} />
            <Route path="/profile" element={<Profile />} />
          </Route>

          <Route path="/" element={<RootRedirect />} />
          <Route path="*" element={<RootRedirect />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
