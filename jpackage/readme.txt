

Instructions for creating OS specific installers for OAAppStore by using JDK21 JPackage command.
   note: this was taken from OABuilder project


FIRST, build OAAppStore and files:
	Before creating the installer, need to build OAAppStore and it's dependencies,
	Do this from windows Eclipse, since it has the keystore for jar signing.


Steps to follow to build OAAppStore Java App:

1: determine new versions that will be used
    oa 3.7.7
    oaappstore 0.0.1

3: create oaAppStore jar
    Set version (multiple places):
        values.properties
        pom.xml
            update oa version
            
    run mvn install 
        creates single large/"uber" jar target/oaAppStore.jar

    Note:
        need to have these, to be able to jarsign:
            c:\users\vince\keystore
            c:\users\vince\.m2\settings.xml 
            
    copy target/oaAppStore.jar to oaappstore-run/executable-jar/oaAppStore.jar 


5: update files in oaappstore-run project under \executable-jar directory
    users can download these files from github and then run java -jar
    
    OAAppStore.ico
    oaAppStore.jar  (already done from #3)
    version.ini - update version, release and the files to download
       release needs to match values.properties
    
5.2 commit oaappstore-run to github.   This will make it available to users to install, and for installed OAAppStore to automatically update.


*NEXT:  create OS specific installer by finding the subdirectory for the OS and the readme.txt for it. 
	note:  installers only need to be created once, and will auto update new OAAppStore releases from github.

