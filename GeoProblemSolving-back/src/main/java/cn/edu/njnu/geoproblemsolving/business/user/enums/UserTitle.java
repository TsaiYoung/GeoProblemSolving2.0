package cn.edu.njnu.geoproblemsolving.business.user.enums;

import lombok.Getter;

@Getter

public enum UserTitle {
    Professor("Professor"), Dr("Dr"), Mr("Mr"), Ms("Ms"), Miss("Miss"), Mrs("Mrs"), Mx("Mx");

    private String desc;

    UserTitle(String desc) {
        this.desc = desc;
    }
}
