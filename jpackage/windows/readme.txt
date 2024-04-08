

OAAppStore Windows installer 

20240310 windows using jdk 21 jpackage

*** NOTE: Only need to create installer once, or when a new JDK is needed.  Otherwise, OAAppStore will automatically 
    update itself from github/OAAppStore-Run project


C:\Program Files\Java\jdk-21.0.2
JAVA_HOME="C:\Program Files\Java\jdk-17\bin\java" 

Downloaded WiX 3.0 or later from https://wixtoolset.org and add it to the PATH.
    after install, it will be in programs, search "wix"
    jpackage uses it for Windows installer
    

*** NOTE: Only need to create installer once, or when a new JDK is needed.  Otherwise, OAAppStore will automatically 
    update itself from github/OAAppStore-run project


First, follow instructions from jpackage/README.txt to build files for OAAppStore
    this can be done on PC and the the files can be retrieved from github OAAppStore-Run project
    to do the Linux installer.


#0) set correct cmd line JDK version, ex: jdk 21
    java -version
    
    control panel / Programs / java
        add jvm version to list
    
    Control Panel\Environment Variables
        add to begin of path:  "C:\Program Files\Java\jdk-21.0.2\bin"
        
#1) remove previous installation
    windows uninstall OAAppStore

#1.2) clear jpackage files
    cd c:\users\vvia\git\oaappstore\jpackage\windows
    del OAAppStore-1.0.0.msi
    rmdir tempfiles /s

#1.3) new jar file
    cd c:\users\vvia\git\oaappstore\jpackage\windows
    copy target\oaAppStore.jar to jpackage\windows\input directory
     
    make sure input directory has server.ini and single.ini

*troubleshooting only*, otherwise go to step "2&3 combined"
    #2) CREATE application image 
            this option will first create a image, and step #3 will then use the image to create installer
        jpackage --type app-image --input ./input --dest . --name OAAppStore --main-jar oaAppStore.jar --main-class com.viaoa.appstore.control.Startup --icon OAAppStore.ico 
        --arguments rootDirectory=app --arguments single --verbose --app-version 1.0.0 --java-options "-Xmx2g" --vendor ViaOA
            
        
        
        > creates OAAppStore
            /bin/OABuilder is executable
            /lib/app is where uber jar and other files are
            /lib/runtime is jdk
    
    #3) CREATE installer 
        copy oabulder.jar to windows\input directory
        cd c:\users\vvia\git\oabuilder\jpackage\windows
        
        jpackage --resource-dir resource_dir --temp tempfiles --name OAAppStore --app-image OAAppStore --dest . --type msi --vendor ViaOA --app-version 1.0.0 --verbose --icon OABuilder.ico --file-associations filemap.txt --copyright "Copyright 2024 ViaOA" --license-file license --win-menu --win-menu-group ViaOA --win-shortcut-prompt
            > creates OAAppStore-*.msi
    
    
***One step,  use this unless there is a need for troublshooting (previous steps 2 & 3) ***
#2&3 combined) create installer directly 
    
    jpackage --verbose --temp tempfiles --name OAAppStore --input ./input --main-jar oaAppStore.jar --main-class com.viaoa.appstore.control.StartupController --arguments rootDirectory=app --arguments single --java-options "-Xmx2g" --resource-dir resource_dir --dest . --type msi --vendor ViaOA --app-version 1.0.0 --icon OAAppStore.ico --file-associations filemap.txt --copyright "Copyright 2024 ViaOA" --license-file license.txt --win-menu --win-menu-group ViaOA --win-shortcut-prompt --win-per-user-install
        # others
        --win-dir-chooser 
        --win-console
     
        > creates 
            OAAppStore-*.msi

jpackage --verbose --temp tempfiles --name OAAppStore --input ./input --main-jar oaAppStore.jar --main-class com.viaoa.appstore.control.StartupController --arguments rootDirectory=app --arguments single --java-options "-Xmx2g" --resource-dir resource_dir --dest . --type msi --vendor ViaOA --app-version 1.0.0 --icon OAAppStore.ico --file-associations filemap.txt --copyright "Copyright 2024 ViaOA" --license-file license --win-menu --win-menu-group ViaOA --win-shortcut-prompt --win-per-user-install



4) installation
    run OAAppStore.msi
    installs:  
    C:\Users\vince\AppData\Local\OAAppStore
        OAAppStore.exe
    C:\Users\vince\AppData\Local\OAAppStore\app
        OAAppStore.cfg
            Args: 
                rootDirectory=app
                single
           


 
 
