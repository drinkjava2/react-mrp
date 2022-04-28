package com.reactmrp.entity;

import com.github.drinkjava2.jdialects.annotation.jdia.COLUMN;
import com.github.drinkjava2.jdialects.annotation.jdia.SingleFKey;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveEntity;

public class RoleAction implements ActiveEntity<RoleAction> {
    @Id
    @SingleFKey(refs = {"roles", "roleName"})
    private String roleName;

    @Id
    @SingleFKey(refs = {"powers", "powerName"})
    @COLUMN(length = 32)
    private String powerName;

    public String getRoleName() {
        return roleName;
    }

    public RoleAction setRoleName(String roleName) {
        this.roleName = roleName;
        return this;
    }

    public String getPowerName() {
        return powerName;
    }

    public RoleAction setPowerName(String powerName) {
        this.powerName = powerName;
        return this;
    }

}
