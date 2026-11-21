package com.gym.task;

import com.gym.service.ExpiryReminderService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 到期提醒定时任务（A6）：每天上午 9:00 自动扫描一次。
 * 想立即验证可调用 POST /api/reminder/scan 手动触发。
 */
@Component
public class ExpiryScanTask {

    private static final Logger log = LoggerFactory.getLogger(ExpiryScanTask.class);

    @Resource
    private ExpiryReminderService reminderService;

    @Scheduled(cron = "0 0 9 * * ?")
    public void scan() {
        try {
            int count = reminderService.scanExpiry();
            log.info("定时任务：到期扫描完成，新增 {} 条提醒", count);
        } catch (Exception e) {
            log.error("定时任务：到期扫描失败", e);
        }
    }
}
