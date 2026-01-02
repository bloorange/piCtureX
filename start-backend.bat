@echo off
chcp 65001 >nul
cd /d "%~dp0backend"
echo ========================================
echo 正在启动后端服务...
echo ========================================
echo.
echo 如果看到错误信息，请仔细查看下面的输出
echo 常见错误：
echo   1. 数据库连接失败 - 检查 application.yml 中的数据库配置
echo   2. 端口被占用 - 检查是否有其他程序占用8080端口
echo   3. JAR文件不存在 - 需要先运行 mvn clean package
echo.
echo ========================================
echo.
java -jar target\picturex-backend-1.0.0.jar
echo.
echo ========================================
echo 后端服务已停止
echo ========================================
pause

