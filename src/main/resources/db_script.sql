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

insert into task(status,coordinate,reward,assignee,type,created_by) values ('WAITING_FOR_APPROVE',point(100,100), 124,123,'ANIMAL','lolitka')
insert into task(status,coordinate,reward,assignee,type,created_by) values ('CREATED',point(200,100), 124,11,'ANIMAL','sukka')
insert into task(status,coordinate,reward,assignee,type,created_by) values ('RESOLVED',point(300,13), 126,2,'PLANT','mamka')
insert into task(status,coordinate,reward,assignee,type,created_by) values ('CREATED',point(100,100), 124,34,'PEOPLE','lox')