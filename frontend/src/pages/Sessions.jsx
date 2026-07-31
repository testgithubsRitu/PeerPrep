import React, { useEffect, useState } from "react";
import api from "../services/api";
import Navbar from "../components/Navbar";

export default function Sessions() {
  const [sessions, setSessions] = useState([]);
  const [me, setMe] = useState(null);
  const [feedbackFor, setFeedbackFor] = useState(null);
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState("");
  const [message, setMessage] = useState("");

  const load = async () => {
    const [sessionsRes, meRes] = await Promise.all([api.get("/sessions"), api.get("/users/me")]);
    setSessions(sessionsRes.data);
    setMe(meRes.data);
  };

  useEffect(() => {
    load();
  }, []);

  const act = async (id, action) => {
    setMessage("");
    try {
      await api.post(`/sessions/${id}/${action}`);
      load();
    } catch (err) {
      setMessage(err.response?.data?.error || "Action failed.");
    }
  };

  const submitFeedback = async (e) => {
    e.preventDefault();
    try {
      await api.post(`/sessions/${feedbackFor}/feedback`, { rating: Number(rating), comment });
      setFeedbackFor(null);
      setComment("");
      setMessage("Feedback submitted.");
      load();
    } catch (err) {
      setMessage(err.response?.data?.error || "Couldn't submit feedback.");
    }
  };

  if (!me) return null;

  return (
    <>
      <Navbar />
      <div className="page">
        <h2>My sessions</h2>
        <div className="stats-bar">
          <span>🔥 {me.currentStreak}-day streak</span>
          <span>🏆 Best: {me.longestStreak} days</span>
          <span>✅ {me.sessionsCompleted} completed</span>
          <span>⭐ {me.averageRating || "No ratings yet"}</span>
        </div>
        {message && <p className="muted">{message}</p>}

        {sessions.length === 0 && <p className="muted">No sessions yet — go find a partner on the dashboard.</p>}

        {sessions.map((s) => {
          const isPartner = s.partner.id === me.id;
          const otherPerson = isPartner ? s.requester : s.partner;
          return (
            <div className="card session-row" key={s.id}>
              <div>
                <strong>{s.skill.name}</strong> with {otherPerson.name}
                <div className="muted">{new Date(s.scheduledTime).toLocaleString()} · {s.status}</div>
              </div>
              <div className="session-actions">
                {s.status === "PENDING" && isPartner && (
                  <>
                    <button onClick={() => act(s.id, "accept")}>Accept</button>
                    <button className="secondary" onClick={() => act(s.id, "decline")}>Decline</button>
                  </>
                )}
                {s.status === "ACCEPTED" && (
                  <button onClick={() => act(s.id, "complete")}>Mark complete</button>
                )}
                {s.status === "COMPLETED" && (
                  <button className="secondary" onClick={() => setFeedbackFor(s.id)}>Leave feedback</button>
                )}
              </div>
            </div>
          );
        })}

        {feedbackFor && (
          <form className="card auth-form" onSubmit={submitFeedback}>
            <h3>Rate your session</h3>
            <label>Rating (1-5)</label>
            <input type="number" min="1" max="5" value={rating} onChange={(e) => setRating(e.target.value)} />
            <label>Comment</label>
            <textarea value={comment} onChange={(e) => setComment(e.target.value)} rows={3} />
            <button type="submit">Submit</button>
            <button type="button" className="secondary" onClick={() => setFeedbackFor(null)}>Cancel</button>
          </form>
        )}
      </div>
    </>
  );
}
