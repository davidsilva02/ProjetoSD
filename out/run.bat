javac -cp jars/jsoup-1.15.4.jar *.java
start cmd.exe /k "echo INICIO SERVER & java SearchModule"
start cmd.exe /k "echo INICIO CLIENT & java RMIClient"
start cmd.exe /k "echo INICIO BARREL & java IndexStorageBarrels"
start cmd.exe /k "echo INICIO DOWNLOADERS & java -cp "/jars/jsoup-1.15.4.jar" "Downloader.java""


