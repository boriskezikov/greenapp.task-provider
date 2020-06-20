CREATE TYPE task_status AS ENUM (
    'TRASHED',
    'CREATED',
    'WAITING_FOR_APPROVE',
    'APPROVED',
    'TO_DO',
    'IN_PROGRESS',
    'RESOLVED',
    'COMPLETED'
    );

CREATE TYPE task_type AS ENUM (
    'ANIMAL',
    'PEOPLE',
    'ENVIRONMENT',
    'PLANT',
    'URBAN',
    'OTHER'
    );

CREATE TABLE public.task
(
    id          SERIAL PRIMARY KEY,
    title       VARCHAR,
    description VARCHAR,
    status      public.task_status NOT NULL,
    coordinate  point              NOT NULL,
    reward      BIGINT             NOT NULL,
    assignee    BIGINT,
    type        public.task_type   NOT NULL,
    due_date    timestamp,
    updated     timestamp,
    created_by  BIGINT             NOT NULL,
    created     timestamp          NOT NULL DEFAULT now()
);

CREATE TABLE public.attachment
(
    id      SERIAL PRIMARY KEY,
    task_id BIGINT REFERENCES public.task (id) ON DELETE CASCADE,
    content BYTEA,
    type    VARCHAR,
    length  BIGINT
);

CREATE INDEX ON public.task (status, assignee, created_by);
CREATE INDEX ON public.task (assignee, created_by);
CREATE INDEX ON public.task (created_by);

/* For test population */
CREATE OR REPLACE FUNCTION populate_tasks(n INTEGER) RETURNS VOID
    LANGUAGE plpgsql
AS
$$
DECLARE
    counter INTEGER := 0;
BEGIN
    LOOP
        EXIT WHEN counter = n;
        counter := counter + 1;
        INSERT INTO public.task (title, description, status, coordinate, reward, assignee, type, due_date, created_by)
        VALUES (CONCAT('Task ', counter),
                CONCAT('Some description of Task ', counter),
                'CREATED',
                point(random() * 261 - 180, random() * 181 - 90),
                counter * 100,
                floor(random() * 1001),
                (SELECT t FROM unnest(enum_range(NULL::task_type)) t ORDER BY random() LIMIT 1),
                (SELECT * FROM generate_series('2020-08-01'::timestamp, '2021-10-01'::timestamp, '1 day'::interval) LIMIT 1),
                floor(random() * 1001));
    END LOOP;
END;
$$;