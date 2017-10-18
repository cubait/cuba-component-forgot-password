-- begin NXSECFP_RESET_PASSWORD_TOKEN
create table NXSECFP_RESET_PASSWORD_TOKEN (
    ID uuid,
    --
    USER_ID uuid not null,
    TOKEN varchar(64) not null,
    EXPIRE_AT timestamp not null,
    --
    primary key (ID)
)^
-- end NXSECFP_RESET_PASSWORD_TOKEN
