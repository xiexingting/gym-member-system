package com.gym.service.impl;

import com.gym.entity.CardType;
import com.gym.mapper.CardTypeMapper;
import com.gym.service.CardTypeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CardTypeServiceImpl implements CardTypeService {

    @Resource
    private CardTypeMapper cardTypeMapper;

    @Override
    public List<CardType> getAllCardType() {
        return cardTypeMapper.selectAll();
    }

    @Override
    public List<CardType> getOnlineCardType() {
        return cardTypeMapper.selectOnline();
    }

    @Override
    public CardType getCardTypeById(Integer cardId) {
        return cardTypeMapper.selectById(cardId);
    }

    @Override
    public int insertCardType(CardType cardType) {
        validate(cardType);
        if (cardType.getStatus() == null) {
            cardType.setStatus(1); // 默认上架
        }
        return cardTypeMapper.insert(cardType);
    }

    @Override
    public int updateCardType(CardType cardType) {
        if (cardType == null || cardType.getCardId() == null) {
            throw new IllegalArgumentException("卡种ID不能为空");
        }
        validate(cardType);
        return cardTypeMapper.updateById(cardType);
    }

    @Override
    public void deleteCardType(Integer cardId) {
        if (cardId == null) {
            throw new IllegalArgumentException("卡种ID不能为空");
        }
        if (cardTypeMapper.selectById(cardId) == null) {
            throw new IllegalArgumentException("未找到该卡种，无法删除");
        }
        // 已被会员续费使用过的卡种不能删除，避免历史记录里出现"孤儿卡种"
        if (cardTypeMapper.countUsedByCard(cardId) > 0) {
            throw new IllegalArgumentException("该卡种已被会员使用，不能删除；如需停用请改为下架");
        }
        cardTypeMapper.deleteById(cardId);
    }

    /** 统一参数校验：名称、价格、卡种类型与对应的有效期字段 */
    private void validate(CardType cardType) {
        if (cardType == null) {
            throw new IllegalArgumentException("卡种信息不能为空");
        }
        if (cardType.getCardName() == null || cardType.getCardName().trim().isEmpty()) {
            throw new IllegalArgumentException("卡种名称不能为空");
        }
        if (cardType.getPrice() == null
                || cardType.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("价格不能为空且不能为负数");
        }

        String type = cardType.getCardType();
        if (!"时长卡".equals(type) && !"次卡".equals(type)) {
            throw new IllegalArgumentException("卡种类型只能是「时长卡」或「次卡」");
        }
        if ("时长卡".equals(type)) {
            if (cardType.getValidDays() == null || cardType.getValidDays() <= 0) {
                throw new IllegalArgumentException("时长卡的有效天数必须大于 0");
            }
            cardType.setTotalTimes(null); // 时长卡不存次数
        } else {
            if (cardType.getTotalTimes() == null || cardType.getTotalTimes() <= 0) {
                throw new IllegalArgumentException("次卡的总次数必须大于 0");
            }
            cardType.setValidDays(null); // 次卡不存天数
        }

        if (cardType.getStatus() != null
                && cardType.getStatus() != 0 && cardType.getStatus() != 1) {
            throw new IllegalArgumentException("状态值无效，只能是 1(上架) 或 0(下架)");
        }
    }
}
