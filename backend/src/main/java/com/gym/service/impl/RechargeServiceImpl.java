package com.gym.service.impl;

import com.gym.entity.Member;
import com.gym.entity.RechargeRecord;
import com.gym.mapper.MemberMapper;
import com.gym.mapper.RechargeMapper;
import com.gym.service.RechargeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class RechargeServiceImpl implements RechargeService {

    @Resource
    private RechargeMapper rechargeMapper;

    @Resource
    private MemberMapper memberMapper;

    /**
     * 续费：更新会员到期时间 + 写续费记录，放在一个事务里，
     * 任何一步失败都会整体回滚，避免出现"钱收了但会员没延期"。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void renew(RechargeRecord record) {
        if (record == null || record.getMemberId() == null) {
            throw new IllegalArgumentException("会员ID不能为空");
        }
        if (record.getAddMonth() == null || record.getAddMonth() <= 0) {
            throw new IllegalArgumentException("续费月数必须大于 0");
        }
        if (record.getRemark() != null && record.getRemark().length() > 200) {
            throw new IllegalArgumentException("备注不能超过 200 个字符");
        }
        if (record.getRechargeMoney() == null) {
            record.setRechargeMoney(BigDecimal.ZERO);
        }

        Member member = memberMapper.selectById(record.getMemberId());
        if (member == null) {
            throw new IllegalArgumentException("未找到该会员，无法续费");
        }

        // 基准时间：到期时间在未来就从到期日往后加；已过期则从今天开始算
        Date expireTime = member.getExpireTime();
        LocalDateTime base = expireTime == null
                ? LocalDateTime.now()
                : LocalDateTime.ofInstant(expireTime.toInstant(), ZoneId.systemDefault());
        if (base.isBefore(LocalDateTime.now())) {
            base = LocalDateTime.now();
        }

        // 关键：先把续费记录插入，再更新会员。若会员更新失败，事务回滚，记录也不会留下
        rechargeMapper.insert(record);

        Date newExpireTime = Date.from(base.plusMonths(record.getAddMonth())
                .atZone(ZoneId.systemDefault()).toInstant());
        memberMapper.updateExpireTime(record.getMemberId(), newExpireTime);
        // 续费后把"已到期(3)"恢复为"正常(1)"；手动停用(2)不受影响
        memberMapper.restoreStatusAfterRenew(record.getMemberId());

        // 备注可选：请求带了 remark 才更新；传空字符串表示清空，没带（null）就保持原样
        if (record.getRemark() != null) {
            memberMapper.updateRemark(record.getMemberId(), record.getRemark());
        }
    }

    @Override
    public List<RechargeRecord> getRecordsByMemberId(Integer memberId) {
        return rechargeMapper.selectByMemberId(memberId);
    }
}
