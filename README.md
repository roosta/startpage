# Roosta's Startpage

Startpage displaying Reddit frontpage feed, org todo file and a ascii art clock in center.
Both feeds are clickable and updates every minute. When clicking on a todo item emacsclient is called, opens file and jumps to location.
Currently expects an emacs server to be running one way or another. (why wouldn't it be, right?)

## Installation

Requires:
* [Leiningen](https://github.com/technomancy/leiningen)
* [Node](https://nodejs.org/en/)

```shell
$ git clone https://github.com/roosta/startpage
$ cd startpage
$ script/build
```

## Configuration
This startpage has a configuration file named ```config.edn```
To get started use the example config:
```shell
cat config.example.edn > config.edn
```

Config currently only has two fields
### :reddit-feed
contains the link to a reddit json feed, you can get this by going to reddit.com and going to **preferences -> RSS feeds -> private listings, frontpage -> JSON** copy that url and paste in config

### :org-file
Physical location to an org file. Currently only supports absolute paths so it would be something like
```edn
:org-file "/home/[user]/TODOs.org"
```
App expects an org-mode todo file and only renders level one nodes. Example:
```
* TODO I'm a todo item
** IGNORED Level two nodes are not rendered
* DONE I'm a done todo item
```
## Usage

```shell
$ script/run
```

Point your browser to http://localhost:3000

## License

Copyright © 2017 Daniel Berg

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
