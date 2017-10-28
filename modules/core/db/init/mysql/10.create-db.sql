-- begin NXSECFP_RESET_PASSWORD_TOKEN
create table NXSECFP_RESET_PASSWORD_TOKEN (
    ID varchar(32),
    --
    USER_ID varchar(32) not null,
    TOKEN varchar(64) not null,
    EXPIRE_AT datetime(3) not null,
    --
    primary key (ID)
)^
-- end NXSECFP_RESET_PASSWORD_TOKEN
