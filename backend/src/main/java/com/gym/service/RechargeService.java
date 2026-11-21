package com.gym.service;

import com.gym.entity.RechargeRecord;

import java.util.List;

public interface RechargeService {

    /**
     * 会员续费：延长到期时间 + 写入续费记录（同一事务）
     *
     * @param record 至少提供 memberId、addMonth；rechargeMoney、operator、remark 可选
     *               remark 为 null 时不改会员备注，传空字符串表示清空备注
     */
    void renew(RechargeRecord record);

    /** 查询某个会员的续费记录 */
    List<RechargeRecord> getRecordsByMemberId(Integer memberId);
}
