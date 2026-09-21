package com.gym.service;

import com.gym.entity.Member;
import java.util.List;

public interface MemberService {
    List<Member> getAllMember();
    // 新增方法
    int insertMember(Member member);
}
