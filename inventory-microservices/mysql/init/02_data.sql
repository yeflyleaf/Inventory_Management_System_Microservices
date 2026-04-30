USE demo1;

INSERT INTO user (username, password, nickname, role, status) VALUES 
('admin', 'admin123', '系统管理员', 'ADMIN', 1),
('user', 'user123', '普通用户', 'USER', 1);

INSERT INTO item (sku, name, category, unit, sale_price) VALUES 
('IPHONE15', 'iPhone 15 128G', '手机', '部', 5999.00),
('MACBOOK_AIR', 'MacBook Air M2', '电脑', '台', 8999.00);

INSERT INTO warehouse (name, address) VALUES 
('主仓库', '上海市浦东新区'),
('备用仓库', '北京市朝阳区');

INSERT INTO system_setting (setting_key, setting_value, setting_type, description) VALUES 
('order_prefix_sales', 'SO', 'string', '销售订单编号前缀'),
('order_prefix_purchase', 'PO', 'string', '采购订单编号前缀');
