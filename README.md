# PictureX - 图片管理网站

基于 React + Spring Boot 的图片管理系统，支持图片上传、EXIF信息提取、分类标签、图片编辑等功能。

## 功能特性

1. ✅ 用户注册、登录功能（用户名、密码至少6个字符，邮箱格式验证，唯一性检查）
2. ✅ 图片上传功能
3. ✅ 自动提取EXIF信息（时间、地点、相机信息、分辨率等）
4. ✅ 自定义分类标签
5. ✅ 自动生成缩略图
6. ✅ 图片信息数据库存储
7. ✅ 多条件查询功能
8. ✅ 图片轮播展示
9. ✅ 图片编辑功能（裁剪、亮度调整、对比度调整）
10. ✅ 图片删除功能

## 技术栈

### 后端
- Spring Boot 3.1.5
- Spring Security + JWT
- Spring Data JPA
- MySQL
- metadata-extractor (EXIF信息提取)
- Thumbnailator (缩略图生成)

### 前端
- React 18
- React Router
- Axios
- react-image-crop (图片裁剪)
- Vite

## 项目结构

```
picturex/
├── backend/                 # Spring Boot 后端
│   ├── src/
│   │   └── main/
│   │       ├── java/com/picturex/
│   │       │   ├── config/      # 配置类
│   │       │   ├── controller/   # 控制器
│   │       │   ├── entity/       # 实体类
│   │       │   ├── repository/  # 数据访问层
│   │       │   ├── security/    # 安全配置
│   │       │   ├── service/     # 业务逻辑层
│   │       │   └── util/        # 工具类
│   │       └── resources/
│   │           └── application.yml
│   └── pom.xml
├── frontend/                # React 前端
│   ├── src/
│   │   ├── pages/           # 页面组件
│   │   ├── utils/           # 工具函数
│   │   ├── App.jsx
│   │   └── main.jsx
│   ├── package.json
│   └── vite.config.js
└── database/                # 数据库脚本
    └── init.sql
```

## 环境要求

- JDK 17+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

## 安装和运行

### 1. 数据库配置

首先创建MySQL数据库：

**Windows PowerShell:**
```powershell
Get-Content database/init.sql | mysql -u root -p
```

**Windows CMD 或 Linux/Mac:**
```bash
mysql -u root -p < database/init.sql
```

**或者手动执行：**
1. 登录MySQL：`mysql -u root -p`
2. 执行：`source database/init.sql`
3. 或者复制SQL内容到MySQL客户端执行

### 2. 后端配置

1. 修改 `backend/src/main/resources/application.yml` 中的数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/picturex?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root  # 修改为你的MySQL用户名
    password: root  # 修改为你的MySQL密码
```

2. 编译并运行后端：

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动。

### 3. 前端配置

1. 安装依赖：

```bash
cd frontend
npm install
```

2. 启动开发服务器：

```bash
npm run dev
```

前端应用将在 `http://localhost:3000` 启动。

## 使用说明

### 用户注册

1. 访问 `http://localhost:3000/register`
2. 填写用户名（至少6个字符）、邮箱、密码（至少6个字符）
3. 系统会验证用户名和邮箱的唯一性

### 用户登录

1. 访问 `http://localhost:3000/login`
2. 输入用户名和密码登录

### 图片上传

1. 登录后，点击"上传图片"
2. 选择图片文件
3. 可选填写图片描述
4. 系统会自动提取EXIF信息并生成缩略图

### 图片查询

1. 在图片库页面，可以使用以下条件搜索：
   - 关键词（文件名、描述）
   - 开始日期
   - 结束日期

### 图片编辑

1. 点击图片进入编辑页面
2. 可以执行以下操作：
   - **裁剪**：在图片上拖拽选择区域，点击"应用裁剪"
   - **亮度调整**：调整滑块，点击"应用亮度调整"
   - **对比度调整**：调整滑块，点击"应用对比度调整"
   - **标签管理**：添加或删除标签

### 图片轮播

1. 在图片库页面，勾选要轮播的图片
2. 点击"轮播显示"按钮
3. 使用左右箭头切换图片

### 删除图片

在图片库页面，点击图片下方的"删除"按钮。

## API接口

### 认证接口

- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录

### 图片接口

- `POST /api/images/upload` - 上传图片
- `GET /api/images` - 获取用户所有图片
- `GET /api/images/{id}` - 获取图片详情
- `GET /api/images/search` - 搜索图片
- `GET /api/images/file/{filename}` - 获取图片文件
- `GET /api/images/thumbnail/{filename}` - 获取缩略图
- `POST /api/images/{id}/tags` - 添加标签
- `DELETE /api/images/{id}/tags/{tagName}` - 删除标签
- `POST /api/images/{id}/crop` - 裁剪图片
- `POST /api/images/{id}/adjust-brightness` - 调整亮度
- `POST /api/images/{id}/adjust-contrast` - 调整对比度
- `DELETE /api/images/{id}` - 删除图片

## 数据库设计

### users 表
- id: 主键
- username: 用户名（唯一）
- password: 密码（加密）
- email: 邮箱（唯一）
- created_at: 创建时间

### images 表
- id: 主键
- filename: 文件名
- original_filename: 原始文件名
- file_path: 文件路径
- thumbnail_path: 缩略图路径
- width: 宽度
- height: 高度
- file_size: 文件大小
- description: 描述
- exif_date: EXIF日期
- exif_location: EXIF位置
- exif_camera: EXIF相机信息
- upload_date: 上传时间
- user_id: 用户ID（外键）

### tags 表
- id: 主键
- name: 标签名称（唯一）

### image_tags 表
- image_id: 图片ID（外键）
- tag_id: 标签ID（外键）

## 注意事项

1. 确保MySQL服务已启动
2. 确保后端服务在8080端口运行
3. 上传的图片会保存在 `backend/uploads` 目录
4. 缩略图会保存在 `backend/thumbnails` 目录
5. 编辑图片会创建新图片，不会覆盖原图

## 开发说明

### 后端开发

- 使用Spring Boot 3.1.5
- 使用JWT进行身份认证
- 使用Spring Data JPA进行数据访问
- 使用metadata-extractor提取EXIF信息
- 使用Thumbnailator生成缩略图

### 前端开发

- 使用React 18和函数组件
- 使用React Router进行路由管理
- 使用Axios进行HTTP请求
- 使用react-image-crop进行图片裁剪

## 许可证

MIT License

