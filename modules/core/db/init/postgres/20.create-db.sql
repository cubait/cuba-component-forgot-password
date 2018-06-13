-- begin NXSECFP_RESET_PASSWORD_TOKEN
alter table NXSECFP_RESET_PASSWORD_TOKEN add constraint FK_NXSECFP_RESET_PASSWORD_TOKEN_ON_USER foreign key (USER_ID) references SEC_USER(ID)^
create unique index IDX_NXSECFP_RESET_PASSWORD_TOKEN_UK_USER_ID on NXSECFP_RESET_PASSWORD_TOKEN (USER_ID) ^
create index IDX_NXSECFP_RESET_PASSWORD_TOKEN_ON_USER on NXSECFP_RESET_PASSWORD_TOKEN (USER_ID)^
-- end NXSECFP_RESET_PASSWORD_TOKEN
