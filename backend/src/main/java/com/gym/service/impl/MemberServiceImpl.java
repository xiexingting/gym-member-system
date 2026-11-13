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

    // ============ 删除 ============
    @Override
    public int deleteMember(Integer memberId) {
        return memberMapper.deleteById(memberId);
    }

    // ============ 按ID查询 ============
    @Override
    public Member getMemberById(Integer memberId) {
        return memberMapper.selectById(memberId);
    }

    // ============ 修改 ============
    @Override
    public int updateMember(Member member) {
        return memberMapper.updateById(member);
    }


}
