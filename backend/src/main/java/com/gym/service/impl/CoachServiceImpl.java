package com.gym.service.impl;

import com.gym.entity.Coach;
import com.gym.mapper.CoachMapper;
import com.gym.service.CoachService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoachServiceImpl implements CoachService {

    @Resource
    private CoachMapper coachMapper;

    @Override
    public List<Coach> getAllCoach() {
        return coachMapper.selectAll();
    }

    @Override
    public List<Coach> getOnJobCoach() {
        return coachMapper.selectOnJob();
    }

    @Override
    public Coach getCoachById(Integer coachId) {
        return coachMapper.selectById(coachId);
    }

    @Override
    public int insertCoach(Coach coach) {
        validate(coach);
        if (coach.getStatus() == null) {
            coach.setStatus(1); // 默认在职
        }
        return coachMapper.insert(coach);
    }

    @Override
    public int updateCoach(Coach coach) {
        if (coach == null || coach.getCoachId() == null) {
            throw new IllegalArgumentException("教练ID不能为空");
        }
        validate(coach);
        return coachMapper.updateById(coach);
    }

    @Override
    public void deleteCoach(Integer coachId) {
        if (coachId == null) {
            throw new IllegalArgumentException("教练ID不能为空");
        }
        if (coachMapper.selectById(coachId) == null) {
            throw new IllegalArgumentException("未找到该教练，无法删除");
        }
        if (coachMapper.countCourseByCoach(coachId) > 0) {
            throw new IllegalArgumentException("该教练名下还有课程，请先删除或改派相关课程");
        }
        coachMapper.deleteById(coachId);
    }

    /** 统一参数校验：姓名必填，状态值只能是 1(在职) 或 0(离职) */
    private void validate(Coach coach) {
        if (coach == null) {
            throw new IllegalArgumentException("教练信息不能为空");
        }
        if (coach.getCoachName() == null || coach.getCoachName().trim().isEmpty()) {
            throw new IllegalArgumentException("教练姓名不能为空");
        }
        if (coach.getStatus() != null && coach.getStatus() != 0 && coach.getStatus() != 1) {
            throw new IllegalArgumentException("状态值无效，只能是 1(在职) 或 0(离职)");
        }
    }
}
