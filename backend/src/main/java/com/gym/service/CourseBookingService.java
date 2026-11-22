package com.gym.service;

import com.gym.entity.CourseBooking;

import java.util.List;

public interface CourseBookingService {

    List<CourseBooking> getAllBooking();

    CourseBooking getBookingById(Integer bookingId);

    int addBooking(CourseBooking booking);

    int cancelBooking(Integer bookingId);
}
