# psssst

A command line application to keep your team abreast of open pull requests that need review. Currently communicates only via slack.

## Building

Psssst uses [lein-binplus](https://github.com/BrunoBonacci/lein-binplus) to create a self contained
executable.

After cloning the project, run `lein bin` to build the executable to `target`. The executable will be named `psssst`.

## Usage

```
$ psssst -h
Pssst polls for pull requests in need of review.

Usage: psssst [options]

Options:
  -c, --config FILE  /Users/brian/projects/psssst/psssst.clj  psssst config file
  -h, --help

See https://github.com/brianium/psssst for more information.
```

### Configuration

By default, `psssst` will look for a configuration file in the current working directory named `psssst.clj`. Alternatively, a configuration file can be passed via the `--config` option.

Configuration files only need a single `hash-map`.

```clojure
{:github-token "github-token"
 :slack-token "slack-token"
 :interval 3600
 :org "netrivet"
 :users {"brianium" "brian"
         "jaredh159" "jared"
         "meatwad5675" "matt"}
 :username "Psssst Bot"
 :icon_emoji ":robot_face"}
```

The only required keys are `:github-token`, `:slack-token`, `:org`, and `:users`.

Configuration keys are used as follows:

**github-token** - This is a token that authorizes `psssst` to poll github for pull requests that do not have an `assignee`.

**slack-token** - This is a token that authorizes `psssst` to send messages via slack.

**interval** - This tells `psssst` how often to check for pull requests and send messages. Intervals are given as seconds. A larger number is encouraged here for the sake of your team's sanity. Defaults to `3600` or every hour.

**org** - The organization to poll for open pull requests.

**users** - A map mapping github user names to slack user names. The key will always be the github user name, and the value will be the slack user name. This is used to send messages, as well as exclude the pull request author from receiving their own pull request messages.

**username** - This is the user name that slack messages will be sent as. Defaults to `Psssst`.

**icon_emoji** - This is a slack emoji of the form `:emoji_name` that will be used when a message is sent. Defaults to `:robot_face`.

## How It Works

`psssst` is ideally run in the background. It checks github on a configured interval for pull requests that have no `assignee`. If any pull requests meet this criteria, the group of configured users will be slack messaged with links to those pull requests. Pull request authors are excluded from receiving messages about pull requests they have authored.

The point of the tool is to encourage members of a team to assign themselves to pull requests. This tool encourages peer review without showing down each other's throats (provided your `:interval` setting is high enough :P).

## Testing

Tests can be run using `lein test`. During development, `lein test-refresh` can be used to run tests
as changes as are made.

## A Note On Slack

Slack's usage states that *spammy* applications may be rate limited if more than 1 message is sent within a second, but short bursts are acceptable. This tool was built for a small team of 3 people. It has not been tested for larger organizations, and I suspect more users will result in more spammy flags being raised.

Since this tool is meant to be an infrequent reminder, it seems ok and trivial to stagger the rate at which messages are sent if a larger organization needs to make use of such a tool.

## Contributing

Pull requests and issues welcome :)

## License

Copyright © 2017 Brian Scaturro

Distributed under the Eclipse Public License
