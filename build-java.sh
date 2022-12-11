modulePath="/usr/share/javafx-fresh/lib"
outputDirectory="./build/"
headerOutputDirectory="./pi-ulcd-jni/"
declare -a javafxModules=(
    "javafx.base"
    "javafx.web"
    "javafx.swing"
    "javafx.graphics"
    "javafx.media"
    )
javaClasspath=""
for val in ${javafxModules[@]}; do
  javaClasspath="$val.jar;$javaClasspath"
done
addModules=""
for val in ${javafxModules[@]}; do
  addModules="$val,$addModules"
done
# java --module-path /usr/share/openjfx/lib --add-modules javafx.base,javafx.graphics,javafx.media -classpath "com.nana;/usr/share/openjfx/lib/javafx.graphics.jar" ./com/nana/uLCDInterface.java
javac -h $headerOutputDirectory --module-path $modulePath --add-modules $addModules -classpath "com.nana;$javaClasspath" -d $outputDirectory com/nana/uLCDInterface.java