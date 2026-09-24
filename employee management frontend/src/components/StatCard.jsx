export default function StatCard({ icon, label, value, color = "primary" }) {
  return (
    <div className="col-sm-6 col-lg-3 mb-3">
      <div className="card ems-stat-card h-100">
        <div className="card-body d-flex align-items-center">
          <div
            className={`rounded-circle d-flex align-items-center justify-content-center me-3 bg-${color}-subtle`}
            style={{ width: 48, height: 48 }}
          >
            <i className={`bi ${icon} fs-4 text-${color}`}></i>
          </div>
          <div>
            <div className="text-muted small">{label}</div>
            <div className="fs-4 fw-semibold">{value}</div>
          </div>
        </div>
      </div>
    </div>
  );
}
