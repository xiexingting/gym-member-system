package com.gym.service.impl;

import com.gym.common.PageResult;
import com.gym.entity.Member;
import com.gym.mapper.MemberMapper;
import com.gym.service.MemberService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

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

    // ============ 搜索 + 分页 ============
    @Override
    public PageResult<Member> getMemberPage(String keyword, int pageNum, int pageSize) {
        // 参数兜底，防止前端传 0 或负数导致 LIMIT 报错
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        if (pageSize > 100) pageSize = 100;
        int offset = (pageNum - 1) * pageSize;

        int total = memberMapper.countByKeyword(keyword);
        List<Member> list = memberMapper.selectByPage(keyword, offset, pageSize);
        return new PageResult<>(list, total, pageNum, pageSize);
    }

    // ============ 状态管理（A4）============
    @Override
    public int updateMemberStatus(Integer memberId, Integer status) {
        if (memberId == null) {
            throw new IllegalArgumentException("会员ID不能为空");
        }
        if (status == null || (status != 1 && status != 2)) {
            throw new IllegalArgumentException("状态值无效，只能是 1(正常) 或 2(停用)");
        }
        return memberMapper.updateStatus(memberId, status);
    }
}
