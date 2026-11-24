package com.gym.common;

import com.gym.entity.Member;

import java.util.List;

/**
 * 敏感信息脱敏工具。
 * 身份证只在「列表/分页」这类批量接口打码返回，详情接口（编辑用）保持完整。
 */
public final class SensitiveUtil {

    private SensitiveUtil() {}

    /** 身份证脱敏：保留前 6 后 4，中间补星号；不足 10 位则全部打码 */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.isEmpty()) {
            return idCard;
        }
        int len = idCard.length();
        if (len <= 10) {
            return "****";
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(len - 4);
    }

    /** 把会员对象复制一份并脱敏身份证（避免污染原始对象，编辑时仍能用完整值） */
    public static Member maskMember(Member src) {
        if (src == null) {
            return null;
        }
        Member copy = new Member();
        copy.setMemberId(src.getMemberId());
        copy.setMemberName(src.getMemberName());
        copy.setPhone(src.getPhone());
        copy.setGender(src.getGender());
        copy.setIdCard(maskIdCard(src.getIdCard()));
        copy.setRegisterTime(src.getRegisterTime());
        copy.setExpireTime(src.getExpireTime());
        copy.setMemberStatus(src.getMemberStatus());
        copy.setRemark(src.getRemark());
        return copy;
    }

    /** 批量脱敏列表 */
    public static List<Member> maskMembers(List<Member> list) {
        if (list == null) {
            return null;
        }
        return list.stream().map(SensitiveUtil::maskMember).toList();
    }
}
