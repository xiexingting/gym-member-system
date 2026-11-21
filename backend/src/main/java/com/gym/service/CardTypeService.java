package com.gym.service;

import com.gym.entity.CardType;

import java.util.List;

public interface CardTypeService {

    /** 全部卡种（上架优先） */
    List<CardType> getAllCardType();

    /** 仅上架卡种，供续费下拉使用 */
    List<CardType> getOnlineCardType();

    CardType getCardTypeById(Integer cardId);

    int insertCardType(CardType cardType);

    int updateCardType(CardType cardType);

    /** 删除卡种；若已被续费记录引用则抛异常拦截 */
    void deleteCardType(Integer cardId);
}
