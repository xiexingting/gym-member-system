package com.gym.mapper;

import com.gym.entity.CardType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CardTypeMapper {

    /** 全部卡种，上架的排前面 */
    List<CardType> selectAll();

    /** 仅上架卡种，供会员续费时下拉选择 */
    List<CardType> selectOnline();

    CardType selectById(Integer cardId);

    int insert(CardType cardType);

    int updateById(CardType cardType);

    int deleteById(Integer cardId);

    /** 该卡种是否已被续费记录引用（用于删除拦截） */
    int countUsedByCard(@Param("cardId") Integer cardId);
}
