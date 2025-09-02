if (-Not (Test-Path -Path "C:\ocesql4j\bin")) {
    mkdir C:\ocesql4j\bin\
}

if (-Not (Test-Path -Path "C:\ocesql4j\lib")) {
    mkdir C:\ocesql4j\lib\
}

copy .\x64\Release\ocesql4j.exe C:\ocesql4j\bin
copy ..\dblibj\target\scala-2.13\ocesql4j.jar C:\ocesql4j\lib\