import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import ReactCrop from 'react-image-crop'
import 'react-image-crop/dist/ReactCrop.css'
import api from '../utils/api'

function ImageEdit() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [image, setImage] = useState(null)
  const [imageSrc, setImageSrc] = useState(null)
  const [crop, setCrop] = useState(null)
  const [brightness, setBrightness] = useState(1.0)
  const [contrast, setContrast] = useState(100)
  const [loading, setLoading] = useState(true)
  const [processing, setProcessing] = useState(false)
  const [tagName, setTagName] = useState('')

  useEffect(() => {
    loadImage()
  }, [id])

  const loadImage = async () => {
    try {
      const response = await api.get(`/images/${id}`)
      setImage(response.data)
      setImageSrc(`http://localhost:8080/api/images/file/${response.data.filename}`)
    } catch (error) {
      console.error('加载图片失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleCrop = async () => {
    if (!crop || !imageSrc) {
      alert('请先选择裁剪区域')
      return
    }

    setProcessing(true)
    try {
      await api.post(`/images/${id}/crop`, {
        x: Math.round(crop.x),
        y: Math.round(crop.y),
        width: Math.round(crop.width),
        height: Math.round(crop.height)
      })
      alert('裁剪成功！已创建新图片')
      navigate('/dashboard')
    } catch (error) {
      alert('裁剪失败: ' + (error.response?.data?.error || error.message))
    } finally {
      setProcessing(false)
    }
  }

  const handleBrightness = async () => {
    setProcessing(true)
    try {
      await api.post(`/images/${id}/adjust-brightness`, { brightness })
      alert('亮度调整成功！已创建新图片')
      navigate('/dashboard')
    } catch (error) {
      alert('调整失败: ' + (error.response?.data?.error || error.message))
    } finally {
      setProcessing(false)
    }
  }

  const handleContrast = async () => {
    setProcessing(true)
    try {
      await api.post(`/images/${id}/adjust-contrast`, { contrast })
      alert('对比度调整成功！已创建新图片')
      navigate('/dashboard')
    } catch (error) {
      alert('调整失败: ' + (error.response?.data?.error || error.message))
    } finally {
      setProcessing(false)
    }
  }

  const handleAddTag = async () => {
    if (!tagName.trim()) {
      alert('请输入标签名称')
      return
    }

    try {
      await api.post(`/images/${id}/tags`, { tagName: tagName.trim() })
      setTagName('')
      loadImage()
    } catch (error) {
      alert('添加标签失败: ' + (error.response?.data?.error || error.message))
    }
  }

  const handleRemoveTag = async (tagName) => {
    try {
      await api.delete(`/images/${id}/tags/${encodeURIComponent(tagName)}`)
      loadImage()
    } catch (error) {
      alert('删除标签失败: ' + (error.response?.data?.error || error.message))
    }
  }

  if (loading) {
    return <div style={styles.loading}>加载中...</div>
  }

  if (!image) {
    return <div>图片不存在</div>
  }

  return (
    <div style={styles.container}>
      <h2 style={styles.title}>编辑图片: {image.originalFilename}</h2>
      
      <div style={styles.content}>
        <div style={styles.leftPanel}>
          <div style={styles.imageContainer}>
            {imageSrc && (
              <ReactCrop
                crop={crop}
                onChange={(newCrop) => setCrop(newCrop)}
                aspect={undefined}
              >
                <img src={imageSrc} alt="编辑" style={styles.editImage} />
              </ReactCrop>
            )}
          </div>
        </div>

        <div style={styles.rightPanel}>
          <div style={styles.section}>
            <h3 style={styles.sectionTitle}>裁剪</h3>
            <p style={styles.hint}>在图片上拖拽选择裁剪区域</p>
            <button
              onClick={handleCrop}
              disabled={processing || !crop}
              style={styles.button}
            >
              {processing ? '处理中...' : '应用裁剪'}
            </button>
          </div>

          <div style={styles.section}>
            <h3 style={styles.sectionTitle}>亮度调整</h3>
            <div style={styles.sliderGroup}>
              <input
                type="range"
                min="0.5"
                max="2.0"
                step="0.1"
                value={brightness}
                onChange={(e) => setBrightness(parseFloat(e.target.value))}
                style={styles.slider}
              />
              <span style={styles.sliderValue}>{brightness.toFixed(1)}</span>
            </div>
            <button
              onClick={handleBrightness}
              disabled={processing}
              style={styles.button}
            >
              {processing ? '处理中...' : '应用亮度调整'}
            </button>
          </div>

          <div style={styles.section}>
            <h3 style={styles.sectionTitle}>对比度调整</h3>
            <div style={styles.sliderGroup}>
              <input
                type="range"
                min="50"
                max="150"
                step="5"
                value={contrast}
                onChange={(e) => setContrast(parseInt(e.target.value))}
                style={styles.slider}
              />
              <span style={styles.sliderValue}>{contrast}%</span>
            </div>
            <button
              onClick={handleContrast}
              disabled={processing}
              style={styles.button}
            >
              {processing ? '处理中...' : '应用对比度调整'}
            </button>
          </div>

          <div style={styles.section}>
            <h3 style={styles.sectionTitle}>标签管理</h3>
            <div style={styles.tagInput}>
              <input
                type="text"
                value={tagName}
                onChange={(e) => setTagName(e.target.value)}
                placeholder="输入标签名称"
                style={styles.input}
                onKeyPress={(e) => e.key === 'Enter' && handleAddTag()}
              />
              <button onClick={handleAddTag} style={styles.tagButton}>
                添加
              </button>
            </div>
            {image.tags && image.tags.length > 0 && (
              <div style={styles.tags}>
                {image.tags.map(tag => (
                  <span key={tag.id} style={styles.tag}>
                    {tag.name}
                    <button
                      onClick={() => handleRemoveTag(tag.name)}
                      style={styles.tagRemove}
                    >
                      ×
                    </button>
                  </span>
                ))}
              </div>
            )}
          </div>

          <div style={styles.section}>
            <h3 style={styles.sectionTitle}>图片信息</h3>
            <div style={styles.info}>
              <p><strong>文件名:</strong> {image.originalFilename}</p>
              <p><strong>尺寸:</strong> {image.width} × {image.height}</p>
              <p><strong>大小:</strong> {(image.fileSize / 1024 / 1024).toFixed(2)} MB</p>
              {image.exifDate && (
                <p><strong>拍摄时间:</strong> {new Date(image.exifDate).toLocaleString()}</p>
              )}
              {image.exifLocation && (
                <p><strong>位置:</strong> {image.exifLocation}</p>
              )}
              {image.exifCamera && (
                <p><strong>相机:</strong> {image.exifCamera}</p>
              )}
              {image.description && (
                <p><strong>描述:</strong> {image.description}</p>
              )}
            </div>
          </div>

          <button
            onClick={() => navigate('/dashboard')}
            style={styles.backButton}
          >
            返回
          </button>
        </div>
      </div>
    </div>
  )
}

const styles = {
  container: {
    maxWidth: '1400px',
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
  content: {
    display: 'flex',
    gap: '20px'
  },
  leftPanel: {
    flex: 1
  },
  rightPanel: {
    width: '350px',
    display: 'flex',
    flexDirection: 'column',
    gap: '20px'
  },
  imageContainer: {
    border: '1px solid #ddd',
    borderRadius: '4px',
    overflow: 'hidden',
    backgroundColor: '#f5f5f5'
  },
  editImage: {
    maxWidth: '100%',
    display: 'block'
  },
  section: {
    border: '1px solid #ddd',
    borderRadius: '4px',
    padding: '15px',
    backgroundColor: '#f9f9f9'
  },
  sectionTitle: {
    fontSize: '18px',
    marginBottom: '10px',
    color: '#333'
  },
  hint: {
    fontSize: '12px',
    color: '#666',
    marginBottom: '10px'
  },
  sliderGroup: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
    marginBottom: '10px'
  },
  slider: {
    flex: 1
  },
  sliderValue: {
    minWidth: '50px',
    textAlign: 'right',
    fontSize: '14px',
    color: '#666'
  },
  button: {
    width: '100%',
    padding: '10px',
    backgroundColor: '#007bff',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: '14px',
    marginTop: '10px'
  },
  tagInput: {
    display: 'flex',
    gap: '10px',
    marginBottom: '10px'
  },
  input: {
    flex: 1,
    padding: '8px',
    border: '1px solid #ddd',
    borderRadius: '4px',
    fontSize: '14px'
  },
  tagButton: {
    padding: '8px 16px',
    backgroundColor: '#28a745',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: '14px'
  },
  tags: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: '8px'
  },
  tag: {
    backgroundColor: '#e9ecef',
    padding: '5px 10px',
    borderRadius: '12px',
    fontSize: '12px',
    color: '#495057',
    display: 'flex',
    alignItems: 'center',
    gap: '5px'
  },
  tagRemove: {
    backgroundColor: 'transparent',
    border: 'none',
    color: '#666',
    cursor: 'pointer',
    fontSize: '16px',
    padding: 0,
    width: '16px',
    height: '16px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center'
  },
  info: {
    fontSize: '14px',
    color: '#666'
  },
  infoP: {
    marginBottom: '8px'
  }
}

export default ImageEdit

