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

    /** 调整已预约人数（B4 取消预约释放名额时使用，delta 为负） */
    int updateBookedCount(@org.apache.ibatis.annotations.Param("courseId") Integer courseId,
                          @org.apache.ibatis.annotations.Param("delta") Integer delta);

    /**
     * 原子占座：仅当课程上架且 booked_count < max_capacity 时才 +1。
     * 返回影响行数：1 = 抢到位；0 = 名额已满或课程已下架。
     * 名额判断与 +1 在同一条 UPDATE 内完成，靠 InnoDB 行锁防止并发超卖。
     */
    int tryBookSeat(Integer courseId);
}
