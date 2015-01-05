# authbox-java Quickstart

The most basic level of Authbox integration gets you high-level detection of suspicious activity. You can get integrated in a matter of minutes.

## Installation

Add `authbox-java-1.0-SNAPSHOT.jar` to your project and add the following to your `web.xml` file:

```xml
<filter>
  <filter-name>AuthboxFilter</filter-name>
  <filter-class>io.authbox.api.AuthboxFilter</filter-class>
</filter>
```

## Configure Authbox

Set the `authbox.apiKey` and `authbox.secretKey` system properties. You can either do this by calling `System.setProperty()`, passing `-D` flags to the `java` VM or via your IDE.

If that's all you feel like doing then that's it! You have now enabled Authbox and are tapping into our abuse prevention services. You rock. But the real fun is just getting started. Read [part two](./02-users.md) to learn about how to send us information about your users.