package com.gym.mapper;

import com.gym.entity.ExpiryReminder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface ExpiryReminderMapper {

    /** 提醒名单，可按处理状态 / 提醒类型筛选，null 表示不筛选 */
    List<ExpiryReminder> selectAll(@Param("status") Integer status,
                                   @Param("type") Integer type);

    /** 当天该会员同类型是否已生成过提醒（防重复） */
    int countExisting(@Param("memberId") Integer memberId,
                      @Param("type") Integer type,
                      @Param("date") Date date);

    int insert(ExpiryReminder reminder);

    /** 标记已处理，可附带处理备注 */
    int markHandled(@Param("reminderId") Integer reminderId,
                    @Param("remark") String remark);
}
