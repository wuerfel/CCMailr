# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table mbox (
  id                        bigint not null,
  adress                    varchar(255),
  ts_active                 bigint,
  expired                   boolean,
  domain                    varchar(255),
  usr_id                    bigint,
  constraint pk_mbox primary key (id))
;

create table user (
  id                        bigint not null,
  forename                  varchar(255),
  surname                   varchar(255),
  mail                      varchar(255),
  passwd                    varchar(255),
  admin                     boolean,
  constraint pk_user primary key (id))
;

create sequence mbox_seq;

create sequence user_seq;

alter table mbox add constraint fk_mbox_usr_1 foreign key (usr_id) references user (id) on delete restrict on update restrict;
create index ix_mbox_usr_1 on mbox (usr_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists mbox;

drop table if exists user;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists mbox_seq;

drop sequence if exists user_seq;

