-- begin NXSECFP_RESET_PASSWORD_TOKEN
create table NXSECFP_RESET_PASSWORD_TOKEN (
    ID varchar(36) not null,
    --
    USER_ID varchar(36) not null,
    TOKEN varchar(64) not null,
    EXPIRE_AT timestamp not null,
    --
    primary key (ID)
)^
-- end NXSECFP_RESET_PASSWORD_TOKEN
