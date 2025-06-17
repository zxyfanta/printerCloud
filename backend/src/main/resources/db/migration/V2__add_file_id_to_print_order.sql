-- 为 print_order 表添加 file_id 字段
-- 用于关联订单和文件

ALTER TABLE print_order ADD COLUMN file_id BIGINT;

-- 添加注释
COMMENT ON COLUMN print_order.file_id IS '关联的文件ID';

-- 可选：添加外键约束（如果有 file 表的话）
-- ALTER TABLE print_order ADD CONSTRAINT fk_print_order_file_id 
-- FOREIGN KEY (file_id) REFERENCES file(id);

-- 为 file_id 字段添加索引以提高查询性能
CREATE INDEX idx_print_order_file_id ON print_order(file_id);
