package com.task.provider.logic;

import com.task.provider.model.Sort;
import com.task.provider.model.Status;
import com.task.provider.model.Task;
import com.task.provider.service.dao.R2dbcAdapter;
import io.r2dbc.client.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.StringJoiner;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;

@Component
@RequiredArgsConstructor
public class FindTasksOperation {

    private final R2dbcAdapter r2dbcAdapter;

    public Flux<Task> process(FindTasksRequest request) {
        return r2dbcAdapter.findList(request);
    }

    @RequiredArgsConstructor
    public static class FindTasksRequest {

        public final Status status;
        public final Long assignee;
        public final Long createdBy;
        public final List<Sort> sort;

        public Query bindOn(Query query) {
            var pos = 1;
            if (nonNull(status)) {
                query.bind(format("$%s", pos++), status.toString());
            }
            if (nonNull(assignee)) {
                query.bind(format("$%s", pos++), assignee);
            }
            if (nonNull(createdBy)) {
                query.bind(format("$%s", pos), createdBy);
            }
            return query;
        }

        public String appendSqlOver(String sql) {
            return sql.concat(where()).concat(orderBy());
        }

        private String where() {
            var pos = 1;
            var params = new StringJoiner(" AND ");

            if (nonNull(status)) {
                params.add(format("status = $%d::task_status", pos++));
            }
            if (nonNull(assignee)) {
                params.add(format("assignee = $%d", pos++));
            }
            if (nonNull(createdBy)) {
                params.add(format("created_by = $%d", pos));
            }

            if (params.length() != 0) {
                return " WHERE " .concat(params.toString());
            }
            return "";
        }

        private String orderBy() {
            if (isNull(sort) || sort.isEmpty()) {
                return "";
            }
            return " ORDER BY "
                .concat(sort.stream()
                            .map(Enum::toString)
                            .collect(joining(", "))
                );
        }
    }
}
