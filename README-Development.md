# JGRAM Developer Note


## Development Environment Setup

We use gradle wrapper, so need to install gradle on your sandbox. 
**Note:** We will soon update an Eclipse project version as well (without using gradle)

### OS X or Linux
- Install JDK 11.0.3 at /Users/edu/sandbox/apps/jdk/
- create ~/scripts/env.sh
```sh
#!/bin/sh
export JAVA_HOME="/Users/edu/sandbox/apps/jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```
### Windows
- Install JDK 11.0.3 at D:/sandbox/apps/jdk
- create C:/sandbox/scripts/env.bat
```bat
setlocal
set JAVA_HOME="C:/sandbox/apps/jdk/"
set PATH=%PATH%;"%JAVA_HOME%/bin"
endlocal
```

## Build

### OS X or Linux
```sh
source C:/sandbox/scripts/env.sh

cd <PROJECT_HOME_DIR>
./gradlew clean build

```
### Windows
```cmd
cd C:/sandbox/scripts/
env.bat

cd <PROJECT_HOME_DIR>
gradlew clean build

```

## Run application

### OS X or Linux
```sh
source ~/scripts/env.sh

cd <PROJECT_HOME_DIR>
./gradlew run

```
### Windows
```cmd
cd C:/sandbox/scripts/
env.bat

cd <PROJECT_HOME_DIR>
gradlew run
```

## Create distribution

### OS X or Linux
```sh
source ~/scripts/env.sh

cd <PROJECT_HOME_DIR>
./gradlew jar

```
This will generate jar distribution under <PROJECT_HOME_DIR>/server/build/distributions/server-1.0.zip

### Windows
```cmd
cd C:/sandbox/scripts/
env.bat

cd <PROJECT_HOME_DIR>
gradlew jar
```
This will generate jar distribution under <PROJECT_HOME_DIR>/server/build/distributions/server-1.0.zip


## Generating Java Doc

### OS X or Linux
```sh
source ~/scripts/env.sh

cd <PROJECT_HOME_DIR>
./gradlew javadoc

```
This will generate javadoc under <PROJECT_HOME_DIR>/server/build/docs/javadoc/


### Windows
```cmd
~/scripts/env.bat

cd <PROJECT_HOME_DIR>
gradlew javadoc

```
This will generate javadoc under <PROJECT_HOME_DIR>/server/build/docs/javadoc/
