import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <nav className="navbar">
      <Link to="/" className="brand">PeerPrep</Link>
      {user && (
        <div className="nav-links">
          <Link to="/dashboard">Find a partner</Link>
          <Link to="/sessions">My sessions</Link>
          <Link to="/profile">Profile</Link>
          <span className="nav-user">Hi, {user.name.split(" ")[0]}</span>
          <button className="link-btn" onClick={handleLogout}>Log out</button>
        </div>
      )}
    </nav>
  );
}
