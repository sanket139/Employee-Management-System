export default function EmptyState({ icon = "bi-inbox", title = "Nothing here yet", message }) {
  return (
    <div className="text-center py-5 text-muted">
      <i className={`bi ${icon} fs-1 d-block mb-2`}></i>
      <h5>{title}</h5>
      {message && <p className="mb-0">{message}</p>}
    </div>
  );
}
