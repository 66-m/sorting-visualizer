@echo off
if not exist "target\sorting-visualizer.jar" (
    echo Jar not found, please build first ^(build.cmd^) or run mvnw.cmd package.
    exit /b 1
)
java --enable-native-access=ALL-UNNAMED --add-opens=java.desktop/com.sun.media.sound=ALL-UNNAMED -cp ".\target\sorting-visualizer.jar;.\target\dependency\*" io.github.compilerstuck.control.MainController %*
