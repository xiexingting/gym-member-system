package com.gym.service.impl;

import com.gym.entity.Coach;
import com.gym.entity.Course;
import com.gym.mapper.CoachMapper;
import com.gym.mapper.CourseMapper;
import com.gym.service.CourseService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private CoachMapper coachMapper;

    @Override
    public List<Course> getAllCourse() {
        return courseMapper.selectAll();
    }

    @Override
    public Course getCourseById(Integer courseId) {
        return courseMapper.selectById(courseId);
    }

    @Override
    public int insertCourse(Course course) {
        validate(course);
        course.setBookedCount(0); // 新课没人预约
        if (course.getStatus() == null) {
            course.setStatus(1);
        }
        return courseMapper.insert(course);
    }

    @Override
    public int updateCourse(Course course) {
        if (course == null || course.getCourseId() == null) {
            throw new IllegalArgumentException("课程ID不能为空");
        }
        Course old = courseMapper.selectById(course.getCourseId());
        if (old == null) {
            throw new IllegalArgumentException("未找到该课程，无法修改");
        }
        validate(course);
        // 已预约人数不在修改接口里变动，保留原值
        course.setBookedCount(old.getBookedCount());
        return courseMapper.updateById(course);
    }

    @Override
    public void deleteCourse(Integer courseId) {
        if (courseId == null) {
            throw new IllegalArgumentException("课程ID不能为空");
        }
        if (courseMapper.selectById(courseId) == null) {
            throw new IllegalArgumentException("未找到该课程，无法删除");
        }
        courseMapper.deleteById(courseId);
    }

    /** 课程参数校验：名称、教练、时间、容量都要合法 */
    private void validate(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("课程信息不能为空");
        }
        if (course.getCourseName() == null || course.getCourseName().trim().isEmpty()) {
            throw new IllegalArgumentException("课程名称不能为空");
        }
        if (course.getCoachId() == null) {
            throw new IllegalArgumentException("请选择授课教练");
        }
        Coach coach = coachMapper.selectById(course.getCoachId());
        if (coach == null) {
            throw new IllegalArgumentException("所选教练不存在");
        }
        if (course.getCourseTime() == null) {
            throw new IllegalArgumentException("请选择上课时间");
        }
        if (course.getMaxCapacity() == null || course.getMaxCapacity() <= 0) {
            throw new IllegalArgumentException("最大容量必须大于 0");
        }
        if (course.getDuration() != null && course.getDuration() <= 0) {
            throw new IllegalArgumentException("课程时长必须大于 0");
        }
        if (course.getStatus() != null && course.getStatus() != 0 && course.getStatus() != 1) {
            throw new IllegalArgumentException("状态值无效，只能是 1(上架) 或 0(下架)");
        }
    }
}
