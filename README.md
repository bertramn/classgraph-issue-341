# Replicate JVM crash during Classgraph scan
 
 https://github.com/classgraph/classgraph/issues/341

## Working Build

```bash
mvn clean verify
```

finishes fine but produces a junit warning

```
WARNING: TestEngine with ID 'junit-jupiter' failed to execute tests
java.lang.NoClassDefFoundError: org/hamcrest/SelfDescribing
        at java.base/java.lang.ClassLoader.defineClass1(Native Method)
        at java.base/java.lang.ClassLoader.defineClass(ClassLoader.java:1016)
        at java.base/java.security.SecureClassLoader.defineClass(SecureClassLoader.java:174)
        at java.base/jdk.internal.loader.BuiltinClassLoader.defineClass(BuiltinClassLoader.java:802)
        ...
```


## Crashing Build

```bash
mvn clean verify -Phamcrest
```

introduces a recent version of hamcrest as test dependency which crashes the JVM

```
...

# Created at 2019-04-30T23:18:37.417
    Using VM: OpenJDK 64-Bit Server VM

# Created at 2019-04-30T23:18:37.417

# Created at 2019-04-30T23:18:40.446
/bin/sh: line 1:  8296 Abort trap: 6           /Library/Java/JavaVirtualMachines/amazon-corretto-11.jdk/Contents/Home/bin/java -XshowSettings:vm --illegal-access=warn -javaagent:/Users/bertramn/.m2/repository/org/jacoco/org.jacoco.agent/0.8.3/org.jacoco.agent-0.8.3-runtime.jar=destfile=/Users/bertramn/workspaces/fasttrack/mocks/classgraph-issue-341/target/jacoco.exec,append=true -jar /Users/bertramn/workspaces/fasttrack/mocks/classgraph-issue-341/target/surefire/surefirebooter11137074665374974571.jar /Users/bertramn/workspaces/fasttrack/mocks/classgraph-issue-341/target/surefire 2019-04-30T23-18-36_868-jvmRun1 surefire14206125834013885742tmp surefire_08669356738330033965tmp

```

Introducing an older hamcrest version also triggers the crash

```bash
mvn clean verify -Phamcrest-old
```
