@echo off
::
:: Configuration variables
::
:: JAVA_HOME
::   Home of Java installation.
::
:: JAVA_OPTIONS
::   Extra options to pass to the JVM

:: ----- Verify and Set Required Environment Variables -------------------------

if not "%JAVA_HOME%" == "" goto gotJavaHome
echo You must set JAVA_HOME to point at your Java Development Kit installation
goto end
:gotJavaHome

:: ----- Check System Properties -----------------------------------------------

if not "%JAVA_OPTIONS%" == "" goto gotJavaOptions
set JAVA_OPTIONS=-Xmx512M -Xms32M
:: set JAVA_OPTIONS=-Xmx512M -Xms32M -Xrunhprof:heap=all,cpu=samples,thread=y,depth=3
:: set JAVA_OPTIONS=-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n
:gotJavaOptions

:: ----- Set Up The Classpath --------------------------------------------------

"%JAVA_HOME%\bin\java.exe" %JAVA_OPTIONS% -jar ./lib/welkin.jar %1 %2 %3 %4 %5 %6 %7 %8 %9

set JAVA_OPTIONS=

