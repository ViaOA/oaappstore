




20240418 Update: OAAppStore-Installer was created to create an intial java Installer that
is much smaller.  It's "only job" is include JDK 21 and to load the latest oaappstore.jar from github

See OAAppStore-Installer readme.txt files 

For this app, you need to update the OAAppStore-Run with new releases
   
1: set release
    values.properties (release, version)
    pom.xml
        update oa version
   
2: create jar files
    maven clean install
    
    this will create 
        oaappstore.jar - which is everything except com.viaoa.**
            rename to dependency-uber-1.0.0.jar
        oaappstore-1.0.0.jar - which is only oaappstore
            rename to oaappstore.jar
            
    copy oaappstore.jar to OAAppStore-Run/executable-jar
    copy dependency-uber-1.0.0.jar to OAAppStore-Run/jarstore
    
    if there are new oa-* jar files, then copy them to OAAppStore-Run/jarstore
     
    update OAAppStore-Run/executable-jar/version.ini
        update version, release and the files to download
        release needs to match values.properties "release"








OLD === OLD === OLD === OLD === OLD === OLD === OLD === OLD === OLD === OLD === OLD === OLD ===  
OLD === OLD === OLD === OLD === OLD === OLD === OLD === OLD === OLD === OLD === OLD === OLD ===  

This used jpackage to create installer, which was about 40mb larger then using OAAppStore-Installer


Instructions for creating OS specific installers for OAAppStore by using JDK21 JPackage command.
   note: this was taken from OABuilder project


FIRST, build OAAppStore and files:
	Before creating the installer, need to build OAAppStore and it's dependencies,
	Do this from windows Eclipse, since it has the keystore for jar signing.


Steps to follow to build OAAppStore Java App:

1: determine new versions that will be used
    oa 3.7.7
    oaappstore 0.0.1

3: create oaappstore jar
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
            
    copy target/oaappstore.jar to oaappstore-run/executable-jar/oaappstore.jar 


5: update files in oaappstore-run project under \executable-jar directory
    users can download these files from github and then run java -jar
    
    OAAppStore.ico
    oaappstore.jar  (already done from #3)
    version.ini - update version, release and the files to download
       release needs to match values.properties
    
5.2 commit oaappstore-run to github.   This will make it available for users to install, and for installed OAAppStore to automatically update.


*NEXT:  create OS specific installer by finding the subdirectory for the OS and the readme.txt for it. 
	note:  installers only need to be created once, and will auto update new OAAppStore releases from github.

