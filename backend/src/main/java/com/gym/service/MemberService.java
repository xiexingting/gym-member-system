package com.gym.service;

import com.gym.entity.Member;
import java.util.List;

public interface MemberService {
    List<Member> getAllMember();
    // 新增方法
    int insertMember(Member member);
    // 根据ID删除会员
    int deleteMember(Integer memberId);
}
