package com.gym.service.impl;

import com.gym.entity.ExpiryReminder;
import com.gym.entity.Member;
import com.gym.mapper.ExpiryReminderMapper;
import com.gym.mapper.MemberMapper;
import com.gym.service.ExpiryReminderService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class ExpiryReminderServiceImpl implements ExpiryReminderService {

    private static final Logger log = LoggerFactory.getLogger(ExpiryReminderServiceImpl.class);

    /** 提前提醒天数：7 天内到期算"即将到期" */
    private static final int WARN_DAYS = 7;

    @Resource
    private ExpiryReminderMapper reminderMapper;

    @Resource
    private MemberMapper memberMapper;

    @Override
    public List<ExpiryReminder> getReminders(Integer status, Integer type) {
        return reminderMapper.selectAll(status, type);
    }

    /**
     * 扫描提醒：事务内一次性完成"写提醒记录 + 更新会员状态"，
     * 同一天同一会员同一类型只记一条，重复执行不会刷屏。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int scanExpiry() {
        List<Member> members = memberMapper.selectNeedRemind(WARN_DAYS);
        Date now = new Date();
        int created = 0;

        for (Member member : members) {
            Date expireTime = member.getExpireTime();
            boolean expired = expireTime.before(now);
            int type = expired ? 2 : 1; // 2=已到期 1=即将到期

            // 当天已生成过同类型提醒就跳过
            if (reminderMapper.countExisting(member.getMemberId(), type, now) > 0) {
                continue;
            }

            ExpiryReminder reminder = new ExpiryReminder();
            reminder.setMemberId(member.getMemberId());
            reminder.setMemberName(member.getMemberName());
            reminder.setExpireTime(expireTime);
            reminder.setReminderType(type);
            reminder.setReminderDate(now);
            reminder.setStatus(0);
            reminderMapper.insert(reminder);
            created++;

            // 已到期会员自动更新状态为 3；手动停用(2)的会员在 SQL 里已被排除
            if (expired) {
                memberMapper.updateStatus(member.getMemberId(), 3);
            }
        }

        log.info("到期扫描完成：命中 {} 人，新增提醒 {} 条", members.size(), created);
        return created;
    }

    @Override
    public int markHandled(Integer reminderId, String remark) {
        if (reminderId == null) {
            throw new IllegalArgumentException("提醒ID不能为空");
        }
        return reminderMapper.markHandled(reminderId, remark);
    }
}
