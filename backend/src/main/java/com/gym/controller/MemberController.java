package com.gym.controller;

import com.gym.common.Result;
import com.gym.entity.Member;
import com.gym.service.MemberService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * 会员管理接口
 * 路径、参数、业务逻辑均保持原样，只把返回值统一成 {code, message, data}
 */
@RestController
@RequestMapping("/api/member")
@CrossOrigin // 允许前端跨域访问，开发用
public class MemberController {

    @Resource
    private MemberService memberService;

    /** 查询会员列表 */
    @GetMapping("/list")
    public Result<List<Member>> memberList() {
        return Result.success(memberService.getAllMember());
    }

    /** 新增会员：注册时间取当前时间，默认状态 1=正常 */
    @PostMapping("/add")
    public Result<Void> addMember(@RequestBody Member member) {
        member.setRegisterTime(new Date());
        member.setMemberStatus(1);
        int rows = memberService.insertMember(member);
        return rows > 0
                ? Result.success("新增成功", null)
                : Result.fail("新增失败，请稍后重试");
    }

    /** 按 ID 删除会员 */
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteMember(@PathVariable("id") Integer memberId) {
        int rows = memberService.deleteMember(memberId);
        return rows > 0
                ? Result.success("删除成功", null)
                : Result.fail("删除失败，未找到该会员");
    }

    /** 按 ID 查询单个会员 */
    @GetMapping("/get/{id}")
    public Result<Member> getMember(@PathVariable("id") Integer memberId) {
        return Result.success(memberService.getMemberById(memberId));
    }

    /** 修改会员 */
    @PutMapping("/update")
    public Result<Void> updateMember(@RequestBody Member member) {
        int rows = memberService.updateMember(member);
        return rows > 0
                ? Result.success("修改成功", null)
                : Result.fail("修改失败，未找到该会员");
    }
}
