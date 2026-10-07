import "./App.css";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import { RedirectToSignIn, Show } from "@clerk/react";
import Landing from "./pages/Landing";
import Dashboard from "./pages/Dashboard";
import Upload from "./pages/Upload";
import MyFiles from "./pages/MyFiles";
import Transactions from "./pages/Transactions";
import Subscription from "./pages/Subscription";
import PublicFileView from "./pages/PublicFileView";
import { Toaster } from "react-hot-toast";
import { UserCreditsProvider } from "./context/UserCreditsContext";

const ProtectedRoute = ({ children }) => {
  return (
    <Show when="signed-in" fallback={<RedirectToSignIn />}>
      {children}
    </Show>
  );
};

const App = () => {
  return (
    <UserCreditsProvider>
      <BrowserRouter>
        <Toaster />
        <Routes>
          {/* Public routes */}
          <Route path="/" element={<Landing />} />
          <Route path="/file/:fileId" element={<PublicFileView />} />

          {/* Protected routes */}
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />

          <Route
            path="/upload"
            element={
              <ProtectedRoute>
                <Upload />
              </ProtectedRoute>
            }
          />

          <Route
            path="/my-files"
            element={
              <ProtectedRoute>
                <MyFiles />
              </ProtectedRoute>
            }
          />

          <Route
            path="/subscriptions"
            element={
              <ProtectedRoute>
                <Subscription />
              </ProtectedRoute>
            }
          />

          <Route
            path="/transactions"
            element={
              <ProtectedRoute>
                <Transactions />
              </ProtectedRoute>
            }
          />

          {/* Unknown routes */}
          <Route path="*" element={<RedirectToSignIn />} />
        </Routes>
      </BrowserRouter>
    </UserCreditsProvider>
  );
};

export default App;
