package com.gym.service;

import com.gym.entity.Course;

import java.util.List;

public interface CourseService {

    List<Course> getAllCourse();

    Course getCourseById(Integer courseId);

    int insertCourse(Course course);

    int updateCourse(Course course);

    void deleteCourse(Integer courseId);
}
