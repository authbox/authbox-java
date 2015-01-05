# Sending user data

Authbox gets better with more data. You can send some of the most important data with `getRequestData()`.

## Configure Authbox Using `getRequestData`

Two additional pieces of data that greatly increase the efficacy of Authbox are the current user and current session. You do this by creating an `AuthboxRequestDataProvider`:

```java

import com.google.gson.*;
import io.authbox.api.AuthboxRequestDataProvider;
import java.security.Principal;
import javax.servlet.http.HttpServletRequest;

public class MyAuthboxRequestDataProvider implements AuthboxRequestDataProvider {
  @Override
  public JsonObject getRequestData(HttpServletRequest request) {
    JsonObject data = new JsonObject();

    // Return some information about the request.

    Principal user = request.getUserPrincipal();

    if (user == null) {
      // user is not logged in
      return data;
    }

    // Tell Authbox who the currently logged in user is
    JsonArray userIDs = new JsonArray();
    JsonObject userID = new JsonObject();

    // Add any more information about the user here.
    userID.addProperty("$type", "username");
    userID.addProperty("$key", user.getName());
    userIDs.add(userID);
    data.add("$user", userIDs);

    // Tell Authbox about the current session
    JsonObject session = new JsonObject();
    session.addProperty("$sessionID", request.getRequestedSessionId());
    data.add("$session", session);

    return data;
  }
}
```

### Users in Authbox

Providing the signed in user is possibly the single highest-value signal you can provide. A sample Authbox user looks like the following:

```javascript
 $user : {
   // Array of userIDs that consist of a type and a key.
   $userIDs: [
     {$type:'$email', $key: 'bingo@authbox.io'},
     {$type:'$phone', $key: '12121112222'},
     ...
   ],
   // OPTIONAL: Account creation time (unix timestamp)
   $creationTime: 1369114061000
 };
```

 As you might have guessed from the above snippet, Authbox User objects consist of two special components: `$userIDs` and `$creationTime`. You can provide any additional information you want on the user, but $userIDs and $creationTime are the most important. $userIDs is an array of user identifiers that consist of a `$type` and a `$key`. The key is the actual value while the type can be one of the following:

  * $email
  * $phone
  * $username
  * $facebook
  * $twitter
  * $google
  * $github
  * $stripe
  * $opaqueID

In summary, an Authbox User consists of an array of userIDs (type and key) and an optional creationTime.

  * **$key**: the user ID (either an email address, phone number, username, or opaque identifier)
  * **$type**: either  `$email`, `$phone`, `$username`, `$facebook`, `$twitter`, `$google`, `$github`, `$stripe`, OR `$opaqueID`
  * **$creationTime**: optional: unix timestamp of when the account was created

## Sessions in Authbox

Sessions in Authbox simply consist of a `$sessionID` and an optional `$creationTime`. A sample session looks like this:
```javascript
$session : {
  $sessionID: 'session id',
  $creationTime : 1369114061000
};
```

## Next steps

Giving Authbox information about specific actions users are performing makes it much, much more effective. See the [the next section](./03-custom-actions.md) for information about this level of integration.
