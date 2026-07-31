import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const SLOT_OPTIONS = ["WEEKDAY_MORNING", "WEEKDAY_EVENING", "WEEKEND_MORNING", "WEEKEND_EVENING"];

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: "", email: "", password: "", college: "" });
  const [slots, setSlots] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const toggleSlot = (slot) => {
    setSlots((prev) => (prev.includes(slot) ? prev.filter((s) => s !== slot) : [...prev, slot]));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await register({ ...form, preferredSlots: slots.join(",") });
      navigate("/profile");
    } catch (err) {
      setError(err.response?.data?.error || "Registration failed.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <form className="card auth-form" onSubmit={handleSubmit}>
        <h2>Create your PeerPrep account</h2>
        {error && <p className="error-text">{error}</p>}
        <label>Name</label>
        <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
        <label>Email</label>
        <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
        <label>Password</label>
        <input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required minLength={6} />
        <label>College (optional)</label>
        <input value={form.college} onChange={(e) => setForm({ ...form, college: e.target.value })} />

        <label>When are you usually free to practice?</label>
        <div className="slot-grid">
          {SLOT_OPTIONS.map((slot) => (
            <button
              type="button"
              key={slot}
              className={`slot-chip ${slots.includes(slot) ? "active" : ""}`}
              onClick={() => toggleSlot(slot)}
            >
              {slot.replace("_", " ").toLowerCase()}
            </button>
          ))}
        </div>

        <button type="submit" disabled={loading}>{loading ? "Creating account..." : "Sign up"}</button>
        <p className="muted">
          Already have an account? <Link to="/login">Log in</Link>
        </p>
      </form>
    </div>
  );
}
