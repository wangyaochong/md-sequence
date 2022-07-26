drop table if exists seq_core;
CREATE TABLE seq_core
(
    `id`      bigint NOT NULL AUTO_INCREMENT comment 'id',
    `max_seq` bigint NOT NULL comment '上一次取值的最大值，这次取需要+1开始',
    `node_id` bigint comment '服务节点id，可能是null，如果是全局递增，则必须不为null',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 0
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = COMPACT COMMENT ='序列核心表，只记录数据';

drop table if exists seq_info;
CREATE TABLE seq_info
(
    `id`          bigint      NOT NULL AUTO_INCREMENT,
    `seq_core_id` bigint,
    `seq_name`    varchar(64) NOT NULL,
    `seq_type`    varchar(16) NOT NULL,
    PRIMARY KEY (`id`),
    unique key `seq_name` (`seq_name`),
    index idx_name (seq_name)
) ENGINE = InnoDB
  AUTO_INCREMENT = 0
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = COMPACT COMMENT ='序列信息表，记录序列名称、类型等信息，序列信息创建以后，不允许修改';

drop table if exists node;
CREATE TABLE node
(
    `id`          bigint(20)  NOT NULL AUTO_INCREMENT,
    `ip`          varchar(32) NOT NULL,
    `port`        int         NOT NULL,
    `create_time` datetime    NOT NULL DEFAULT now() COMMENT '创建时间',
    `version`     bigint      NOT NULL DEFAULT 0 COMMENT '更新版本号',
    `update_time` datetime    NOT NULL DEFAULT now() on update now() COMMENT '更新时间，只有version会更新',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ip_port` (`ip`, `port`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 0
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = COMPACT COMMENT ='服务节点表';
