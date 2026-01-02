import React, { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../utils/api'

function ImageGallery() {
  const [images, setImages] = useState([])
  const [loading, setLoading] = useState(true)
  const [searchKeyword, setSearchKeyword] = useState('')
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [selectedImages, setSelectedImages] = useState([])
  const [showCarousel, setShowCarousel] = useState(false)
  const [carouselIndex, setCarouselIndex] = useState(0)

  useEffect(() => {
    loadImages()
  }, [])

  const loadImages = async () => {
    setLoading(true)
    try {
      const response = await api.get('/images', {
        timeout: 10000 // 10秒超时
      })
      console.log('加载图片成功:', response.data)
      setImages(Array.isArray(response.data) ? response.data : [])
    } catch (error) {
      console.error('加载图片失败:', error)
      let errorMessage = '加载图片失败'
      if (error.code === 'ECONNABORTED') {
        errorMessage = '请求超时，请检查网络连接'
      } else if (error.response) {
        if (error.response.status === 401) {
          errorMessage = '未授权，请重新登录'
          // 可以在这里触发登出逻辑
        } else {
          errorMessage = `加载失败: ${error.response.status}`
        }
      } else if (error.request) {
        errorMessage = '无法连接到服务器，请检查后端服务是否运行'
      }
      alert(errorMessage)
      setImages([]) // 确保设置为空数组，避免显示"加载中"
    } finally {
      setLoading(false)
    }
  }

  const handleSearch = async () => {
    try {
      const params = {}
      if (searchKeyword) params.keyword = searchKeyword
      if (startDate) params.startDate = new Date(startDate).toISOString()
      if (endDate) params.endDate = new Date(endDate).toISOString()

      const response = await api.get('/images/search', { params })
      setImages(response.data)
    } catch (error) {
      console.error('搜索失败:', error)
    }
  }

  const handleDelete = async (id) => {
    if (!window.confirm('确定要删除这张图片吗？')) return

    try {
      await api.delete(`/images/${id}`)
      loadImages()
    } catch (error) {
      alert('删除失败: ' + (error.response?.data?.error || error.message))
    }
  }

  const toggleImageSelection = (id) => {
    setSelectedImages(prev =>
      prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id]
    )
  }

  const handleCarousel = () => {
    if (selectedImages.length === 0) {
      alert('请先选择要轮播的图片')
      return
    }
    setCarouselIndex(0)
    setShowCarousel(true)
  }

  const getImageUrl = (filename) => {
    return `http://localhost:8080/api/images/file/${filename}`
  }

  const getThumbnailUrl = (filename) => {
    return `http://localhost:8080/api/images/thumbnail/${filename}`
  }

  if (loading) {
    return <div style={styles.loading}>加载中...</div>
  }

  return (
    <div>
      <div style={styles.searchBar}>
        <input
          type="text"
          placeholder="搜索关键词..."
          value={searchKeyword}
          onChange={(e) => setSearchKeyword(e.target.value)}
          style={styles.searchInput}
        />
        <input
          type="date"
          value={startDate}
          onChange={(e) => setStartDate(e.target.value)}
          style={styles.dateInput}
        />
        <input
          type="date"
          value={endDate}
          onChange={(e) => setEndDate(e.target.value)}
          style={styles.dateInput}
        />
        <button onClick={handleSearch} style={styles.searchBtn}>搜索</button>
        <button onClick={loadImages} style={styles.resetBtn}>重置</button>
        {selectedImages.length > 0 && (
          <button onClick={handleCarousel} style={styles.carouselBtn}>
            轮播显示 ({selectedImages.length})
          </button>
        )}
      </div>

      <div style={styles.gallery}>
        {images.map((image) => (
          <div key={image.id} style={styles.imageCard}>
            <input
              type="checkbox"
              checked={selectedImages.includes(image.id)}
              onChange={() => toggleImageSelection(image.id)}
              style={styles.checkbox}
            />
            <Link to={`/dashboard/edit/${image.id}`}>
              <img
                src={getThumbnailUrl(image.filename)}
                alt={image.originalFilename}
                style={styles.thumbnail}
                onError={(e) => {
                  e.target.src = getImageUrl(image.filename)
                }}
              />
            </Link>
            <div style={styles.imageInfo}>
              <p style={styles.filename}>{image.originalFilename}</p>
              {image.width && image.height && (
                <p style={styles.dimensions}>{image.width} × {image.height}</p>
              )}
              {image.exifDate && (
                <p style={styles.exif}>拍摄时间: {new Date(image.exifDate).toLocaleString()}</p>
              )}
              {image.exifLocation && (
                <p style={styles.exif}>位置: {image.exifLocation}</p>
              )}
              {image.tags && image.tags.length > 0 && (
                <div style={styles.tags}>
                  {image.tags.map(tag => (
                    <span key={tag.id} style={styles.tag}>{tag.name}</span>
                  ))}
                </div>
              )}
            </div>
            <button
              onClick={() => handleDelete(image.id)}
              style={styles.deleteBtn}
            >
              删除
            </button>
          </div>
        ))}
      </div>

      {images.length === 0 && (
        <div style={styles.empty}>暂无图片，<Link to="/dashboard/upload">立即上传</Link></div>
      )}

      {showCarousel && (
        <div style={styles.carouselOverlay} onClick={() => setShowCarousel(false)}>
          <div style={styles.carouselContent} onClick={(e) => e.stopPropagation()}>
            <button
              style={styles.carouselClose}
              onClick={() => setShowCarousel(false)}
            >
              ×
            </button>
            <button
              style={styles.carouselPrev}
              onClick={() => setCarouselIndex(prev => 
                prev > 0 ? prev - 1 : selectedImages.length - 1
              )}
            >
              ‹
            </button>
            <img
              src={getImageUrl(images.find(img => img.id === selectedImages[carouselIndex])?.filename)}
              alt="轮播"
              style={styles.carouselImage}
            />
            <button
              style={styles.carouselNext}
              onClick={() => setCarouselIndex(prev => 
                prev < selectedImages.length - 1 ? prev + 1 : 0
              )}
            >
              ›
            </button>
            <div style={styles.carouselIndicator}>
              {carouselIndex + 1} / {selectedImages.length}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

const styles = {
  searchBar: {
    display: 'flex',
    gap: '10px',
    marginBottom: '20px',
    flexWrap: 'wrap'
  },
  searchInput: {
    flex: 1,
    minWidth: '200px',
    padding: '10px',
    border: '1px solid #ddd',
    borderRadius: '4px'
  },
  dateInput: {
    padding: '10px',
    border: '1px solid #ddd',
    borderRadius: '4px'
  },
  searchBtn: {
    padding: '10px 20px',
    backgroundColor: '#007bff',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer'
  },
  resetBtn: {
    padding: '10px 20px',
    backgroundColor: '#6c757d',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer'
  },
  carouselBtn: {
    padding: '10px 20px',
    backgroundColor: '#28a745',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer'
  },
  gallery: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))',
    gap: '20px'
  },
  imageCard: {
    backgroundColor: 'white',
    borderRadius: '8px',
    padding: '10px',
    boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
    position: 'relative'
  },
  checkbox: {
    position: 'absolute',
    top: '15px',
    left: '15px',
    zIndex: 10,
    width: '20px',
    height: '20px'
  },
  thumbnail: {
    width: '100%',
    height: '200px',
    objectFit: 'cover',
    borderRadius: '4px',
    cursor: 'pointer'
  },
  imageInfo: {
    marginTop: '10px'
  },
  filename: {
    fontSize: '14px',
    fontWeight: 'bold',
    marginBottom: '5px',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap'
  },
  dimensions: {
    fontSize: '12px',
    color: '#666',
    marginBottom: '5px'
  },
  exif: {
    fontSize: '12px',
    color: '#666',
    marginBottom: '5px'
  },
  tags: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: '5px',
    marginTop: '5px'
  },
  tag: {
    backgroundColor: '#e9ecef',
    padding: '2px 8px',
    borderRadius: '12px',
    fontSize: '12px',
    color: '#495057'
  },
  deleteBtn: {
    marginTop: '10px',
    padding: '8px 16px',
    backgroundColor: '#dc3545',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    width: '100%'
  },
  empty: {
    textAlign: 'center',
    padding: '40px',
    color: '#666'
  },
  loading: {
    textAlign: 'center',
    padding: '40px',
    fontSize: '18px'
  },
  carouselOverlay: {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: 'rgba(0,0,0,0.9)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 1000
  },
  carouselContent: {
    position: 'relative',
    maxWidth: '90%',
    maxHeight: '90%'
  },
  carouselImage: {
    maxWidth: '100%',
    maxHeight: '90vh',
    objectFit: 'contain'
  },
  carouselClose: {
    position: 'absolute',
    top: '-40px',
    right: '0',
    backgroundColor: 'transparent',
    border: 'none',
    color: 'white',
    fontSize: '40px',
    cursor: 'pointer',
    width: '40px',
    height: '40px'
  },
  carouselPrev: {
    position: 'absolute',
    left: '-60px',
    top: '50%',
    transform: 'translateY(-50%)',
    backgroundColor: 'rgba(255,255,255,0.3)',
    border: 'none',
    color: 'white',
    fontSize: '40px',
    cursor: 'pointer',
    width: '50px',
    height: '50px',
    borderRadius: '50%'
  },
  carouselNext: {
    position: 'absolute',
    right: '-60px',
    top: '50%',
    transform: 'translateY(-50%)',
    backgroundColor: 'rgba(255,255,255,0.3)',
    border: 'none',
    color: 'white',
    fontSize: '40px',
    cursor: 'pointer',
    width: '50px',
    height: '50px',
    borderRadius: '50%'
  },
  carouselIndicator: {
    position: 'absolute',
    bottom: '-40px',
    left: '50%',
    transform: 'translateX(-50%)',
    color: 'white',
    fontSize: '18px'
  }
}

export default ImageGallery

