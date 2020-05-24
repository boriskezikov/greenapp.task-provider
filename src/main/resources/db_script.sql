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
    title       varchar,
    description varchar,
    status      public.task_status NOT NULL,
    coordinate  point              NOT NULL,
    reward      BIGINT             NOT NULL,
    assignee    BIGINT             NOT NULL,
    type        public.task_type   NOT NULL,
    due_date    timestamp,
    updated     timestamp,
    created_by  varchar            NOT NULL,
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