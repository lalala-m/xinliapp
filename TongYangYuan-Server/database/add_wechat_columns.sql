-- ============================================================
-- 为 users 表添加微信登录字段（与 JPA User 实体一致）
-- 可重复执行：已存在的列/索引会被跳过（需 MySQL 8.0.12+ 的 IF NOT EXISTS）
--
-- Navicat：选中库 mental_health_db → 新建查询 → 粘贴全文 → 运行
-- ============================================================

USE mental_health_db;

-- 逐列添加，避免「部分列已存在」导致整句失败
ALTER TABLE users ADD COLUMN IF NOT EXISTS wx_open_id VARCHAR(100) NULL COMMENT '微信OpenID' AFTER avatar_url;
ALTER TABLE users ADD COLUMN IF NOT EXISTS wx_union_id VARCHAR(100) NULL COMMENT '微信UnionID' AFTER wx_open_id;
ALTER TABLE users ADD COLUMN IF NOT EXISTS wx_nickname VARCHAR(100) NULL COMMENT '微信昵称' AFTER wx_union_id;
ALTER TABLE users ADD COLUMN IF NOT EXISTS wx_avatar_url VARCHAR(500) NULL COMMENT '微信头像URL' AFTER wx_nickname;

-- 索引单独加；若报 Duplicate key name 'idx_xxx'，说明索引已有，可忽略
ALTER TABLE users ADD INDEX idx_wx_open_id (wx_open_id);
ALTER TABLE users ADD INDEX idx_wx_union_id (wx_union_id);

-- ------------------------------------------------------------
-- 若你的 MySQL 低于 8.0.12，不支持 ADD COLUMN IF NOT EXISTS：
-- 在 Navicat 里用「设计表」看 users 缺哪几列，只执行缺的，例如：
-- ALTER TABLE users ADD COLUMN wx_open_id VARCHAR(100) NULL ...;
-- ------------------------------------------------------------
