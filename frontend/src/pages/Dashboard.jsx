import React from 'react'
import { Outlet, Link, useNavigate } from 'react-router-dom'
import { removeToken } from '../utils/auth'
import { getUser } from '../utils/auth'

function Dashboard({ setIsAuthenticated }) {
  const navigate = useNavigate()
  const user = getUser()

  const handleLogout = () => {
    removeToken()
    setIsAuthenticated(false)
    navigate('/login')
  }

  return (
    <div style={styles.container}>
      <nav style={styles.nav}>
        <div style={styles.navContent}>
          <h1 style={styles.logo}>PictureX</h1>
          <div style={styles.navLinks}>
            <Link to="/dashboard" style={styles.navLink}>图片库</Link>
            <Link to="/dashboard/upload" style={styles.navLink}>上传图片</Link>
            <span style={styles.userInfo}>欢迎, {user?.username}</span>
            <button onClick={handleLogout} style={styles.logoutBtn}>退出</button>
          </div>
        </div>
      </nav>
      <main style={styles.main}>
        <Outlet />
      </main>
    </div>
  )
}

const styles = {
  container: {
    minHeight: '100vh',
    backgroundColor: '#f5f5f5'
  },
  nav: {
    backgroundColor: '#fff',
    boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
    padding: '0 20px'
  },
  navContent: {
    maxWidth: '1200px',
    margin: '0 auto',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    height: '60px'
  },
  logo: {
    fontSize: '24px',
    fontWeight: 'bold',
    color: '#007bff'
  },
  navLinks: {
    display: 'flex',
    alignItems: 'center',
    gap: '20px'
  },
  navLink: {
    textDecoration: 'none',
    color: '#333',
    fontSize: '16px',
    padding: '8px 16px',
    borderRadius: '4px',
    transition: 'background-color 0.2s'
  },
  userInfo: {
    color: '#666',
    fontSize: '14px'
  },
  logoutBtn: {
    padding: '8px 16px',
    backgroundColor: '#dc3545',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: '14px'
  },
  main: {
    maxWidth: '1200px',
    margin: '0 auto',
    padding: '20px'
  }
}

export default Dashboard

