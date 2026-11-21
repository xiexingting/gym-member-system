package com.gym.mapper;

import com.gym.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
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

    // ===== 搜索 + 分页（A1 / A2）=====
    /** 按关键字分页查询，keyword 可为空 */
    List<Member> selectByPage(@Param("keyword") String keyword,
                              @Param("offset") int offset,
                              @Param("pageSize") int pageSize);

    /** 统计符合关键字的会员总数，用于分页 */
    int countByKeyword(@Param("keyword") String keyword);

    // ===== 续费时只更新到期时间（A3）=====
    int updateExpireTime(@Param("memberId") Integer memberId,
                         @Param("expireTime") Date expireTime);

    // ===== 续费时可选顺手更新会员备注 =====
    int updateRemark(@Param("memberId") Integer memberId,
                     @Param("remark") String remark);

    // ===== 状态管理（A4）：1=正常 2=停用 3=已到期（定时任务写）=====
    int updateStatus(@Param("memberId") Integer memberId,
                     @Param("status") Integer status);

    /** 续费后把"已到期(3)"恢复为"正常(1)"，不动手动停用的会员 */
    int restoreStatusAfterRenew(@Param("memberId") Integer memberId);

    // ===== 到期提醒（A6）：查已到期或 days 天内到期的会员（排除手动停用）=====
    List<Member> selectNeedRemind(@Param("days") int days);
}
