import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../utils/api'

function ImageUpload() {
  const [file, setFile] = useState(null)
  const [description, setDescription] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const navigate = useNavigate()

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0]
    if (selectedFile) {
      // 验证文件类型
      if (!selectedFile.type.startsWith('image/')) {
        setError('请选择图片文件（支持 JPG、PNG、GIF 等格式）')
        e.target.value = '' // 清空文件选择
        return
      }
      
      // 验证文件大小（50MB限制）
      const maxSize = 50 * 1024 * 1024 // 50MB
      if (selectedFile.size > maxSize) {
        setError('文件大小不能超过 50MB')
        e.target.value = ''
        return
      }
      
      // 验证文件名
      if (!selectedFile.name || !selectedFile.name.includes('.')) {
        setError('文件名必须包含扩展名')
        e.target.value = ''
        return
      }
      
      setFile(selectedFile)
      setError('')
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!file) {
      setError('请选择要上传的图片')
      return
    }

    setLoading(true)
    setError('')

    const formData = new FormData()
    formData.append('file', file)
    if (description) {
      formData.append('description', description)
    }

    try {
      const response = await api.post('/images/upload', formData, {
        timeout: 60000 // 60秒超时
      })
      console.log('上传成功:', response.data)
      // 上传成功后刷新图片库并跳转
      navigate('/dashboard', { replace: true })
    } catch (err) {
      console.error('上传错误:', err)
      let errorMessage = '上传失败，请检查网络连接和后端服务'
      if (err.code === 'ECONNABORTED') {
        errorMessage = '上传超时，请检查网络连接或文件大小'
      } else if (err.response) {
        errorMessage = err.response.data?.error || 
                      err.response.data?.message || 
                      `服务器错误: ${err.response.status}`
      } else if (err.request) {
        errorMessage = '无法连接到服务器，请检查后端服务是否运行'
      } else {
        errorMessage = err.message || errorMessage
      }
      setError(errorMessage)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={styles.container}>
      <h2 style={styles.title}>上传图片</h2>
      {error && <div style={styles.error}>{error}</div>}
      <form onSubmit={handleSubmit} style={styles.form}>
        <div style={styles.formGroup}>
          <label style={styles.label}>选择图片</label>
          <input
            type="file"
            accept="image/*"
            onChange={handleFileChange}
            style={styles.fileInput}
          />
          {file && (
            <div style={styles.preview}>
              <img
                src={URL.createObjectURL(file)}
                alt="预览"
                style={styles.previewImage}
              />
            </div>
          )}
        </div>
        <div style={styles.formGroup}>
          <label style={styles.label}>描述（可选）</label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={4}
            style={styles.textarea}
            placeholder="输入图片描述..."
          />
        </div>
        <button type="submit" disabled={loading || !file} style={styles.button}>
          {loading ? '上传中...' : '上传'}
        </button>
      </form>
    </div>
  )
}

const styles = {
  container: {
    maxWidth: '600px',
    margin: '0 auto',
    backgroundColor: 'white',
    padding: '30px',
    borderRadius: '8px',
    boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
  },
  title: {
    fontSize: '24px',
    marginBottom: '20px',
    color: '#333'
  },
  form: {
    display: 'flex',
    flexDirection: 'column'
  },
  formGroup: {
    marginBottom: '20px'
  },
  label: {
    display: 'block',
    marginBottom: '8px',
    fontSize: '14px',
    fontWeight: '500',
    color: '#333'
  },
  fileInput: {
    width: '100%',
    padding: '10px',
    border: '1px solid #ddd',
    borderRadius: '4px'
  },
  preview: {
    marginTop: '10px'
  },
  previewImage: {
    maxWidth: '100%',
    maxHeight: '300px',
    borderRadius: '4px'
  },
  textarea: {
    width: '100%',
    padding: '10px',
    border: '1px solid #ddd',
    borderRadius: '4px',
    fontSize: '14px',
    fontFamily: 'inherit'
  },
  button: {
    padding: '12px 24px',
    backgroundColor: '#007bff',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    fontSize: '16px',
    cursor: 'pointer',
    marginTop: '10px'
  },
  error: {
    backgroundColor: '#fee',
    color: '#c33',
    padding: '12px',
    borderRadius: '4px',
    marginBottom: '20px'
  }
}

export default ImageUpload

