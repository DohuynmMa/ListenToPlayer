# ListenToPlayer
The first server plugin as a zero-based learner of Java.
It's just a simple listener plugin,which is used to listening  messages or commands players sent automatically.
It can also listen the specified words in the config.yml.
You can edit the config.yml to set your email profile or other things.
Used javax.

# Commands /ltp
 - /ltp help  Finding help.
 - /ltp mail         sending email with messages or commands collected,and clear gather.
 -           all     sending messages and commands
 -           message sending messages
 -           command sending commands
 - /ltp word  adding a listening messages word.(If the gather is empty,the plugin will listen all messages)
 - /ltp list         listing all messages or commands collected.
 -           message listing messages
 -           command listing commands
 - /ltp time            setting wether to sending email automatically on a scheduled basis or setting the sending time.It not seems to work?
 -           false      will not send email automatically.
 -           true       will send email automatically.
 -           set <int>  set sending time(h).
 - /ltp reload reload config.
 - /ltp clear        clear the messages' or commands' gather.
 -           message clear messages
 -           command clear commands

# Version
Bukkit Spigot 1.19.4

# Build
./gradlew shadowJar
gradle build
