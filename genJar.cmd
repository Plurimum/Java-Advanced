SET class1=info/kgeorgiy/ja/lihanov/implementor/Implementor

javac -cp ../../java-advanced-2021/artifacts/info.kgeorgiy.java.advanced.implementor.jar %class1%.java
jar cmvf MANIFEST.MF Implementor.jar %class1%.class