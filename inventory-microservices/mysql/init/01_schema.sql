-- Business Schema for demo1
CREATE DATABASE IF NOT EXISTS demo1;
USE demo1;

-- 1. User table
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
    password VARCHAR(100) NOT NULL COMMENT '加密密码',
    nickname VARCHAR(50) COMMENT '姓名/昵称',
    role VARCHAR(20) COMMENT '角色',
    status TINYINT DEFAULT 1 COMMENT '状态 1=启用 0=禁用',
    avatar VARCHAR(255) COMMENT '头像URL',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    last_login_at DATETIME COMMENT '最后登录时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 2. Item table
CREATE TABLE IF NOT EXISTS item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    sku VARCHAR(50) NOT NULL UNIQUE COMMENT '商品编码',
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    category VARCHAR(50) COMMENT '商品分类',
    unit VARCHAR(20) COMMENT '计量单位',
    barcode VARCHAR(50) COMMENT '条码',
    sale_price DECIMAL(10, 2) COMMENT '销售价',
    image_url VARCHAR(2000) COMMENT '商品图片URL列表',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

-- 3. Customer table
CREATE TABLE IF NOT EXISTS customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '客户名称',
    contact_person VARCHAR(50) COMMENT '联系人',
    phone VARCHAR(20) COMMENT '联系电话',
    phone2 VARCHAR(20) COMMENT '备用电话',
    address VARCHAR(255) COMMENT '联系地址',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

-- 4. Supplier table
CREATE TABLE IF NOT EXISTS supplier (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '供应商名称',
    contact_person VARCHAR(50) COMMENT '联系人',
    phone VARCHAR(20) COMMENT '联系电话',
    phone2 VARCHAR(20) COMMENT '备用电话',
    address VARCHAR(255) COMMENT '联系地址',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

-- 5. Warehouse table
CREATE TABLE IF NOT EXISTS warehouse (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '仓库名称',
    address VARCHAR(255) COMMENT '仓库地址',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

-- 6. Purchase Order table
CREATE TABLE IF NOT EXISTS purchase_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '采购单编号',
    supplier_id BIGINT COMMENT '供应商ID',
    total_amount DECIMAL(12, 2) COMMENT '采购总金额',
    status VARCHAR(20) COMMENT '状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    created_by BIGINT COMMENT '创建人'
);

-- 7. Purchase Order Item table
CREATE TABLE IF NOT EXISTS purchase_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    purchase_order_id BIGINT COMMENT '采购单ID',
    item_id BIGINT COMMENT '商品ID',
    qty INT COMMENT '采购数量',
    cost_price DECIMAL(10, 2) COMMENT '采购单价',
    subtotal DECIMAL(12, 2) COMMENT '小计金额'
);

-- 8. Sales Order table
CREATE TABLE IF NOT EXISTS sales_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '销售单编号',
    customer_id BIGINT COMMENT '客户ID',
    total_amount DECIMAL(12, 2) COMMENT '销售总金额',
    status VARCHAR(20) COMMENT '状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    created_by BIGINT COMMENT '创建人'
);

-- 9. Sales Order Item table
CREATE TABLE IF NOT EXISTS sales_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    sales_order_id BIGINT COMMENT '销售单ID',
    item_id BIGINT COMMENT '商品ID',
    qty INT COMMENT '销售数量',
    sale_price DECIMAL(10, 2) COMMENT '销售单价',
    subtotal DECIMAL(12, 2) COMMENT '小计金额'
);

-- 10. Stock Flow table
CREATE TABLE IF NOT EXISTS stock_flow (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    item_id BIGINT COMMENT '商品ID',
    change_amount INT COMMENT '库存变动数量',
    change_type VARCHAR(20) COMMENT '变动类型',
    ref_type VARCHAR(20) COMMENT '来源单据类型',
    ref_id VARCHAR(50) COMMENT '来源单据ID',
    warehouse_id BIGINT COMMENT '仓库ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间'
);

-- 11. Operation Log table
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '操作用户名',
    module VARCHAR(50) COMMENT '操作模块',
    action VARCHAR(50) COMMENT '操作类型',
    target VARCHAR(100) COMMENT '操作对象',
    target_id VARCHAR(50) COMMENT '操作对象ID',
    description TEXT COMMENT '操作描述',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '浏览器信息',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_url VARCHAR(255) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_status INT COMMENT '响应状态码',
    execution_time INT COMMENT '执行时间(毫秒)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间'
);

-- 12. System Setting table
CREATE TABLE IF NOT EXISTS system_setting (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    setting_key VARCHAR(50) NOT NULL UNIQUE COMMENT '设置键',
    setting_value TEXT COMMENT '设置值',
    setting_type VARCHAR(20) COMMENT '类型: string/number/boolean/json',
    description VARCHAR(200) COMMENT '描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- Seata undo_log table
CREATE TABLE IF NOT EXISTS `undo_log` (
  `branch_id` bigint(20) NOT NULL,
  `xid` varchar(100) NOT NULL,
  `context` varchar(128) NOT NULL,
  `rollback_info` longblob NOT NULL,
  `log_status` int(11) NOT NULL,
  `log_created` datetime(6) NOT NULL,
  `log_modified` datetime(6) NOT NULL,
  PRIMARY KEY (`branch_id`),
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
