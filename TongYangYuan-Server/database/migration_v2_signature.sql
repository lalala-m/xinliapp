-- 咨询留痕与数字签名功能 - 数据库变更脚本
-- 执行时间：2026-04-16

-- 1. 扩展 consultation_records 表
ALTER TABLE consultation_records
    ADD COLUMN consultant_summary TEXT COMMENT '咨询师咨询总结',
    ADD COLUMN parent_acknowledged BOOLEAN DEFAULT FALSE COMMENT '家长是否已确认',
    ADD COLUMN signature_record_id BIGINT COMMENT '关联的签名记录ID',
    ADD COLUMN verification_media_url VARCHAR(500) COMMENT '验证视频/图片URL',
    ADD COLUMN record_completeness VARCHAR(20) DEFAULT 'PARTIAL' COMMENT '记录完整度：PARTIAL/COMPLETE';

-- 2. 创建 consultation_signatures 签名记录表
CREATE TABLE IF NOT EXISTS consultation_signatures (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    consultation_record_id BIGINT NOT NULL COMMENT '关联咨询记录ID',
    signer_type ENUM('CONSULTANT', 'PARENT') NOT NULL COMMENT '签名者类型',
    signer_user_id BIGINT NOT NULL COMMENT '签名者用户ID',
    signature_data TEXT COMMENT 'Base64签名图片数据',
    signature_hash VARCHAR(64) COMMENT 'SHA-256签名内容哈希',
    verification_media_url VARCHAR(500) COMMENT '签字时的拍照/视频URL',
    verification_type ENUM('PHOTO', 'VIDEO') DEFAULT 'PHOTO' COMMENT '验证类型',
    signed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '签名时间',
    ip_address VARCHAR(50) COMMENT '签名者IP地址',
    user_agent VARCHAR(500) COMMENT '签名者浏览器UA',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_record_id (consultation_record_id),
    INDEX idx_signer_type (consultation_record_id, signer_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='咨询签名确认记录表';
