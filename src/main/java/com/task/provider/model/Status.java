package com.task.provider.model;

import static java.lang.String.format;

public enum Status {

    TRASHED(100),
    CREATED(1),
    WAITING_FOR_APPROVE(2),
    APPROVED(3),
    TO_DO(4),
    IN_PROGRESS(4),
    RESOLVED(6),
    COMPLETED(7);

    public final int value;

    Status(int i) {
        this.value = i;
    }

    public void validateOver(Status s) {
        if (this.value < s.value) {
            throw new IllegalStateException(format("It's not available to change status from %s to %s", this, s));
        }
    }
}
