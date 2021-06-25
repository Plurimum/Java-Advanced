SET link=https://docs.oracle.com/en/java/javase/11/docs/api/
SET path1=info/kgeorgiy/ja/lihanov/implementor/
SET path2=../../java-advanced-2021/modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/

javadoc -link %link% -private -d ../javadoc %path1%Implementor.java %path2%Impler.java %path2%JarImpler.java %path2%ImplerException.java
