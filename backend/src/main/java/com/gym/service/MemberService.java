package com.gym.service;

import com.gym.common.PageResult;
import com.gym.entity.Member;

import java.util.List;

public interface MemberService {
    List<Member> getAllMember();

    // 新增方法
    int insertMember(Member member);

    // 根据ID删除会员
    int deleteMember(Integer memberId);

    // 根据ID查询单个会员
    Member getMemberById(Integer memberId);

    // 修改会员
    int updateMember(Member member);

    // ===== 搜索 + 分页（A1 / A2）=====
    PageResult<Member> getMemberPage(String keyword, int pageNum, int pageSize);

    // ===== 状态管理（A4）：1=正常 2=停用 =====
    int updateMemberStatus(Integer memberId, Integer status);
}
