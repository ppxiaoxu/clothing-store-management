-- =============================================
-- 童装门店库存管理系统 - 建表语句
-- 数据库: clothmgmtsys
-- =============================================

-- 1. 分类表
CREATE TABLE IF NOT EXISTS category (
    id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    name        VARCHAR(50)  NOT NULL                 COMMENT '分类名称',
    parent_id   BIGINT       DEFAULT 0                COMMENT '父分类ID，0表示顶级分类',
    sort_order  INT          DEFAULT 0                COMMENT '排序',
    deleted     TINYINT      DEFAULT 0                COMMENT '逻辑删除: 0-未删除, 1-已删除',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- 2. 供应商表
CREATE TABLE IF NOT EXISTS supplier (
    id             BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    name           VARCHAR(100) NOT NULL                 COMMENT '供应商名称',
    contact_person VARCHAR(50)  DEFAULT ''               COMMENT '联系人',
    phone          VARCHAR(20)  DEFAULT ''               COMMENT '联系电话',
    address        VARCHAR(200) DEFAULT ''               COMMENT '地址',
    remark         VARCHAR(255) DEFAULT ''               COMMENT '备注',
    deleted        TINYINT      DEFAULT 0                COMMENT '逻辑删除: 0-未删除, 1-已删除',
    create_time    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

-- 3. 商品表
CREATE TABLE IF NOT EXISTS product (
    id           BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    name         VARCHAR(100)  NOT NULL                 COMMENT '商品名称',
    product_code VARCHAR(50)   NOT NULL                 COMMENT '货号',
    category_id  BIGINT        NOT NULL                 COMMENT '分类ID',
    supplier_id  BIGINT        NOT NULL                 COMMENT '供应商ID',
    gender       TINYINT       DEFAULT 0                COMMENT '性别: 0-通用, 1-男童, 2-女童',
    suitable_age VARCHAR(50)   DEFAULT ''               COMMENT '适合年龄',
    season       VARCHAR(20)   DEFAULT ''               COMMENT '季节',
    image_url    VARCHAR(255)  DEFAULT ''               COMMENT '图片地址',
    remark       VARCHAR(255)  DEFAULT ''               COMMENT '备注',
    deleted      TINYINT       DEFAULT 0                COMMENT '逻辑删除: 0-未删除, 1-已删除',
    create_time  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_code (product_code),
    INDEX idx_category_id (category_id),
    INDEX idx_supplier_id (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 4. SKU表（商品规格）
CREATE TABLE IF NOT EXISTS sku (
    id             BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    product_id     BIGINT        NOT NULL                 COMMENT '商品ID',
    color          VARCHAR(30)   DEFAULT ''               COMMENT '颜色',
    size           VARCHAR(30)   DEFAULT ''               COMMENT '尺码',
    stock          INT           DEFAULT 0                COMMENT '库存数量',
    cost_price     DECIMAL(10,2) DEFAULT 0.00             COMMENT '进价',
    sale_price     DECIMAL(10,2) DEFAULT 0.00             COMMENT '售价',
    deleted        TINYINT       DEFAULT 0                COMMENT '逻辑删除: 0-未删除, 1-已删除',
    create_time    DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SKU表(商品规格)';
