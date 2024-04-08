
20220316 ubuntu linux using jdk17

Research:
	main:
		https://docs.oracle.com/en/java/javase/16/jpackage/packaging-tool-user-guide.pdf
			see:
		https://docs.oracle.com/en/java/javase/16/jpackage/packaging-overview.html#GUID-786E15C0-2CE7-4BDF-9B2F-AC1C57249134
	Others			 
		https://linuxhint.com/install_jdk_14_ubuntu/
		https://alvinalexander.com/java/how-use-jpackage-command-java-14-jdk-sdk/


Notes:
	This will create a *.dbm installation file for Ubuntu.
	JPackage has a resource directory option that allows it to include custom debian files, where I have included a postinst file.
	The verbose option will include lots of info about what is happening and can be used to help customize.
	I have two ways to create ... step 2 then 3 or step 2&3 (combined)
		the separate steps help to see what is going on by first building the image, and then the installer, in 2 steps
			the verbose option and the temp directory option allow to see what it is doing.

get jdk
	sudo apt install openjdk-17-jdk

	if error:
		sudo apt --fix-broken install
			and then retry

java -version

Change default command line JDK 
	sudo update-alternatives --config java
	

** get OABuilder-[version].jar from github


run maven install from Eclipse
	> creates target/oabuilder-3.2.3.jar



================================================================================
Note:  the OABuilder Ubuntu installer 

*** NOTE: Only need to create installer once, or when a new JDK is needed.  Otherwise, OABuilder will automatically 
	update itself from github/OABuilder_run project


First, follow instructions from jpackage/README.txt to build files for OABuilder
	this can be done on PC and the the files can be retrieved from github OABuilder_Run project
	to do the Linux installer.


#0) set correct cmd line JDK version
	get java version
		java -version
	
	Change default command line JDK to Java 17 
		sudo update-alternatives --config java

#1) remove previous installation
	sudo dpkg --remove oabuilder
	sudo dpkg --purge oabuilder
	sudo rm -rf ~/oabuilder
	
#1.2) clear jpackage files
	cd ~/git/oabuilder/jpackage/ubuntu
	rm -rf OABuilder
	rm oabuilder*.deb
	rm -rf tempfiles


***skip to step "2&3 combined"
#2) CREATE application image (**NOTE**: steps 2&3 could be combined ... using #2&3 option below)
		this option will first create a image, and step #3 will then use the image to create installer
	cd ~/git/oabuilder/jpackage/ubuntu
	copy oabulder.jar to ubuntu\input directory
	jpackage --type app-image --input ./input --dest . --name OABuilder --main-jar oabuilder.jar --main-class com.viaoa.builder.control.Startup --icon OABuilder.png --arguments rootDir=~/oabuilder/lib/app --verbose --app-version 3.2 --java-options "-Xmx1g" --vendor ViaOA
	> creates OABuilder
		/bin/OABuilder is executable
		/lib/app is where uber jar and other files are
		/lib/runtime is jdk
	
#3) CREATE installer 
	cd ~/git/oabuilder/jpackage/ubuntu
    jpackage --resource-dir resource_dir --temp tempfiles --name OABuilder --app-image OABuilder --dest . --type deb --vendor ViaOA --app-version 3.2 --verbose --linux-shortcut --icon OABuilder.png --file-associations filemap.txt --linux-menu-group "Development" --copyright "Copyright 2022 Via Object Architects (viaoa.com)" --install-dir ~ --license-file license.txt
	> creates oabuilder*.deb

***Preferred
#2&3 combined) create installer directly *** use this unless troublshooting ***
	cd ~/git/oabuilder/jpackage/ubuntu
    jpackage --verbose --name OABuilder --input ./input --main-jar oabuilder.jar --main-class com.viaoa.builder.control.Startup --arguments rootDir=~/oabuilder/lib/app --java-options "-Xmx1g" --resource-dir resource_dir --dest . --type deb --vendor ViaOA --app-version 3.2 --linux-shortcut --icon OABuilder.png --file-associations filemap.txt --linux-menu-group "Development" --copyright "Copyright 2022 Via Object Architects (viaoa.com)" --install-dir ~ --license-file license.txt
	> creates oabuilder*.deb

#4) verify		
	cd ~/git/oabuilder/jpackage/ubuntu
	dpkg --contents oabuilder_3.2-1_amd64.deb
	> also check console and tempfiles
	
#5) install
	cd ~/git/oabuilder/jpackage/ubuntu
	sudo dpkg -D2 --install oabuilder_3.2-1_amd64.deb
	# see for debug codes: 
		https://stackoverflow.com/questions/11274290/why-is-my-debian-postinst-script-not-being-run
	
#6) verify	
	dpkg --list | grep -i oabui
#7) check files
	/home/vvia/oabuilder/lib/app/
	ls -l /home/vvia/oabuilder 

#8) Change default command line JDK back to previous version
	get java version
		java -version
	
	Change default command line JDK to Java 17 
		sudo update-alternatives --config java

 

qqqqqqqqqqqq

sudo apt list
sudo apt remove oabuilder

https://store.kde.org/browse/

https://www.debian.org/doc/manuals/maint-guide/start.en.html


