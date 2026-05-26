-- 咨询记录PDF与聊天记录导出功能 - 数据库变更脚本
-- 执行时间：2026-04-26

USE mental_health_db;

-- 1. 扩展 consultation_records 表 - 添加PDF相关字段
ALTER TABLE consultation_records
    ADD COLUMN pdf_file_path VARCHAR(500) COMMENT '生成的PDF文件路径',
    ADD COLUMN chat_export_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '聊天记录导出状态：PENDING/EXPORTING/EXPORTED/FAILED',
    ADD COLUMN chat_export_completed_at DATETIME COMMENT '聊天记录导出完成时间',
    ADD COLUMN both_parties_signed BOOLEAN DEFAULT FALSE COMMENT '双方是否都已签名';

-- 2. 扩展 consultation_signatures 表 - 添加更多字段用于PDF合并
ALTER TABLE consultation_signatures
    ADD COLUMN signature_position VARCHAR(50) DEFAULT 'BOTTOM_RIGHT' COMMENT '签名在PDF中的位置',
    ADD COLUMN page_number INT DEFAULT 1 COMMENT '签名所在的页码',
    ADD COLUMN pdf_incorporated BOOLEAN DEFAULT FALSE COMMENT '签名是否已合并到PDF';

-- 3. 创建聊天记录导出表
CREATE TABLE IF NOT EXISTS consultation_chat_exports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '导出记录ID',
    consultation_record_id BIGINT NOT NULL COMMENT '关联咨询记录ID',
    appointment_id BIGINT NOT NULL COMMENT '关联预约ID',
    chat_content TEXT COMMENT '导出的聊天记录内容(JSON格式)',
    export_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '导出状态：PENDING/COMPLETED/FAILED',
    export_started_at DATETIME COMMENT '导出开始时间',
    export_completed_at DATETIME COMMENT '导出完成时间',
    export_error_message VARCHAR(500) COMMENT '导出错误信息',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_record_id (consultation_record_id),
    INDEX idx_appointment_id (appointment_id),
    INDEX idx_status (export_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='咨询聊天记录导出表';

-- 4. 创建PDF存储表
CREATE TABLE IF NOT EXISTS consultation_pdf_documents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'PDF文档ID',
    consultation_record_id BIGINT NOT NULL COMMENT '关联咨询记录ID',
    pdf_file_name VARCHAR(255) NOT NULL COMMENT 'PDF文件名',
    pdf_file_path VARCHAR(500) NOT NULL COMMENT 'PDF文件存储路径',
    pdf_file_size BIGINT COMMENT 'PDF文件大小(字节)',
    pdf_page_count INT COMMENT 'PDF页数',
    pdf_generated_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'PDF生成时间',
    pdf_checksum VARCHAR(64) COMMENT 'PDF文件校验和(SHA-256)',
    is_latest BOOLEAN DEFAULT TRUE COMMENT '是否为最新版本',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_record_id (consultation_record_id),
    INDEX idx_is_latest (is_latest)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='咨询PDF文档表';

-- 5. 添加备注列到appointments表
ALTER TABLE appointments
    ADD COLUMN consultation_summary TEXT COMMENT '咨询摘要/总结',
    ADD COLUMN parent_rating DECIMAL(3,2) COMMENT '家长评分(1-5)',
    ADD COLUMN parent_comment TEXT COMMENT '家长评价';

-- 6. 添加索引优化查询
CREATE INDEX idx_consultation_records_appointment ON consultation_records(appointment_id);
CREATE INDEX idx_consultation_records_status ON consultation_records(status);
CREATE INDEX idx_consultation_records_created ON consultation_records(created_at);
