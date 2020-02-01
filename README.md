# bb4-adventure

A configurable story builder. Stories are configured using xml script files that reside in 
com/barryecker4/puzzle/adventure/stories.

Currently there is a Ludlow adventure based on a scenario from an old Dragon magazine, 
and a learnBinary tutorial that demonstrates that the framework can be used for things other than adventure gaming. 

This project started as a framework to create adventure games (hence the name), but then I realized it could have many other applications. It can be used to deliver any interactive story or tutorial. Furthermore, it could be used to edit any arbitrary graph or hierarchy such as the data for [aikido application builder]().


### [How to Build](https://github.com/barrybecker4/bb4-common/wiki/Building-bb4-Projects)

If you have not already done so, first install [Git](http://git-scm.com/), and [Intellij](http://www.jetbrains.com/idea/).

Type 'gradlew build run' at the root (or ./gradlew if running in Cygwin). This will start the adventure game.
If you want to open the source in Intellij, then first run 'gradle idea'.

When there is a new release, versioned artifacts will be published by Barry Becker to [Sonatype](https://oss.sonatype.org).

### License
All source (unless otherwise specified in individual file) is provided under the [MIT License](http://www.opensource.org/licenses/MIT)






