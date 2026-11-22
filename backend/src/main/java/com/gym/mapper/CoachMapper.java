package com.gym.mapper;

import com.gym.entity.Coach;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CoachMapper {

    /** 全部教练，在职的排前面 */
    List<Coach> selectAll();

    /** 仅在职教练，供课程排期下拉选择 */
    List<Coach> selectOnJob();

    Coach selectById(Integer coachId);

    int insert(Coach coach);

    int updateById(Coach coach);

    int deleteById(Integer coachId);

    /** 统计该教练名下的课程数量（删除教练前检查） */
    int countCourseByCoach(Integer coachId);
}
