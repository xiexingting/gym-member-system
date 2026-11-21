package com.gym.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 卡种 / 套餐，对应 member_card_type 表（A5）。
 *
 * card_type = 时长卡：用 valid_days 表示有效天数
 * card_type = 次卡  ：用 total_times 表示总次数
 */
public class CardType {
    private Integer cardId;
    private String cardName;
    private BigDecimal price;
    private Integer validDays;
    private String cardType;
    private Integer totalTimes;
    private String remark;
    private Integer status;          // 1=上架 0=下架
    private Date createTime;

    public CardType() {
    }

    public Integer getCardId() {
        return cardId;
    }

    public void setCardId(Integer cardId) {
        this.cardId = cardId;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getValidDays() {
        return validDays;
    }

    public void setValidDays(Integer validDays) {
        this.validDays = validDays;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public Integer getTotalTimes() {
        return totalTimes;
    }

    public void setTotalTimes(Integer totalTimes) {
        this.totalTimes = totalTimes;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
