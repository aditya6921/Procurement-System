package com.procureflow.policy;

public enum ApprovalRole {

    MANAGER(1),

    PROCUREMENT(2),

    FINANCE(3);

    private final int order;

    ApprovalRole(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}