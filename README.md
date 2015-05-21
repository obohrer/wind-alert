# wind

Example of a clj webapp/notifier to find windy spots.

## Usage

```
lein run 8080
```

Open your browser at 8080 and wait for measures to be fetched.

Set up your mailgun api-key, from & recipients fields inside ```resources/wind.json``` if you want to enable notifications.

This example use a private API so access to wind data could stop working at any time.
