package com.gym.mapper;

import com.gym.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface MemberMapper {
    List<Member> selectAll();
    // 新增
    int insert(Member member);
    // 根据会员ID删除
    int deleteById(Integer memberId);
}
