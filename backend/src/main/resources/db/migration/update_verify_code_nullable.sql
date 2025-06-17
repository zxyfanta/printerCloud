-- 更新取件码字段为可空，并清空未支付订单的取件码
-- 执行时间：2025-06-17

-- 1. 修改字段为可空
ALTER TABLE print_orders MODIFY COLUMN verify_code VARCHAR(255) NULL;

-- 2. 清空未支付订单的取件码（status = 0）
UPDATE print_orders SET verify_code = NULL WHERE status = 0;

-- 3. 确保已支付订单有取件码
UPDATE print_orders 
SET verify_code = LPAD(FLOOR(RAND() * 1000000), 6, '0')
WHERE status > 0 AND (verify_code IS NULL OR verify_code = '');

-- 4. 添加注释
ALTER TABLE print_orders MODIFY COLUMN verify_code VARCHAR(255) NULL COMMENT '取件验证码，支付成功后生成';
