-- begin NXSECFP_RESET_PASSWORD_TOKEN
create table NXSECFP_RESET_PASSWORD_TOKEN (
    ID uniqueidentifier,
    --
    USER_ID uniqueidentifier not null,
    TOKEN varchar(64) not null,
    EXPIRE_AT datetime2 not null,
    --
    primary key nonclustered (ID)
)^
-- end NXSECFP_RESET_PASSWORD_TOKEN
