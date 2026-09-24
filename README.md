<div align="center">

<h2>Employee Management System</h2>

<p>Full-Stack Web Application</p>

<p>
<b>Java • Spring Boot • Spring Security • MySQL • React.js • Vite</b>
</p>

<img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk">
<img src="https://img.shields.io/badge/Spring_Boot-3.3.4-brightgreen?style=for-the-badge&logo=springboot">
<img src="https://img.shields.io/badge/MySQL-8.0-blue?style=for-the-badge&logo=mysql">
<img src="https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react">

</div>

---

<h3>About</h3>

<p>
Employee Management System is a full-stack web application designed to manage employee-related operations with role-based access for <b>Admin, HR, and Employee</b> users.
</p>

<h3>Features</h3>

<ul>
<li>🔐 JWT Authentication & Role-Based Authorization</li>
<li>👨‍💼 Employee Management</li>
<li>🏢 Department Management</li>
<li>🕐 Attendance Management</li>
<li>📝 Leave Management</li>
<li>💰 Payroll Management</li>
<li>🔔 Notifications</li>
<li>📊 Role-Based Dashboards</li>
</ul>

<h3>Tech Stack</h3>

<p><b>Backend:</b> Java 21, Spring Boot, Spring Security, JWT, JPA/Hibernate, MySQL, Maven</p>

<p><b>Frontend:</b> React 18, Vite, JavaScript, React Router, HTML5, CSS3</p>

<p><b>Tools:</b> Git, GitHub, VS Code, MySQL Workbench</p>

<h3>Project Structure</h3>

<pre>
employee-management-system/
├── employee-management-backend/
├── employee-management-frontend/
├── .gitignore
└── README.md
</pre>

<h3>Setup</h3>

<h4>Database</h4>

<pre>
CREATE DATABASE employee_management;
</pre>

<p>Configure MySQL credentials in:</p>

<pre>
employee-management-backend/src/main/resources/application.properties
</pre>

<h4>Backend</h4>

<pre>
cd employee-management-backend
mvn clean install -DskipTests
mvn spring-boot:run
</pre>

<p>Backend: <code>http://localhost:8080</code></p>

<h4>Frontend</h4>

<pre>
cd employee-management-frontend
npm install
npm run dev
</pre>

<p>Frontend: <code>http://localhost:5173</code></p>

<h3>Demo Credentials</h3>

<table>
<tr>
<th>Role</th>
<th>Username</th>
<th>Password</th>
</tr>
<tr>
<td>Admin</td>
<td><code>admin</code></td>
<td><code>Admin@123</code></td>
</tr>
<tr>
<td>HR</td>
<td><code>hr_manager</code></td>
<td><code>Hr@12345</code></td>
</tr>
<tr>
<td>Employee</td>
<td><code>john.doe</code></td>
<td><code>Employee@123</code></td>
</tr>
</table>

<p><i>Demo credentials are for local testing only.</i></p>

<h3>Security</h3>

<ul>
<li>JWT-based authentication</li>
<li>BCrypt password hashing</li>
<li>Role-based access control</li>
<li>Protected REST APIs</li>
</ul>

