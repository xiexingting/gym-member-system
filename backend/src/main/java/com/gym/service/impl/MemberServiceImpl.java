package com.gym.service.impl;

import com.gym.entity.Member;
import com.gym.mapper.MemberMapper;
import com.gym.service.MemberService;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import java.util.List;

@Service
public class MemberServiceImpl implements MemberService {

    @Resource
    private MemberMapper memberMapper;

    @Override
    public List<Member> getAllMember() {
        return memberMapper.selectAll();
    }

    // ============ 新增 ============
    @Override
    public int insertMember(Member member) {
        return memberMapper.insert(member);
    }
}
