package com.gym.controller;

import com.gym.common.Result;
import com.gym.entity.Course;
import com.gym.service.CourseService;
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
 * 课程管理接口（B3）
 */
@RestController
@RequestMapping("/api/course")
@CrossOrigin
public class CourseController {

    @Resource
    private CourseService courseService;

    /** 全部课程：GET /api/course/list */
    @GetMapping("/list")
    public Result<List<Course>> list() {
        return Result.success(courseService.getAllCourse());
    }

    /** 按 ID 查询：GET /api/course/get/{id} */
    @GetMapping("/get/{id}")
    public Result<Course> get(@PathVariable("id") Integer courseId) {
        return Result.success(courseService.getCourseById(courseId));
    }

    /** 新增：POST /api/course/add */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody Course course) {
        int rows = courseService.insertCourse(course);
        return rows > 0
                ? Result.success("新增成功", null)
                : Result.fail("新增失败，请稍后重试");
    }

    /** 修改：PUT /api/course/update */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody Course course) {
        int rows = courseService.updateCourse(course);
        return rows > 0
                ? Result.success("修改成功", null)
                : Result.fail("修改失败，请稍后重试");
    }

    /** 删除：DELETE /api/course/delete/{id} */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable("id") Integer courseId) {
        courseService.deleteCourse(courseId);
        return Result.success("删除成功", null);
    }
}
