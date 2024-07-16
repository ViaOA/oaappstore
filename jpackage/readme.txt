



================= Add application to OAAppStore =================

This is to use any OA Application from OAAppStore

see: OAAppStore-Installer, for the App Store installer.  

   
    create jar files
    maven clean install

    copy oaappstore-1.0.2.jar to OAAppStore-Run/jarstore/com/viaoa/oaappstore

    // OAAppStore project only
    the maven install also created the file "oaappstore.jar" - which is an uber jar with everything except com.viaoa.**
       if there has been "outside" (not oa) jar dependency changes, then the project needs to use the new dependencies
           copy oaappstore.jar to OAAppStore-Run/jarstore/com/viaoa/dependency-uber-1.0.XX.jar
    
    if there are new oa-* jar files, then copy them to OAAppStore-Run/jarstore/com/viaoa
     
    update OAAppStore-Run/appstore/[project url]/version.ini
        update version, release and the files to download
        release needs to match values.properties "release"


