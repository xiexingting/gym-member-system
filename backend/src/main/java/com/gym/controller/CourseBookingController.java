package com.gym.controller;

import com.gym.common.Result;
import com.gym.entity.CourseBooking;
import com.gym.service.CourseBookingService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 课程预约接口（B4）
 */
@RestController
@RequestMapping("/api/booking")
@CrossOrigin
public class CourseBookingController {

    @Resource
    private CourseBookingService bookingService;

    /** 全部预约：GET /api/booking/list */
    @GetMapping("/list")
    public Result<List<CourseBooking>> list() {
        return Result.success(bookingService.getAllBooking());
    }

    /** 按 ID 查询：GET /api/booking/get/{id} */
    @GetMapping("/get/{id}")
    public Result<CourseBooking> get(@PathVariable("id") Integer bookingId) {
        return Result.success(bookingService.getBookingById(bookingId));
    }

    /** 新增预约：POST /api/booking/add */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody CourseBooking booking) {
        int rows = bookingService.addBooking(booking);
        return rows > 0
                ? Result.success("预约成功", null)
                : Result.fail("预约失败，请稍后重试");
    }

    /** 取消预约：PUT /api/booking/cancel/{id} */
    @org.springframework.web.bind.annotation.PutMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable("id") Integer bookingId) {
        int rows = bookingService.cancelBooking(bookingId);
        return rows > 0
                ? Result.success("已取消预约", null)
                : Result.fail("取消失败，请稍后重试");
    }
}
