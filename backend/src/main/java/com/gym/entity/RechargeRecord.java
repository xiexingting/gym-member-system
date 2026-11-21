package com.gym.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 续费记录，对应 member_recharge 表。
 *
 * 注意：这张表数据库里已经存在了，字段为
 * recharge_id / member_id / recharge_money / recharge_time / add_month / operator
 */
public class RechargeRecord {
    private Integer rechargeId;
    private Integer memberId;
    private BigDecimal rechargeMoney;
    private Date rechargeTime;
    private Integer addMonth;
    private String operator;
    private Integer cardId;          // 关联卡种ID（可选，写入 member_recharge 表）
    /** 续费时顺手更新的会员备注（可选；仅作请求参数，不写入 member_recharge 表） */
    private String remark;

    public RechargeRecord() {
    }

    public Integer getRechargeId() {
        return rechargeId;
    }

    public void setRechargeId(Integer rechargeId) {
        this.rechargeId = rechargeId;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public BigDecimal getRechargeMoney() {
        return rechargeMoney;
    }

    public void setRechargeMoney(BigDecimal rechargeMoney) {
        this.rechargeMoney = rechargeMoney;
    }

    public Date getRechargeTime() {
        return rechargeTime;
    }

    public void setRechargeTime(Date rechargeTime) {
        this.rechargeTime = rechargeTime;
    }

    public Integer getAddMonth() {
        return addMonth;
    }

    public void setAddMonth(Integer addMonth) {
        this.addMonth = addMonth;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Integer getCardId() {
        return cardId;
    }

    public void setCardId(Integer cardId) {
        this.cardId = cardId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
