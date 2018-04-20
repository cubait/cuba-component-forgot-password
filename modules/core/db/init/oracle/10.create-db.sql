-- begin NXSECFP_RESET_PASSWORD_TOKEN
create table NXSECFP_RESET_PASSWORD_TOKEN (
    ID varchar2(32),
    --
    USER_ID varchar2(32) not null,
    TOKEN varchar2(64) not null,
    EXPIRE_AT timestamp not null,
    --
    primary key (ID)
)^
-- end NXSECFP_RESET_PASSWORD_TOKEN
