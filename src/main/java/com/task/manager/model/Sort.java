package com.task.manager.model;

public enum Sort {

    REWARD_DESC("reward desc"),
    CREATED_DESC("created desc"),
    DUE_DATE_DESC("due_date desc"),
    REWARD("reward"),
    CREATED("created"),
    DUE_DATE("due_date");

    private String value;

    Sort(String reward) {
    }
}
