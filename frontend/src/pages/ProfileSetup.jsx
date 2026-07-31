import React, { useEffect, useState } from "react";
import api from "../services/api";
import Navbar from "../components/Navbar";

const COMMON_SKILLS = ["DSA", "SQL", "System Design", "OOPs", "DBMS", "HR/Behavioral", "Operating Systems", "React"];

export default function ProfileSetup() {
  const [skills, setSkills] = useState([]);
  const [skillName, setSkillName] = useState(COMMON_SKILLS[0]);
  const [type, setType] = useState("WANT");
  const [proficiency, setProficiency] = useState("BEGINNER");
  const [message, setMessage] = useState("");

  const loadSkills = async () => {
    const { data } = await api.get("/users/me/skills");
    setSkills(data);
  };

  useEffect(() => {
    loadSkills();
  }, []);

  const handleAdd = async (e) => {
    e.preventDefault();
    setMessage("");
    try {
      await api.post("/users/me/skills", { skillName, type, proficiency });
      setMessage(`Saved: ${type === "WANT" ? "practicing" : "offering"} ${skillName} (${proficiency.toLowerCase()})`);
      loadSkills();
    } catch (err) {
      setMessage(err.response?.data?.error || "Couldn't save that skill.");
    }
  };

  const offered = skills.filter((s) => s.type === "OFFER");
  const wanted = skills.filter((s) => s.type === "WANT");

  return (
    <>
      <Navbar />
      <div className="page">
        <h2>Your skills</h2>
        <p className="muted">
          Add topics you can help others practice ("OFFER") and topics you want to practice yourself ("WANT").
          The matcher pairs a WANT with a slightly stronger OFFER.
        </p>

        <form className="card inline-form" onSubmit={handleAdd}>
          <select value={skillName} onChange={(e) => setSkillName(e.target.value)}>
            {COMMON_SKILLS.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
          <select value={type} onChange={(e) => setType(e.target.value)}>
            <option value="WANT">I want to practice this</option>
            <option value="OFFER">I can help others with this</option>
          </select>
          <select value={proficiency} onChange={(e) => setProficiency(e.target.value)}>
            <option value="BEGINNER">Beginner</option>
            <option value="INTERMEDIATE">Intermediate</option>
            <option value="ADVANCED">Advanced</option>
          </select>
          <button type="submit">Add</button>
        </form>
        {message && <p className="muted">{message}</p>}

        <div className="two-col">
          <div>
            <h3>Practicing (WANT)</h3>
            {wanted.length === 0 && <p className="muted">Nothing added yet.</p>}
            {wanted.map((s) => (
              <div key={s.id} className="skill-pill">{s.skill.name} · {s.proficiency.toLowerCase()}</div>
            ))}
          </div>
          <div>
            <h3>Offering (OFFER)</h3>
            {offered.length === 0 && <p className="muted">Nothing added yet.</p>}
            {offered.map((s) => (
              <div key={s.id} className="skill-pill">{s.skill.name} · {s.proficiency.toLowerCase()}</div>
            ))}
          </div>
        </div>
      </div>
    </>
  );
}
