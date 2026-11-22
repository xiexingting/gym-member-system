package com.gym.controller;

import com.gym.common.Result;
import com.gym.entity.Coach;
import com.gym.service.CoachService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 教练管理接口（B1）
 * 路径风格与分工文档第七节保持一致。
 */
@RestController
@RequestMapping("/api/coach")
@CrossOrigin
public class CoachController {

    @Resource
    private CoachService coachService;

    /** 全部教练：GET /api/coach/list */
    @GetMapping("/list")
    public Result<List<Coach>> list() {
        return Result.success(coachService.getAllCoach());
    }

    /** 在职教练（课程排期用）：GET /api/coach/onjob */
    @GetMapping("/onjob")
    public Result<List<Coach>> onJob() {
        return Result.success(coachService.getOnJobCoach());
    }

    /** 按 ID 查询：GET /api/coach/get/{id} */
    @GetMapping("/get/{id}")
    public Result<Coach> get(@PathVariable("id") Integer coachId) {
        return Result.success(coachService.getCoachById(coachId));
    }

    /** 新增：POST /api/coach/add */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody Coach coach) {
        int rows = coachService.insertCoach(coach);
        return rows > 0
                ? Result.success("新增成功", null)
                : Result.fail("新增失败，请稍后重试");
    }

    /** 修改：PUT /api/coach/update */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody Coach coach) {
        int rows = coachService.updateCoach(coach);
        return rows > 0
                ? Result.success("修改成功", null)
                : Result.fail("修改失败，未找到该教练");
    }

    /** 删除：DELETE /api/coach/delete/{id} */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable("id") Integer coachId) {
        coachService.deleteCoach(coachId);
        return Result.success("删除成功", null);
    }
}
