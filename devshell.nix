{ pkgs }:

with pkgs;

let
  # android-studio is not available in aarch64-darwin
  conditionalPackages = if pkgs.system != "aarch64-darwin" then [ android-studio ] else [ ];

  # Find the AAPT2 executable in the Android SDK
  aapt2Path = "${android-sdk}/share/android-sdk/build-tools/35.0.0/aapt2";

  # Create an FHS environment for Android development
  androidFHSEnv = pkgs.buildFHSEnv {
    name = "android-fhs-env";
    targetPkgs = pkgs: with pkgs; [
      android-sdk
      gradle
      jdk
      zlib
      ncurses5
      stdenv.cc.cc.lib
    ] ++ conditionalPackages;
    profile = ''
      export ANDROID_HOME="${android-sdk}/share/android-sdk"
      export ANDROID_SDK_ROOT="${android-sdk}/share/android-sdk"
      export JAVA_HOME="${jdk.home}"
      export PATH="$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$PATH"
      
      # Create an alias for gradlew that includes the AAPT2 path
      alias gradlew='./gradlew -Pandroid.aapt2FromMavenOverride=${aapt2Path}'
      
      echo "FHS environment ready. Use 'gradlew assembleDebug' to build the app."
    '';
    runScript = "bash";
  };

  # Create a wrapper script for gradlew that sets the AAPT2 path
  gradlewWrapper = pkgs.writeScriptBin "gradlew-nix" ''
    #!/usr/bin/env bash
    ./gradlew -Pandroid.aapt2FromMavenOverride=${aapt2Path} "$@"
  '';
in
with pkgs;

# Configure your development environment.
#
# Documentation: https://github.com/numtide/devshell
devshell.mkShell {
  name = "android-project";
  motd = ''
    Entered the Android app development environment.
    
    To build the app, you have two options:
    
    Option 1: Use the gradlew-nix wrapper (recommended)
      gradlew-nix assembleDebug
    
    Option 2: Use the FHS environment
      android-fhs-env
      # Inside the FHS environment, gradlew is aliased to include the AAPT2 path
      gradlew assembleDebug
    
    Both options configure Gradle to use the AAPT2 executable from the Nix store,
    avoiding issues with dynamically linked executables on NixOS.
  '';
  env = [
    {
      name = "ANDROID_HOME";
      value = "${android-sdk}/share/android-sdk";
    }
    {
      name = "ANDROID_SDK_ROOT";
      value = "${android-sdk}/share/android-sdk";
    }
    {
      name = "JAVA_HOME";
      value = jdk.home;
    }
    {
      name = "GRADLE_OPTS";
      value = "-Dandroid.aapt2FromMavenOverride=${aapt2Path}";
    }
  ];
  packages = [
    android-sdk
    gradle
    jdk
    androidFHSEnv
    gradlewWrapper
  ] ++ conditionalPackages;
}
