# This installs two files, app.exe and logo.ico, creates a start menu shortcut, builds an uninstaller, and
# adds uninstall information to the registry for Add/Remove Programs
 
# To get started, put this script into a folder with the two files (app.exe, logo.ico, and license.rtf -
# You'll have to create these yourself) and run makensis on it
 
# If you change the names "app.exe", "logo.ico", or "license.rtf" you should do a search and replace - they
# show up in a few places.
# All the other settings can be tweaked by editing the !defines at the top of this script
!define APPNAME "Pluto Plate Reverb"
!define COMPANYNAME "Pluto Plate Reverb"
!define DESCRIPTION "Traditional stereo plate reverb"
# These three must be integers
!define VERSIONMAJOR 1
!define VERSIONMINOR 1
!define VERSIONBUILD 1
# These will be displayed by the "Click here for support information" link in "Add/Remove Programs"
# It is possible to use "mailto:" links in here to open the email client
!define HELPURL "http://www.plutoplate.com/faq.html" # "Support Information" link
!define UPDATEURL "http://www.plutoplate.com/" # "Product Updates" link
!define ABOUTURL "http://www.plutoplate.com/specifications.html" # "Publisher" link
# This is the size (in kB) of all the files copied into "Program Files"
!define INSTALLSIZE 7233
 
RequestExecutionLevel admin ;Require admin rights on NT6+ (When UAC is turned on)
 
InstallDir "$PROGRAMFILES\${APPNAME}"
 
# rtf or txt file - remember if it is txt, it must be in the DOS text format (\r\n)
LicenseData "gpl.txt"
# This will be in the installer/uninstaller's title bar
Name "${APPNAME}"
Icon "logo.ico"
outFile "plutoplate-installer.exe"
 
!include LogicLib.nsh
 
# Just three pages - license agreement, install location, and installation
page license
page directory
Page instfiles
 
!macro VerifyUserIsAdmin
UserInfo::GetAccountType
pop $0
${If} $0 != "admin" ;Require admin rights on NT4+
        messageBox mb_iconstop "Administrator rights required!"
        setErrorLevel 740 ;ERROR_ELEVATION_REQUIRED
        quit
${EndIf}
!macroend
 
function .onInit
	setShellVarContext all
	!insertmacro VerifyUserIsAdmin
functionEnd

; These are the programs that are needed by ACME Suite.
Section -Prerequisites
  IfFileExists "$PROGRAMFILES\Phidgets\phidget22.dll" done do_phidgets_install
  
  do_phidgets_install:
	Var /GLOBAL EXIT_CODE
	MessageBox MB_YESNO "Install Phidgets 32-bit Drivers? If not, you will be prompted for the 64-bit option as well." /SD IDYES IDNO installPhidgets64
		Var /GLOBAL phidgets32DidDownload	
		NSISdl::download "http://www.phidgets.com/downloads/libraries/Phidget-x86.exe" "$TEMP\Phidgets.exe" $phidgets32DidDownload
	
		StrCmp $phidgets32DidDownload success installPhidgets32Manual
	installPhidgets32Manual:
		File ".\Prerequisites\Phidget22-x86_1.0.0.20181105.exe"
		ExecWait ".\Prerequisites\Phidget22-x86_1.0.0.20181105.exe"
		IfErrors fail
		Goto is_reboot_requested
	installPhidgets64:
		MessageBox MB_YESNO "Install Phidgets 64-bit Drivers?" /SD IDYES IDNO done
		Var /GLOBAL phidgets64DidDownload	
		NSISdl::download "http://www.phidgets.com/downloads/libraries/Phidget-x64.exe" "$TEMP\Phidgets.exe" $phidgets64DidDownload
		
		StrCmp $phidgets64DidDownload success installPhidgets64Manual
	installPhidgets64Manual:
		File ".\Prerequisites\Phidget22-x64_1.0.0.20181105.exe"
		ExecWait ".\Prerequisites\Phidget22-x64_1.0.0.20181105.exe"
		
		IfErrors fail
		Goto is_reboot_requested
	success:
			ExecWait "$TEMP\Phidgets.exe" $EXIT_CODE
			Goto is_reboot_requested
	fail:
			MessageBox MB_OK|MB_ICONEXCLAMATION "Unable to download Phidgets 32-bit Drivers.  ${APPNAME} will be installed, but will not function without Phidgets!"
			Goto done
	# $EXIT_CODE contains the return codes.  1641 and 3010 means a Reboot has been requested
	is_reboot_requested:
		${If} $EXIT_CODE = 1641
		${OrIf} $EXIT_CODE = 3010
			SetRebootFlag true
		${EndIf}
		Goto done
  done:
SectionEnd
 
section "install"
	# Files for the install directory - to build the installer, these should be in the same directory as the install script (this file)
	setOutPath $INSTDIR
	# Files added here should be removed by the uninstaller (see section "uninstall")
	file "Plutoplate.jar"
	file "phidget22.jar"
	file "logo.ico"
	# Add any other files for the install directory (license files, app data, etc) here
 
	# Uninstaller - See function un.onInit and section "uninstall" for configuration
	writeUninstaller "$INSTDIR\uninstall.exe"
 
	# Start Menu
	createDirectory "$SMPROGRAMS\${COMPANYNAME}"
	createShortCut "$SMPROGRAMS\${COMPANYNAME}\${APPNAME}.lnk" "$INSTDIR\Plutoplate.jar" "" "$INSTDIR\logo.ico"
	createShortCut "$DESKTOP\${APPNAME}.lnk" "$INSTDIR\Plutoplate.jar" "" "$INSTDIR\logo.ico"
 
	# Registry information for add/remove programs
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "DisplayName" "${APPNAME} - ${DESCRIPTION}"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "UninstallString" "$\"$INSTDIR\uninstall.exe$\""
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "QuietUninstallString" "$\"$INSTDIR\uninstall.exe$\" /S"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "InstallLocation" "$\"$INSTDIR$\""
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "DisplayIcon" "$\"$INSTDIR\logo.ico$\""
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "Publisher" "$\"${COMPANYNAME}$\""
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "HelpLink" "$\"${HELPURL}$\""
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "URLUpdateInfo" "$\"${UPDATEURL}$\""
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "URLInfoAbout" "$\"${ABOUTURL}$\""
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "DisplayVersion" "$\"${VERSIONMAJOR}.${VERSIONMINOR}.${VERSIONBUILD}$\""
	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "VersionMajor" ${VERSIONMAJOR}
	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "VersionMinor" ${VERSIONMINOR}
	# There is no option for modifying or repairing the install
	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "NoModify" 1
	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "NoRepair" 1
	# Set the INSTALLSIZE constant (!defined at the top of this script) so Add/Remove Programs can accurately report the size
	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "EstimatedSize" ${INSTALLSIZE}
sectionEnd
 
# Uninstaller
 
function un.onInit
	SetShellVarContext all
 
	#Verify the uninstaller - last chance to back out
	MessageBox MB_OKCANCEL "Permanantly remove ${APPNAME}?" IDOK next
		Abort
	next:
	!insertmacro VerifyUserIsAdmin
functionEnd
 
section "uninstall"
 
	# Remove desktop shortcut
	delete "$DESKTOP\${APPNAME}.lnk"
	# Remove Start Menu launcher
	delete "$SMPROGRAMS\${COMPANYNAME}\${APPNAME}.lnk"
	# Try to remove the Start Menu folder - this will only happen if it is empty
	rmDir "$SMPROGRAMS\${COMPANYNAME}"
 
	# Remove files
	delete $INSTDIR\Plutoplate.jar
	delete $INSTDIR\phidget22.jar
	delete $INSTDIR\logo.ico
 
	# Always delete uninstaller as the last action
	delete $INSTDIR\uninstall.exe
 
	# Try to remove the install directory - this will only happen if it is empty
	rmDir $INSTDIR
 
	# Remove uninstaller information from the registry
	DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}"
sectionEnd