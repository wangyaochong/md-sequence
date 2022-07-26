drop table if exists seq_core;
CREATE TABLE seq_core
(
    `id`      bigint NOT NULL AUTO_INCREMENT,
    `max_seq` bigint NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 0
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = COMPACT COMMENT ='序列核心表，只记录数据';

drop table if exists seq_info;
CREATE TABLE seq_info
(
    `id`       bigint      NOT NULL AUTO_INCREMENT,
    `seq_core_id`  bigint,
    `seq_name` varchar(64) NOT NULL,
    `seq_type` varchar(16) NOT NULL,
    PRIMARY KEY (`id`),
    unique key `seq_name` (`seq_name`),
    index idx_name (seq_name)
) ENGINE = InnoDB
  AUTO_INCREMENT = 0
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = COMPACT COMMENT ='序列信息表，记录序列名称、类型等信息，序列信息创建以后，不允许修改';

drop table if exists node_seq;
CREATE TABLE node_seq
(
    `id`      bigint NOT NULL AUTO_INCREMENT,
    `seq_core_id`  bigint NOT NULL,
    `node_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    unique key idx_seq (seq_core_id),
    index idx_node (node_id)
) ENGINE = InnoDB
  AUTO_INCREMENT = 0
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = COMPACT COMMENT ='节点序列关系表，全局严格递增下一个序列只有一个节点服务';
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
