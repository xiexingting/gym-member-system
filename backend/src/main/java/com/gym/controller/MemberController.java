package com.gym.controller;

import com.gym.common.PageResult;
import com.gym.common.Result;
import com.gym.entity.Member;
import com.gym.entity.RechargeRecord;
import com.gym.service.MemberService;
import com.gym.service.RechargeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @Resource
    private RechargeService rechargeService;

    /** 查询会员列表（原接口，保留兼容现有前端） */
    @GetMapping("/list")
    public Result<List<Member>> memberList() {
        return Result.success(memberService.getAllMember());
    }

    /** 搜索 + 分页：GET /api/member/page?keyword=张&pageNum=1&pageSize=10 */
    @GetMapping("/page")
    public Result<PageResult<Member>> memberPage(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return Result.success(memberService.getMemberPage(keyword, pageNum, pageSize));
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

    // ============ 续费（A3）============

    /**
     * 会员续费
     * POST /api/member/renew
     * body: { "memberId": 6, "addMonth": 12, "rechargeMoney": 1200, "operator": "管理员", "remark": "年卡会员" }
     * remark 可选：不传则保持原备注，传空字符串则清空备注
     */
    @PostMapping("/renew")
    public Result<Void> renew(@RequestBody RechargeRecord record) {
        rechargeService.renew(record);
        return Result.success("续费成功", null);
    }

    /** 查询某会员的续费记录：GET /api/member/recharge/list/{memberId} */
    @GetMapping("/recharge/list/{memberId}")
    public Result<List<RechargeRecord>> rechargeList(@PathVariable("memberId") Integer memberId) {
        return Result.success(rechargeService.getRecordsByMemberId(memberId));
    }

    // ============ 状态管理（A4）============

    /**
     * 启用 / 停用会员：PUT /api/member/status/{id}?status=2
     * status: 1=正常，2=停用
     */
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable("id") Integer memberId,
                                     @RequestParam("status") Integer status) {
        int rows = memberService.updateMemberStatus(memberId, status);
        return rows > 0
                ? Result.success("状态更新成功", null)
                : Result.fail("状态更新失败，未找到该会员");
    }
}
