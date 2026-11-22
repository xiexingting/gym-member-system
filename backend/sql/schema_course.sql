-- B3 课程管理：课程表
CREATE TABLE IF NOT EXISTS course (
    course_id    INT PRIMARY KEY AUTO_INCREMENT COMMENT '课程ID',
    course_name  VARCHAR(100) NOT NULL COMMENT '课程名称',
    coach_id     INT NOT NULL COMMENT '授课教练ID，关联 coach.coach_id',
    course_time  DATETIME NOT NULL COMMENT '上课时间',
    duration     INT DEFAULT 60 COMMENT '时长（分钟）',
    max_capacity INT NOT NULL COMMENT '最大容量（人数）',
    booked_count INT DEFAULT 0 COMMENT '已预约人数',
    status       TINYINT DEFAULT 1 COMMENT '状态：1上架 0下架/取消',
    remark       VARCHAR(255) COMMENT '备注',
    INDEX idx_coach (coach_id),
    INDEX idx_time (course_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';
