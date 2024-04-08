;This file will be executed next to the application bundle image
;I.e. current directory will contain folder OAAppStore with application files
[Setup]
AppId={{com.viaoa.appstore}}
AppName=OAAppStore
AppVersion=0.0.1
AppVerName=OAAppStore 0.0.1
AppPublisher=Goldovi
AppComments=OAAppStore Platform
AppCopyright=(c) 2019 Goldovi
AppPublisherURL=http://www.goldovi.com/
AppSupportURL=http://www.goldovi.com/
AppUpdatesURL=http://www.goldovi.com/
DefaultDirName={localappdata}\OAAppStore
DisableStartupPrompt=No
DisableDirPage=No
DisableProgramGroupPage=No
DisableReadyPage=No
DisableFinishedPage=No
DisableWelcomePage=No
DefaultGroupName=Goldovi AppStore
;Optional License
LicenseFile=license.txt
;WinXP or above
MinVersion=0,5.1 
OutputBaseFilename=OAAppStore-0.0.1
Compression=lzma
SolidCompression=yes
PrivilegesRequired=lowest
SetupIconFile=OAAppStore\OAAppStore.ico
UninstallDisplayIcon={app}\OAAppStore.ico
UninstallDisplayName=OAAppStore
WizardImageStretch=No
WizardSmallImageFile=OAAppStore-setup-icon.bmp   
ArchitecturesInstallIn64BitMode=x64


[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "OAAppStore\OAAppStore.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "OAAppStore\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\OAAppStore"; Filename: "{app}\OAAppStore.exe"; IconFilename: "{app}\OAAppStore.ico"; Check: returnTrue()
;Name: "{commondesktop}\OAAppStore"; Filename: "{app}\OAAppStore.exe";  IconFilename: "{app}\OAAppStore.ico"; Check: returnFalse()
Name: "{userdesktop}\OAAppStore"; Filename: "{app}\OAAppStore.exe"; IconFilename: "{app}\OAAppStore.ico"; Tasks: desktopicon
Name: "{group}\Uninstall OAAppStore"; Filename: "{uninstallexe}";IconFilename: "{app}\OAAppStore.ico";


[Run]
Filename: "{app}\OAAppStore.exe"; Parameters: "-Xappcds:generatecache"; Check: returnFalse()
Filename: "{app}\OAAppStore.exe"; Description: "{cm:LaunchProgram,OAAppStore}"; Flags: nowait postinstall skipifsilent; Check: returnTrue()
Filename: "{app}\OAAppStore.exe"; Parameters: "-install -svcName ""OAAppStore"" -svcDesc ""OAAppStore from Goldovi"" -mainExe ""OAAppStore.exe""  "; Check: returnFalse()

[UninstallRun]
Filename: "{app}\OAAppStore.exe"; Parameters: "-uninstall -svcName OAAppStore -stopOnUninstall"; Check: returnFalse()

[Code]
function returnTrue(): Boolean;
begin
  Result := True;
end;

function returnFalse(): Boolean;
begin
  Result := False;
end;

function InitializeSetup(): Boolean;
begin
// Possible future improvements:
//   if version less or same => just launch app
//   if upgrade => check if same app is running and wait for it to exit
//   Add pack200/unpack200 support? 
  Result := True;
end;  
