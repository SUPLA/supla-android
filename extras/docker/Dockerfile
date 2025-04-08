FROM debian:12.10

ENV ANDROID_SDK_ROOT=/usr/lib/android-sdk
ENV PATH="$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/cmdline-tools/bin:$ANDROID_SDK_ROOT/tools:${PATH}"

RUN cd
RUN apt-get update 
RUN apt-get upgrade -y
RUN apt-get -y install android-sdk wget zip vim
RUN wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
RUN unzip commandlinetools-linux-11076708_latest.zip
RUN rm commandlinetools-linux-*
RUN mv cmdline-tools /usr/lib/android-sdk/
RUN wget https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.deb
RUN dpkg -i jdk-21_linux-x64_bin.deb

RUN yes | sdkmanager "platforms;android-33" --sdk_root=/usr/lib/android-sdk
RUN yes | sdkmanager "build-tools;34.0.0" --sdk_root=/usr/lib/android-sdk

