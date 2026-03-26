-- 修改 users.avatar_url 字段类型，从 VARCHAR(500) 改为 TEXT
-- 以支持 base64 Data URL 格式的头像（图片转 base64 后约增大 33%）
ALTER TABLE users MODIFY COLUMN avatar_url TEXT;

-- 同步修改 consultants.avatar_url 保持一致
ALTER TABLE consultants MODIFY COLUMN avatar_url TEXT;
