package com.gym.controller;

import com.gym.common.Result;
import com.gym.entity.CardType;
import com.gym.service.CardTypeService;
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

import java.util.List;

/**
 * 卡种 / 套餐管理接口（A5）
 * 路径风格与分工文档第七节保持一致。
 */
@RestController
@RequestMapping("/api/card")
@CrossOrigin
public class CardTypeController {

    @Resource
    private CardTypeService cardTypeService;

    /** 全部卡种：GET /api/card/list */
    @GetMapping("/list")
    public Result<List<CardType>> list() {
        return Result.success(cardTypeService.getAllCardType());
    }

    /** 上架卡种（续费下拉用）：GET /api/card/online */
    @GetMapping("/online")
    public Result<List<CardType>> online() {
        return Result.success(cardTypeService.getOnlineCardType());
    }

    /** 按 ID 查询：GET /api/card/get/{id} */
    @GetMapping("/get/{id}")
    public Result<CardType> get(@PathVariable("id") Integer cardId) {
        return Result.success(cardTypeService.getCardTypeById(cardId));
    }

    /** 新增：POST /api/card/add */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody CardType cardType) {
        int rows = cardTypeService.insertCardType(cardType);
        return rows > 0
                ? Result.success("新增成功", null)
                : Result.fail("新增失败，请稍后重试");
    }

    /** 修改：PUT /api/card/update */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody CardType cardType) {
        int rows = cardTypeService.updateCardType(cardType);
        return rows > 0
                ? Result.success("修改成功", null)
                : Result.fail("修改失败，未找到该卡种");
    }

    /** 删除：DELETE /api/card/delete/{id}（被使用时会被拦截） */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable("id") Integer cardId) {
        cardTypeService.deleteCardType(cardId);
        return Result.success("删除成功", null);
    }
}
