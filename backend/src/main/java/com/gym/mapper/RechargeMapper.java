package com.gym.mapper;

import com.gym.entity.RechargeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RechargeMapper {

    /** 新增一条续费记录 */
    int insert(RechargeRecord record);

    /** 按会员ID查询续费记录（按时间倒序，最新在前） */
    List<RechargeRecord> selectByMemberId(@Param("memberId") Integer memberId);
}
