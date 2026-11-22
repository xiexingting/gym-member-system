package com.gym.mapper;

import com.gym.entity.CourseBooking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseBookingMapper {

    /** 全部预约，JOIN 带出课程、会员、教练信息 */
    List<CourseBooking> selectAll();

    CourseBooking selectById(Integer bookingId);

    /** 统计某会员对某课程的有效预约数（防重复预约） */
    int countActive(@Param("memberId") Integer memberId,
                    @Param("courseId") Integer courseId);

    int insert(CourseBooking booking);

    /** 取消：把状态置为 0 */
    int cancelById(Integer bookingId);
}
