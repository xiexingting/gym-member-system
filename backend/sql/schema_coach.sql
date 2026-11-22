-- B1 教练管理：教练表
CREATE TABLE IF NOT EXISTS coach (
    coach_id   INT PRIMARY KEY AUTO_INCREMENT COMMENT '教练ID',
    coach_name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone      VARCHAR(20) COMMENT '联系电话',
    gender     VARCHAR(10) COMMENT '性别',
    specialty  VARCHAR(100) COMMENT '专长（瑜伽/力量/有氧等）',
    hire_date  DATE COMMENT '入职日期',
    status     TINYINT DEFAULT 1 COMMENT '状态：1=在职 0=离职',
    remark     VARCHAR(255) COMMENT '备注'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教练表';
