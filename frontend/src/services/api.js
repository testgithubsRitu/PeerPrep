import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
});

// Attach the JWT to every request once the user is logged in
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("peerprep_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// If the token expires or is invalid, bounce back to login
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem("peerprep_token");
      localStorage.removeItem("peerprep_user");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export default api;
