SET class1=info\kgeorgiy\ja\lihanov\implementor\Implementor
SET class2=info\kgeorgiy\ja\lihanov\implementor\JarImplementor

javac -cp ..\..\java-advanced-2021\artifacts\info.kgeorgiy.java.advanced.implementor.jar %class1%.java %class2%.java
jar cmvf MANIFEST.MF Implementor.jar %class1%.class %class2%.class
java -jar Implementor.jar -jar info.kgeorgiy.java.advanced.implementor.Impler demonstration/demo.jar