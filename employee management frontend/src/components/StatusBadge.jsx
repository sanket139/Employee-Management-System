export default function StatusBadge({ status }) {
  const map = {
    ACTIVE: "bg-success",
    INACTIVE: "bg-secondary",
    PRESENT: "bg-success",
    ABSENT: "bg-danger",
    HALF_DAY: "bg-warning text-dark",
    LEAVE: "bg-info text-dark",
    PENDING: "bg-warning text-dark",
    APPROVED: "bg-success",
    REJECTED: "bg-danger",
    PAID: "bg-success",
  };
  const cls = map[status] || "bg-secondary";
  return <span className={`badge ${cls}`}>{status}</span>;
}
