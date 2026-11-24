package com.gym.service.impl;

import com.gym.entity.Course;
import com.gym.entity.CourseBooking;
import com.gym.entity.Member;
import com.gym.mapper.CourseBookingMapper;
import com.gym.mapper.CourseMapper;
import com.gym.mapper.MemberMapper;
import com.gym.service.CourseBookingService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseBookingServiceImpl implements CourseBookingService {

    @Resource
    private CourseBookingMapper bookingMapper;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private MemberMapper memberMapper;

    @Override
    public List<CourseBooking> getAllBooking() {
        return bookingMapper.selectAll();
    }

    @Override
    public CourseBooking getBookingById(Integer bookingId) {
        return bookingMapper.selectById(bookingId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addBooking(CourseBooking booking) {
        if (booking == null || booking.getMemberId() == null) {
            throw new IllegalArgumentException("请选择预约会员");
        }
        if (booking.getCourseId() == null) {
            throw new IllegalArgumentException("请选择预约课程");
        }

        // 1. 会员必须存在
        Member member = memberMapper.selectById(booking.getMemberId());
        if (member == null) {
            throw new IllegalArgumentException("所选会员不存在");
        }

        // 2. 课程必须存在且处于上架状态
        Course course = courseMapper.selectById(booking.getCourseId());
        if (course == null) {
            throw new IllegalArgumentException("所选课程不存在");
        }
        if (course.getStatus() == null || course.getStatus() == 0) {
            throw new IllegalArgumentException("该课程已下架，无法预约");
        }

        // 3. 同一会员同一课程不能重复预约
        if (bookingMapper.countActive(booking.getMemberId(), booking.getCourseId()) > 0) {
            throw new IllegalArgumentException("该会员已预约这门课，请勿重复预约");
        }

        // 4. 原子占座：名额判断与 +1 合并成一条带条件的 UPDATE，靠 InnoDB 行锁防并发超卖。
        //    原来的写法是「先查 booked_count → Java 判断 → 再 +1」，两个请求同时通过
        //    校验就会把 10 人的课约进 11 人。现在抢不到位时影响行数为 0，直接拒绝。
        int seats = courseMapper.tryBookSeat(booking.getCourseId());
        if (seats == 0) {
            // 名额已满，或课程在查询后被下架（SQL 里同时校验了 status=1）
            throw new IllegalArgumentException("该课程名额已满或已下架，请选择其他课程");
        }

        // 5. 占座成功后写预约记录；两步在同一事务里，
        //    若插入失败抛异常，占座会自动回滚，不会出现「占了位却没记录」。
        int rows = bookingMapper.insert(booking);
        if (rows == 0) {
            throw new IllegalStateException("预约记录写入失败，请稍后重试");
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelBooking(Integer bookingId) {
        if (bookingId == null) {
            throw new IllegalArgumentException("预约ID不能为空");
        }
        CourseBooking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("未找到该预约记录");
        }
        if (booking.getStatus() == null || booking.getStatus() == 0) {
            throw new IllegalArgumentException("该预约已取消，无需重复操作");
        }

        // 标记取消，同时释放名额，两步在同一事务里
        int rows = bookingMapper.cancelById(bookingId);
        courseMapper.updateBookedCount(booking.getCourseId(), -1);
        return rows;
    }
}
