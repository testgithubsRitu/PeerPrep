import React, { createContext, useContext, useState } from "react";
import api from "../services/api";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem("peerprep_user");
    return stored ? JSON.parse(stored) : null;
  });

  const persist = (authResponse) => {
    localStorage.setItem("peerprep_token", authResponse.token);
    const userData = { id: authResponse.userId, name: authResponse.name };
    localStorage.setItem("peerprep_user", JSON.stringify(userData));
    setUser(userData);
  };

  const login = async (email, password) => {
    const { data } = await api.post("/auth/login", { email, password });
    persist(data);
    return data;
  };

  const register = async (payload) => {
    const { data } = await api.post("/auth/register", payload);
    persist(data);
    return data;
  };

  const logout = () => {
    localStorage.removeItem("peerprep_token");
    localStorage.removeItem("peerprep_user");
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
