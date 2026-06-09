CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username    VARCHAR(50)  NOT NULL                COMMENT '用户名',
    password    VARCHAR(100) NOT NULL                COMMENT 'BCrypt加密密码',
    nickname    VARCHAR(50)  DEFAULT NULL            COMMENT '昵称',
    phone       VARCHAR(20)  DEFAULT ''              COMMENT '手机号',
    avatar      VARCHAR(255) DEFAULT ''              COMMENT '头像地址',
    role        VARCHAR(30)  NOT NULL DEFAULT 'STORE_OWNER' COMMENT '角色',
    status      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1启用，0禁用',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

INSERT IGNORE INTO sys_user (username, password, nickname, phone, avatar, role, status)
VALUES (
    'admin',
    '$2a$10$1b9WCDjuaA5Lz7k9toa.1O5jJoPiLBhfYr.WofObiAtc2zJqOu90.',
    '管理员',
    '',
    '',
    'ADMIN',
    1
);
