drop table if exists seq_core;
CREATE TABLE seq_core
(
    `id`       bigint           NOT NULL AUTO_INCREMENT comment 'id',
    `last_max` bigint default 1 NOT NULL comment '上一次取值的最大值，这次取需要+1开始',
    `node_id`  bigint comment '服务节点id，可能是null，如果是全局递增，则必须不为null',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 0
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = COMPACT COMMENT ='序列核心表，只记录数据';

drop table if exists seq_info;
CREATE TABLE seq_info
(
    `id`                bigint      NOT NULL AUTO_INCREMENT,
    `core_id`           bigint,
    `name`              varchar(64) NOT NULL,
    `server_cache_size` int         not null default 10000000 comment '服务端缓存，大于等于2，建议100万',
    `client_cache_size` int         not null default 10000 comment '客户端缓存，大于等于1，建议1万',
    `type`              varchar(64) NOT NULL,
    PRIMARY KEY (`id`),
    unique key `name` (`name`)
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
    UNIQUE KEY `ip_port` (`ip`, `port`),
    index idx_ut (`update_time`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 0
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = COMPACT COMMENT ='服务节点表';

drop table if exists db_lock;
CREATE TABLE db_lock
(
    id       int NOT NULL AUTO_INCREMENT PRIMARY KEY,
    lock_key varchar(255) UNIQUE,
    token    varchar(255),
    expireAt TIMESTAMP
);