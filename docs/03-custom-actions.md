# Custom actions

The more information you can give Authbox about what the user is doing, the better we can make predictions about whether the user is a good or bad actor.

## Logging Actions using `log()`

The first step is by explicitly sending actions. To do this, simply add a `log()` call to any endpoint or user action that you are interested in and pass along the relevant data. Send as little or as much data as you feel comfortable with.

```javascript

// Inside of your servlet
public void doPost(HttpServletRequest httpRequest,
                   HttpServletResponse response)
    throws ServletException, IOException
{
    AuthboxHttpServletRequestWrapper request = (AuthboxHttpServletRequestWrapper)httpRequest;
    JsonObject actionData = new JsonObject();

    // ... populate actionData ...

    request.log(actionData);
}
```

## Reserved Keywords `($)` and Type Tags `(__type_tag)`

Authbox uses `$` to indicate special keywords that have semantic meaning and get processed specially in our system. This is not some kind of subliminal attempt to get you to pay us, though you are welcome to do so if you feel so inspired.

Here is a non-exhaustive list of reserved keywords that Authbox recognizes out of the box. As time goes on, we will be adding to this list.

```javascript
{
  $apiKey: 'API Key to interact with Authbox',
  $secretKey: 'Secret Key to interact with Authbox',
  $user: AuthboxUserType,
  $session: AuthboxSessionType
  $timestamp: 'Timestamp of the current action',
  // Reserved Keywords Set By Express Middleware
  $localMachineID: {
    $key: 'Cookie to identify the current machine',
    $new: 'Boolean indicating whether we created the cookie for this request'
  },
  $ipAddress: 'IP Address that the action is coming from',
  $userAgent: 'User Agent of the user',
  // Reserved Keywords Set On The Action
  $actionName: 'Name of the action being passed in. See actions section for list of known actions',
  // This should be used if a piece of content is being manually marked as good or bad.
  $manuallyMarkedContentID: {
    $contentID: 'ID of the piece of content being marked.'
    $isGood: 'Boolean indicating whether the content is being marked as good or bad',
    $isKnownTrustedMarking: 'Boolean indicating if the current user making the marking is known to be trusted. This could be an admin, staffer, or community moderator.'
  },
  // Similar to the manuallyMarkedContentID above, this should be used if a user is being manually marked as good or bad.
  $manuallyMarkedUser: {
    $user: AuthboxUserType,
    $isGood: 'Boolean indicating whether the content is being marked as good or bad',
    $isKnownTrustedMarking: 'Boolean indicating if the current user making the marking is known to be trusted. This could be an admin, staffer, or community moderator.'
  },
  // NOTE: For both $targetContentID and $targetUser the difference here is that the action
  // targeting the user or content is not an abuse or non-abuse report. These should be used
  // when the action is something like adding a friend/contact, commenting on a post, following a user, etc.
  $targetContentID: 'ID of a piece of content',
  $targetUser: AuthboxUserType
}
```

Not all fields require the '$' prefix. In fact, the majority of data customers send us is custom data and is NOT prefixed with '$'. In order for us to be able to parse this data and run it through the appropriate classifiers and models, you can annotate these with `Type Tags`.

## Using Type Tags

`Type Tags` allow you to provide custom data and help us determine how to process it. Type Tags are indicated by adding a suffix with two leading underscores. Here's an example:

```javascript
{
  message_subject__text: "New information",
  message_body__text: "Here's the content of the message"
}
```

In the above example the message_subject and message_body fields had the `__text` Type Tag. This indicates to Authbox that these fields contain user generated content and should be processed as such.

Here's another example using a custom $actionName. Let's say your site wants to add a check for a user creating a custom alert that has a title and text body field. You could do this by creating a custom action and data on that action like so:

```javascript
JsonObject data = new JsonObject();
data.addProperty("$actionName", "create_custom_alert");
data.addProperty("alert_title__text", "Title of the Alert");
data.addProperty("alert_body__text", "Body of the Custom Alert");
request.log(data);
```

Note that both the `alert_title__text` and `alert_body__text` are not prefixed with a '$' and both have the *'__text'* suffix on the key. The presence of '__text' indicates to Authbox that user generated content is being provided and it will be parsed properly. For a list of valid type tags see below:

## Valid Type Tags

### __text
Used to indicate user generated content. Fields tagged with this type will be parsed for abusive URLs, phone numbers, and content.
Example:
```javascript
{
  ...
  title__text: ‘whatever’,
  body__text: ‘blah’
};
```

### __address
Used to indicate a given value should be explicitly processed as an address. Keys with the address Tag Type consist of an object with several special reserved keywords.
Example:
```javascript
{
  ...
  billing_addr__address: {
    $name: 'First and Last Name',
    $address1: 'address1',
    $address2: 'address2',
    $country: 'country',
    $postalCode: 'postalCode',
    $region: 'region',
    $apartmentNumber: 'apartmentNumber'
  }
};
```
The reserved key fields are optional and you can provide custom fields that are relevant to your addresses if you so desire.

### __file
Used to indicate a given value should be explicitly processed as a file. This allows us to scan against file hash blacklists, detect bad images, and fight malware distribution.
Example:
```javascript
{
  ...
  attachment__file: {
    $filename: 'filename',
    $fileURL: 'fileURL',
    $fileBytes: 'fileBytes',
    $fileSHA256: 'fileSHA256',
    $fileType: '$generic | $photo | $video | $audio'
  }
};
```

### __phone
Used to indicate a given value should be explicitly processed as a phone number.
Example:
```javascript
{
  ...
  billing__phone: '1-212-111-1111',  // Delimited phone number
  shipping__phone: '12121111111'     // Note that we support both forms
  }
};
```

### __email
Used to indicate a given value should be explicitly processed as an email address.
Example:
```javascript
{
  ...
  contact__email: 'ninjaAssassin@authbox.io'
};
```

### __setting_change
Used to indicate a given value should be explicitly processed as a setting being changed.
Example:
```javascript
{
  ...
  secondary_email__setting_change: {
    $settingName: 'Name of the setting being changed',
    $settingNewValue: 'New value of the setting if present / applicable',
    $settingOldValue: 'Old value of the setting if present / applicable',
    $isRiskySetting: 'Boolean indicating if this is a high risk setting (password, secondary phone, secondary email, etc)'
  }
};
```
