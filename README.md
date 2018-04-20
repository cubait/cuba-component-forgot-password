[![license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![Semver](http://img.shields.io/SemVer/2.0.0.png)](http://semver.org/spec/v2.0.0.html)
[![Generic badge](https://img.shields.io/badge/API%20docs-HERE-orange.svg)][2]
[![Run in Postman](https://run.pstmn.io/button.svg)][1]

# CUBA Forgot Password Component

This application component gives the following features once added to a CUBA project:

- Enables displaying an optional _Forgot password_ link in the main login window, allowing users to send 
themselves an email with a reset password link
- Enables logging in by both login name and email (this works also when using REST API). **PLEASE NOTE**
that requires a `UNIQUE` constraint on the `email` attribute of the `User` entity (simply put, users'
emails must be unique in the system)
- Exposes a new REST service (_extsec_UserManagementService_) that enables REST clients to use the
forgot password functionality via API calls
- Adds an _allowAnonymous_ boolean attribute to the _rest-services.xml_ file, allowing for only some
methods to be called without an authorization token via REST API (v2). This is the mechanism that
allows the _extsec_UserManagementService_ to be used before authentication, but can be leveraged by
custom services too


## Installation

1. Add the following maven repository `https://dl.bintray.com/pfurini/cuba-components` to the build.gradle of your CUBA application:

```
buildscript {
    
    //...
    
    repositories {
    
        // ...
    
        maven {
            url  "https://dl.bintray.com/pfurini/cuba-components"
        }
    }
    
    // ...
}
```

2. Select a version of the add-on which is compatible with the platform version used in your project:

| Platform Version | Add-on Version |
| ---------------- | -------------- |
| 6.6.4            | 0.1.x          |
| 6.8.6            | 0.2.x          |

The latest version is: `0.2.0`

Add custom application component to your project:

* Artifact group: `it.nexbit.cuba.security.forgotpassword`
* Artifact name: `nxsecfp-global`
* Version: *add-on version*

## Supported DBMS engines

Currently (as of version 0.2.0) this plugin supports the following RDBMS engines:

- HSQLDB
- PostgreSQL
- MySQL/MariaDB 10.x (thanks to Mario David)
- Microsoft SQL Server (tested against *2017-GA Express* on Ubuntu 16.04)
- Oracle 11g+ (tested against *Oracle Express Edition 11g*)

## Created tables

| Table Name | Scope |
| ---------- | ----- |
| NXSECFP_RESET_PASSWORD_TOKEN | Holds generated reset tokens, alongside the linked User entity and timestamp of expiration |

## Usage

### Using the _allowAnonymous_ attribute

The following step is required only if you already have (or plan to have) services for which you
want to leverage the extended `allowAnonymous="true"` attribute.

First, follow the normal procedure to add and register a new `rest-services.xml` file to your project.

Then open the `rest-services.xml` file and change the following line:

```xml
<services xmlns="http://schemas.haulmont.com/cuba/rest-services-v2.xsd">
```

to this line:

```xml
<services xmlns="http://schemas.haulmont.com/cuba/rest-services-v2-ext.xsd">
```

This will allow editing the xml file inside an IDE, without it complaining for an unknown attribute.

Here is a sample `rest-services.xml` file using the new attribute:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<services xmlns="http://schemas.haulmont.com/cuba/rest-services-v2-ext.xsd">
    <service name="testsec_NewService">
        <method name="someMethod" allowAnonymous="true">
            <param name="someArg"/>
        </method>
    </service>
</services>
```

### Using the Forgot Password functionality

To enable the Forgot password link, and related functionality in the main login window, follow these steps:

1. Add the following properties to your `web-app.properties` file:
```properties
# this property enables the reset password link in login window
ext.security.showResetPasswordLinkAtLogin = true

# you must include the "reset" link handler action to be able to open the change pass dialog via custom link
cuba.web.linkHandlerActions = open|o|reset
```

2. Optionally, create custom reset link emails and set the corresponding paths in your `app.properties` file,
located in the `core` module:
```properties
# create customized email templates, and set the full path in these properties
ext.security.resetPasswordLinkTemplateBody = /it/nexbit/cuba/security/forgotpassword/app/email/reset-password-link-body.gsp
ext.security.resetPasswordLinkTemplateSubject = /it/nexbit/cuba/security/forgotpassword/app/email/reset-password-link-subject.gsp
```
You can find built-in templates here: [Default email templates](https://github.com/pfurini/cuba-component-forgot-password/tree/master/modules/global/src/it/nexbit/cuba/security/forgotpassword/app/email).
Remember that they use the same mechanism described here in the official docs: https://doc.cuba-platform.com/manual-6.6/users.html, so
they can be localized adding a locale suffix, and they can also be located or overridden in the tomcat configuration directory, with 
the properties added to the `local.app.properties` file.

#### Properties
The component also exposes the following DATABASE properties, that you can configure in your running app:

| Property                                       | Default Value                       | Description                                              |
| ---------------------------------------------- | ----------------------------------- | -------------------------------------------------------- |
| ext.security.resetPasswordLinkWebPath          | /reset                              | The path appended to your web application URL when the token is generated from the standard login window (**WARNING**: do not modify this value unless you are extending or changing the component behavior) |
| ext.security.resetPasswordLinkPortalUrl        | http://localhost:8080/portal/reset  | You **should** change this property if you plan to use the forgot password functionality from REST clients. This endpoint must be configured using your JS framework of choice, and it is beyond the scope of this documentation |
| ext.security.resetPasswordTokenLifetimeMinutes | 1440                                | The lifetime of a newly generated reset token (default 24 hours) |

#### extsec_UserManagementService Methods

The following are the methods exposed by the `extsec_UserManagementService`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<services xmlns="http://schemas.haulmont.com/cuba/rest-services-v2-ext.xsd">
    <service name="extsec_UserManagementService">
        <method name="checkUserExist" allowAnonymous="true">
            <param name="loginOrEmail"/>
        </method>
        <method name="sendResetPasswordLink" allowAnonymous="true">
            <param name="loginOrEmail"/>
        </method>
        <method name="checkResetPasswordToken" allowAnonymous="true">
            <param name="token"/>
        </method>
        <method name="changePasswordWithToken" allowAnonymous="true">
            <param name="token"/>
            <param name="password"/>
        </method>
        <method name="deleteResetPasswordToken" allowAnonymous="true">
            <param name="token"/>
        </method>
    </service>
</services>
```

Consult the online documentation at [this link][2],
or the JavaDoc documentation on the interface methods for explanation of their usage and parameters.

You can use the following button to open a collection of requests in the [Postman](https://www.getpostman.com/) application:

[![Run in Postman](https://run.pstmn.io/button.svg)][1]

#### Usage Notes

The methods exposed to REST clients are not all the methods available on the `NexbitUserManagementService` interface,
but only the _safest_ ones.

Feel free to use the other ones in your own client and middleware code, for example if you want to implement
a new management functionality to bulk send reset passwords to a group of users.

This is specially useful, because the methods on that interface allow to explicitly set the `baseUrl` to use when
generating reset links, and so you could let your power user (admin) to choose where end users will be redirected
when clicking the link (if you have multiple apps for multiple groups of users).

In this regard, the component use a couple of defaults when choosing which `baseUrl` to use when generating reset links:

- if the user click the _Forgot password_ link on the main (vaadin) login window, it will use the URL obtained by
concatenating the values from `GlobalConfig#getWebAppUrl()` and the `ext.security.resetPasswordLinkWebPath` property
- if the request comes from the `sendResetPasswordLink` REST service method, it will use the URL in the `ext.security.resetPasswordLinkPortalUrl`
property

In both cases, it will append a `token=<TOKEN_VALUE>` query string to the final URL.

## Known Issues

See the corresponding issue to find if a workaround is currently available.

#### [[#2] allowAnonymous does not work for POST requests](https://github.com/pfurini/cuba-component-forgot-password/issues/2)

[1]: https://app.getpostman.com/run-collection/f7b921d260a173059894#?env%5Bsec-forgot-password%20TEST%5D=W3sia2V5IjoiYmFzZXVybCIsInZhbHVlIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwL2FwcC9yZXN0IiwiZW5hYmxlZCI6dHJ1ZSwidHlwZSI6InRleHQifV0=
[2]: https://documenter.getpostman.com/view/48162/sec-forgot-password-cuba/RW1Vr2Zh