package com.task.manager.logic;

import com.task.manager.model.Sort;
import com.task.manager.model.Task;
import com.task.manager.service.R2dbcAdapter;
import io.r2dbc.client.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.StringJoiner;

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

        public final String status;
        public final Long assignee;
        public final Long createdBy;
        public final List<Sort> sort;

        public Query bindOn(Query query) {
            if (nonNull(status)) {
                query.bind("$1", status);
            }
            if (nonNull(assignee)) {
                query.bind("$2", assignee);
            }
            if (nonNull(createdBy)) {
                query.bind("$3", createdBy);
            }
            return query;
        }

        public String appendSqlOver(String sql) {
            return sql.concat(where()).concat(orderBy());
        }

        private String where() {
            var params = new StringJoiner(", ");

            if (nonNull(status)) {
                params.add("status = $1");
            }
            if (nonNull(assignee)) {
                params.add("assignee = $2");
            }
            if (nonNull(createdBy)) {
                params.add("created_by = $3");
            }

            if (params.length() != 0) {
                return " WHERE ".concat(params.toString());
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
