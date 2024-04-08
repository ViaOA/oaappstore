

jar:file:/c:ajar.jar!/MyMap.jar


help.hs - defines help information

map.jhm - maps names (targets) to urls

index.xml - maps words to targets

toc.xml - table of contents, mapped to targets


- Create html pages to use

- deploy with jh.jar file

- C:\projects\java\jh2.0\javahelp\bin\jhindexer - used to generate search index

** Build/generate index files
cd \projects\java\DispatcherLG\src\com\oldcastle\dispatcher\help
C:\projects\java\jh2.0\javahelp\bin\jhindexer *.html


Specify the top-level folders as arguments to the jhindexer command, as follows:
jhindexer dir1 dir2 dir3

- creating/jar - put all files into a jar file

- Viewer - to view/run a help file.  Double click using Windows Explorer.
it will run c:\projects\java\hsviewer.bat.

hsviewer.bat
java -jar C:\projects\java\jh2.0\demos\bin\hsviewer.jar -helpset %1

