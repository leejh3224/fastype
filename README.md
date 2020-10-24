# Fastype

CLI program to convert markdown file/string into Postype compatible html and upload it

## Getting Started

This project requires `maven` and `java`.

```sh
# install
mvn install

# build
mvn package

# exec
java -cp "/path/to/fastype/project/target/fastype-0.0.1.jar" fastype.Fastype --help
```

## Usage

### Configuration

This program read configuration from `$HOME/.config/fastype/config.yaml`.

Example yaml file

```yaml
token: "token"
blogUrl: "https://example.postype.com"
blogId: "1111111" # will be automatically set if you set `blogUrl`
```

You can create/edit this file directly or use `fastype config` command instead

```sh
fastype config --key "blogUrl" --value "https://gompro.postype.com"
```

### Auth

In order to save draft, program requires Postype authentication token (token).

There are two ways you can provide `token` to config.

```sh
fastype auth -i # interative mode (use browser)
fastype auth -t, --token "value" # pass token directly to config
``` 

### Draft

You can use this command to save draft from markdown.

Example)

```sh
# pass content from clipboard (OS X)
fastype --id [draftId] --content "$(pbpaste)" -ip /path/to/save/images --title "this is title" --subtitle "this is subtitle"

# pass content from markdown file
fastype --id [draftId] --content ./path/to/markdown/file -ip /path/to/save/images --title "this is title" --subtitle "this is subtitle"
```

## Motivation

I hate most WYSIWYG style editors regardless of its ease of use.

And as I use Notion as my primary note app, I wanted to export my markdown files easily into my blog.

So I reverse engineered Postype API and made this CLI application.

If you're interested in improving this app, please leave an issue.



