# 数据库初始化说明

## 方法一：使用PowerShell脚本（推荐）

在 `database` 目录下执行：

```powershell
.\init.ps1
```

脚本会提示输入MySQL密码并自动执行SQL文件。

## 方法二：使用PowerShell管道

```powershell
Get-Content init.sql | mysql -u root -p
```

## 方法三：使用CMD或Git Bash

```bash
mysql -u root -p < init.sql
```

## 方法四：手动执行

1. 登录MySQL：
```bash
mysql -u root -p
```

2. 在MySQL命令行中执行：
```sql
source init.sql;
```

或者复制 `init.sql` 的内容到MySQL客户端执行。

## 注意事项

- 确保MySQL服务已启动
- 确保有创建数据库的权限
- 如果数据库已存在，脚本会先尝试删除（`DROP DATABASE IF EXISTS`）

