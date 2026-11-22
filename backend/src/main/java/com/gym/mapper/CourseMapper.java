package com.gym.mapper;

import com.gym.entity.Course;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CourseMapper {

    /** 全部课程，按上课时间倒序，JOIN 带出教练姓名 */
    List<Course> selectAll();

    Course selectById(Integer courseId);

    int insert(Course course);

    int updateById(Course course);

    int deleteById(Integer courseId);

    /** 调整已预约人数（B4 预约/取消时使用，delta 为正或负） */
    int updateBookedCount(@org.apache.ibatis.annotations.Param("courseId") Integer courseId,
                          @org.apache.ibatis.annotations.Param("delta") Integer delta);
}
