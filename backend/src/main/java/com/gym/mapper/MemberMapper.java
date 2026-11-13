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
        // 根据ID查询单个会员
        Member selectById(Integer memberId);
        // 根据ID修改会员信息
        int updateById(Member member);

}
