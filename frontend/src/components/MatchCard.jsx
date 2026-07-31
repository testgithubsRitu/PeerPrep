import React from "react";

export default function MatchCard({ match, onRequest }) {
  return (
    <div className="card match-card">
      <div className="match-card-header">
        <h3>{match.name}</h3>
        <span className="score-badge">{match.matchScore}% match</span>
      </div>
      {match.college && <p className="muted">{match.college}</p>}
      <div className="match-stats">
        <span>⭐ {match.averageRating || "New"} rating</span>
        <span>{match.sessionsCompleted} sessions completed</span>
        <span>Level: {match.proficiency}</span>
      </div>
      <button onClick={() => onRequest(match)}>Request session</button>
    </div>
  );
}
