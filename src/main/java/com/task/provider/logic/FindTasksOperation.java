package com.task.provider.logic;

import com.task.provider.model.Sort;
import com.task.provider.model.Status;
import com.task.provider.model.Task;
import com.task.provider.service.dao.R2dbcAdapter;
import io.r2dbc.client.Query;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.StringJoiner;

import static com.task.provider.utils.Utils.logProcessFlux;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.joining;

@Component
@RequiredArgsConstructor
public class FindTasksOperation {

    private final static Logger log = LoggerFactory.getLogger(FindTasksOperation.class);

    private final R2dbcAdapter r2dbcAdapter;

    public Flux<Task> process(FindTasksRequest request) {
        return r2dbcAdapter.findList(request)
            .as(logProcessFlux(log, "FindTasksOperation", request));
    }

    @ToString
    public static class FindTasksRequest {

        public final Status status;
        public final Long assignee;
        public final Long createdBy;
        public final List<Sort> sort;
        public final Long offset;
        public final Long limit;

        public FindTasksRequest(Status status, Long assignee, Long createdBy, List<Sort> sort, Long offset, Long limit) {
            this.status = status;
            this.assignee = assignee;
            this.createdBy = createdBy;
            this.sort = sort;
            this.offset = requireNonNullElse(offset, 0L);
            this.limit = requireNonNullElse(limit, 20L);
        }

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
            params.add(" WHERE id > " + offset.toString());

            if (nonNull(status)) {
                params.add(format("status = $%d::task_status", pos++));
            }
            if (nonNull(assignee)) {
                params.add(format("assignee = $%d", pos++));
            }
            if (nonNull(createdBy)) {
                params.add(format("created_by = $%d", pos));
            }

            return params.toString()
                .concat(format(" FETCH FIRST %d ROWS ONLY", limit));
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
