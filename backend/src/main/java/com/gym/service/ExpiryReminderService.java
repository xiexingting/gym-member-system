package com.gym.service;

import com.gym.entity.ExpiryReminder;

import java.util.List;

public interface ExpiryReminderService {

    /** 提醒名单，可按状态 / 类型筛选 */
    List<ExpiryReminder> getReminders(Integer status, Integer type);

    /** 执行一次到期扫描：生成提醒记录 + 把已到期会员状态改为 3，返回新增提醒条数 */
    int scanExpiry();

    /** 标记提醒已处理 */
    int markHandled(Integer reminderId, String remark);
}
