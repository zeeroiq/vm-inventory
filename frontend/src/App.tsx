import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import Projects from './pages/Projects';
import ProjectDetails from './pages/ProjectDetails';
import Instances from './pages/Instances';
import Deployments from './pages/Deployments';
import Administration from './pages/Administration';
import { AuthProvider } from './context/AuthContext';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Layout />}>
            <Route index element={<Dashboard />} />
            <Route path="projects" element={<Projects />} />
            <Route path="projects/:id" element={<ProjectDetails />} />
            <Route path="instances" element={<Instances />} />
            <Route path="deployments" element={<Deployments />} />
            <Route path="admin" element={<Administration />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
