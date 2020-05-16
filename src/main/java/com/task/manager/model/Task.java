package com.task.manager.model;

import io.r2dbc.spi.Row;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import javax.validation.constraints.NotNull;

@Builder
@EqualsAndHashCode(exclude = {"id", "updated", "created", "createdBy"})
@RequiredArgsConstructor
public class Task {

    @NotNull
    public final Long id;
    public final String title;
    public final String description;
    @NotNull
    public final Status status;
    @NotNull
    public final Point coordinate;
    @NotNull
    public final Type type;
    public final List<Photo> photos;
    @NotNull
    public final Long reward;
    public final Long assignee;
    public final LocalDateTime dueDate;
    public final LocalDateTime updated;
    public final String createdBy;
    public final LocalDateTime created;

    public static Task fromGetByIdRow(Row row) {
        return Task.builder()
            .id(row.get("id", Long.class))
            .title(row.get("title", String.class))
            .description(row.get("description", String.class))
            .status(Status.valueOf(row.get("status", String.class)))
            .coordinate(Point.fromString(row.get("coordinate", String.class)))
            .type(Type.valueOf(row.get("type", String.class)))
            .reward(row.get("reward", Long.class))
            .assignee(row.get("assignee", Long.class))
            .dueDate(row.get("due_date", LocalDateTime.class))
            .updated(row.get("updated", LocalDateTime.class))
            .createdBy(row.get("created_by", String.class))
            .created(row.get("created", LocalDateTime.class))
            .build();
    }

    public static Task fromFindRow(Row row) {
        return Task.builder()
            .id(row.get("id", Long.class))
            .title(row.get("title", String.class))
            .status(Status.valueOf(row.get("status", String.class)))
            .coordinate(Point.fromString(row.get("coordinate", String.class)))
            .type(Type.valueOf(row.get("type", String.class)))
            .reward(row.get("reward", Long.class))
            .dueDate(row.get("due_date", LocalDateTime.class))
            .build();
    }

    @RequiredArgsConstructor
    public static class Photo {

        private final Long id;
        private final Long taskId;
        private final byte[] content;
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    public static class Point {

        public final float longitude;
        public final float latitude;

        public static Point fromString(String s) {
            s = s.replaceAll("[()]", "");
            float lo = Objects.requireNonNull(Float.valueOf(s.substring(0, s.indexOf(","))), "Null parsed as longitude");
            float la = Objects.requireNonNull(Float.valueOf(s.substring(s.indexOf(",") + 1)), "Null parsed as latitude");
            return new Point(lo, la);
        }

        public String toString() {
            return "(" + longitude + "," + latitude + ")";
        }
    }
}
