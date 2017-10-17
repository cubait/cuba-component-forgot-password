package it.nexbit.cuba.security.forgotpassword.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

import javax.annotation.Nullable;


public enum ResetPasswordLinkInfo implements EnumClass<String> {

    OK("OK"),
    KO_NOT_FOUND("KO_NOT_FOUND"),
    KO_MISSING_EMAIL("KO_MISSING_EMAIL"),
    KO_NOT_ACTIVE("KO_NOT_ACTIVE");

    private String id;

    ResetPasswordLinkInfo(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static ResetPasswordLinkInfo fromId(String id) {
        for (ResetPasswordLinkInfo at : ResetPasswordLinkInfo.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}