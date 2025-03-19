# gomuks android
A GeckoView wrapper app for gomuks web that adds push notifications and other
native integrations.

## Development with Nix

This project uses Nix for reproducible development environments.

1. Enter the development environment:
   ```bash
   nix develop
   ```

2. Build the app:
   ```bash
   # Option 1: Use the gradlew-nix wrapper (recommended)
   gradlew-nix assembleDebug

   # Option 2: Use the FHS environment
   android-fhs-env
   # Inside the FHS environment, gradlew is aliased to include the AAPT2 path
   gradlew assembleDebug
   ```

### Notes for NixOS Users

This project includes special configuration to handle the AAPT2 executable on NixOS. The development environment automatically sets up the necessary configuration to use the AAPT2 executable from the Nix store, avoiding issues with dynamically linked executables.

The `gradlew-nix` wrapper and the `gradlew` alias in the FHS environment both pass the AAPT2 path directly to Gradle, ensuring that it uses the Nix-provided AAPT2 executable instead of downloading its own.
