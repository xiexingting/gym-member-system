package com.gym.controller;

import com.gym.entity.Member;
import com.gym.service.MemberService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/member")
@CrossOrigin // 允许前端跨域访问，开发用
public class MemberController {

    @Resource
    private MemberService memberService;

    @GetMapping("/list")
    public List<Member> memberList(){
        return memberService.getAllMember();
    }

    // ============ 新增：新增会员接口 ============
    @PostMapping("/add")
    public String addMember(@RequestBody Member member){
        // 自动设置注册时间为当前时间
        member.setRegisterTime(new java.util.Date());
        // 默认会员状态：1=正常
        member.setMemberStatus(1);
        int rows = memberService.insertMember(member);
        return rows > 0 ? "success" : "fail";
    }

    // ============ 删除会员接口：ID 拼在地址里 ============
    @DeleteMapping("/delete/{id}")
    public String deleteMember(@PathVariable("id") Integer memberId){
        int rows = memberService.deleteMember(memberId);
        return rows > 0 ? "success" : "fail";
    }

    // ============ 按ID查询单个会员 ============
    @GetMapping("/get/{id}")
    public Member getMember(@PathVariable("id") Integer memberId){
        return memberService.getMemberById(memberId);
    }

    // ============ 修改会员 ============
    @PutMapping("/update")
    public String updateMember(@RequestBody Member member){
        int rows = memberService.updateMember(member);
        return rows > 0 ? "success" : "fail";
    }

}
