-- 基础表：会员主表 member
-- 说明：member 表是所有业务表的基础（续费、卡种、预约都外键引用它），
--       但此前只存在于本地库，从未纳入版本库。新电脑建库时请先执行本脚本。
-- 可重复执行：CREATE TABLE IF NOT EXISTS，已有表会跳过、不破坏现有数据。

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS member (
    member_id     INT          NOT NULL AUTO_INCREMENT COMMENT '会员编号',
    member_name   VARCHAR(50)  NOT NULL COMMENT '会员姓名',
    phone         VARCHAR(20)  NOT NULL COMMENT '手机号',
    gender        VARCHAR(10)  DEFAULT NULL COMMENT '性别',
    id_card       VARCHAR(30)  DEFAULT NULL COMMENT '身份证号',
    register_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    expire_time   DATETIME     DEFAULT NULL COMMENT '会员到期时间',
    member_status TINYINT      DEFAULT 1 COMMENT '会员状态：1正常 0已过期 2停用 3到期',
    remark        VARCHAR(200) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (member_id),
    UNIQUE KEY uk_member_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员信息表';
