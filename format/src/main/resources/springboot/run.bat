@echo off
chcp 65001


set allparam=

:param
set str=%1
if "%str%"=="" (
    goto end
)
set allparam=%allparam% %str%
shift /0
goto param
:end

set path=%path%;.;
set app_jar=tools-1.0-SNAPSHOT.jar

set script_path=%~dp0

set cur_path=%cd%
cd %script_path%
cd ..
set home_path=%cd%
cd %cur_path%

:: you can add or set your running parameter in here
set PARAM=%allparam%

:: you can add your jvm-properties in here
set PROPERTIES=

set prop_path=%home_path%/conf
set lib_path=%home_path%/libs
set loader_path=%prop_path%,%lib_path%

:: if you has your jar-libs,you can add them in here
set extra_path=

if defined extra_path  (
    set loader_path=%loader_path%,%extra_path%
)


set CMD=java %PROPERTIES% -Dloader.path=%loader_path% -jar %lib_path%/%app_jar% %PARAM%

echo %CMD%

%CMD%

pause