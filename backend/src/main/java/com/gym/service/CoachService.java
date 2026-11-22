package com.gym.service;

import com.gym.entity.Coach;

import java.util.List;

public interface CoachService {

    List<Coach> getAllCoach();

    List<Coach> getOnJobCoach();

    Coach getCoachById(Integer coachId);

    int insertCoach(Coach coach);

    int updateCoach(Coach coach);

    void deleteCoach(Integer coachId);
}
