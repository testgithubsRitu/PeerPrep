import React, { useState } from "react";
import api from "../services/api";
import Navbar from "../components/Navbar";
import MatchCard from "../components/MatchCard";

const COMMON_SKILLS = ["DSA", "SQL", "System Design", "OOPs", "DBMS", "HR/Behavioral", "Operating Systems", "React"];

export default function Dashboard() {
  const [skill, setSkill] = useState(COMMON_SKILLS[0]);
  const [matches, setMatches] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [requestedTime, setRequestedTime] = useState("");
  const [message, setMessage] = useState("");

  const search = async () => {
    setLoading(true);
    setError("");
    try {
      const { data } = await api.get("/matches", { params: { skill } });
      setMatches(data);
    } catch (err) {
      setError(err.response?.data?.error || "Couldn't load matches.");
    } finally {
      setLoading(false);
    }
  };

  const requestSession = async (match) => {
    if (!requestedTime) {
      setMessage("Pick a date/time first.");
      return;
    }
    try {
      await api.post("/sessions", {
        partnerId: match.userId,
        skillName: skill,
        scheduledTime: requestedTime,
      });
      setMessage(`Session request sent to ${match.name}.`);
    } catch (err) {
      setMessage(err.response?.data?.error || "Couldn't send the request.");
    }
  };

  return (
    <>
      <Navbar />
      <div className="page">
        <h2>Find a practice partner</h2>
        <p className="muted">
          Ranked by a match score that blends proficiency gap, rating, availability overlap and experience —
          not just "first come first served".
        </p>

        <div className="card inline-form">
          <select value={skill} onChange={(e) => setSkill(e.target.value)}>
            {COMMON_SKILLS.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
          <input
            type="datetime-local"
            value={requestedTime}
            onChange={(e) => setRequestedTime(e.target.value)}
          />
          <button onClick={search} disabled={loading}>{loading ? "Searching..." : "Find partners"}</button>
        </div>

        {error && <p className="error-text">{error}</p>}
        {message && <p className="muted">{message}</p>}

        <div className="match-grid">
          {matches.map((m) => (
            <MatchCard key={m.userId} match={m} onRequest={requestSession} />
          ))}
          {matches.length === 0 && !loading && <p className="muted">No matches yet — try a different topic, or check back once more people have added it.</p>}
        </div>
      </div>
    </>
  );
}
