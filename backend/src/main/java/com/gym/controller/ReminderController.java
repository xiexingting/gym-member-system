package com.gym.controller;

import com.gym.common.Result;
import com.gym.entity.ExpiryReminder;
import com.gym.service.ExpiryReminderService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 到期提醒接口（A6）
 */
@RestController
@RequestMapping("/api/reminder")
@CrossOrigin
public class ReminderController {

    @Resource
    private ExpiryReminderService reminderService;

    /** 提醒名单：GET /api/reminder/list?status=0&type=2（参数均可省略） */
    @GetMapping("/list")
    public Result<List<ExpiryReminder>> list(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "type", required = false) Integer type) {
        return Result.success(reminderService.getReminders(status, type));
    }

    /** 手动触发一次扫描：POST /api/reminder/scan（定时任务每天 9:00 也会自动跑） */
    @PostMapping("/scan")
    public Result<Integer> scan() {
        int count = reminderService.scanExpiry();
        return Result.success("扫描完成，新增 " + count + " 条提醒", count);
    }

    /** 标记已处理：PUT /api/reminder/handle/{id}?remark=xxx */
    @PutMapping("/handle/{id}")
    public Result<Void> handle(@PathVariable("id") Integer reminderId,
                               @RequestParam(value = "remark", required = false) String remark) {
        int rows = reminderService.markHandled(reminderId, remark);
        return rows > 0
                ? Result.success("已标记为已处理", null)
                : Result.fail("未找到该提醒记录");
    }
}
