-- SEATA AT 模式需要 undo_log 表
CREATE TABLE IF NOT EXISTS undo_log
(
    branch_id     BIGINT       NOT NULL COMMENT 'branch transaction id',
    xid           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    context       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    rollback_info LONGBLOB     NOT NULL COMMENT 'rollback info',
    log_status    INT(11)      NOT NULL COMMENT '0:normal status,1:defense status',
    log_created   DATETIME(6)  NOT NULL COMMENT 'create datetime',
    log_modified  DATETIME(6)  NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT ='AT transaction mode undo table';
ALTER TABLE undo_log ADD INDEX ix_log_created (log_created);

CREATE TABLE seata_storage (
    id bigint NOT NULL COMMENT 'id',
    commodity_code varchar(255) COMMENT '商品编码',
    total int COMMENT '商品库存数',
    PRIMARY KEY (id),
    UNIQUE KEY (commodity_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='库存表';

INSERT INTO seata_storage(id, commodity_code, total) VALUES (1, '1001', 99);
