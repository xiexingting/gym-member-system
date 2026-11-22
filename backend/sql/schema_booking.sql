-- B4 课程预约：预约表
CREATE TABLE IF NOT EXISTS course_booking (
    booking_id   INT PRIMARY KEY AUTO_INCREMENT COMMENT '预约ID',
    member_id    INT NOT NULL COMMENT '会员ID，关联 member.member_id',
    course_id    INT NOT NULL COMMENT '课程ID，关联 course.course_id',
    booking_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '预约操作时间',
    status       TINYINT DEFAULT 1 COMMENT '状态：1有效预约 0已取消',
    remark       VARCHAR(255) COMMENT '备注',
    INDEX idx_member (member_id),
    INDEX idx_course (course_id),
    CONSTRAINT fk_booking_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    CONSTRAINT fk_booking_course FOREIGN KEY (course_id) REFERENCES course (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程预约表';
