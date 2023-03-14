# People Counter App

## **Task Board**
## Join the Trello at [this link](https://trello.com/invite/b/FXZQRIFY/ATTI4be1d9e37624f00442f8c742c447cac9D96B85AB/people-counter-app) to see and complete your assigned tasks!

# Documentation
## Installing Kivy
Install Kivy: https://kivy.org/doc/stable/gettingstarted/installation.html 
  (You will need python in the same location)

## Building the app on GitHub Actions
### On development branches
1. Create a new branch to use for development.
2. If you want to run builds on that branch every push, edit this part of the `.github/workflows/main.yml` file to include the name of your new branch. Make sure to commit your changes.
```
push:
      branches:
      - main
      - <YOUR-BRANCH-NAME>
```
3. Now, every time you push to the branch, it will start a new build. Each build will take around ~20 mins. If it is successful, an artifact named `package.zip` will be uploaded to the build page. Inside is an `.apk` file that you can install on an Android phone or emulator.

### On `main` branch
1. When you have tested the app on your development branch, you can create a pull request to the `main` branch. This pull request has to approved by another person before it is merged.

## Building the app locally
### Windows
1. Install WSL and Ubuntu using [this guide](https://learn.microsoft.com/en-us/windows/wsl/install).
2. Install Docker Desktop using [this guide](https://docs.docker.com/desktop/install/windows-install/).
3. Open the `Ubuntu` app from the Start Menu.
4. Inside the Ubuntu terminal, clone this repository, and navigate to the new directory.
5. Make sure Docker Desktop is running. If not, open it from the Start Menu.
6. Run this command in the repository folder. There should be a `Dockerfile` in the directory. This command builds a Docker image from the `Dockerfile`.
```
docker build -t buildozer .
```
7. Run this command to test the Docker container. You should get an output similar to `Buildozer 1.5.0`. This command creates and runs a Docker container using the image we built earlier. This container allows us to run the `buildozer` command with the options we set. In this case, the only option is `--version`.
```
docker run buildozer --version
```
8. Run the following commands to create a `~/.buildozer` folder and build an Android app from the `src` directory. This will take around ~30 minutes the first time but will be faster on subsequent runs. This command will store the SDK, NDK, and other libraries that Buildozer requires in the `~/.buildozer` folder.
```
mkdir ~/.buildozer
docker run -v "$HOME/.buildozer":/home/user/.buildozer -v "$PWD/src":/home/user/hostcwd buildozer android debug
```
9. You can replace `android debug` with any other options that `buildozer` accepts, such as `android clean`.
10. On subsequent app builds, you can open the Ubuntu terminal, navigate to the `people-counter-app` directory, and run this command while Docker Desktop is running. The previous steps are not necessary.
```
docker run -v "$HOME/.buildozer":/home/user/.buildozer -v "$PWD/src":/home/user/hostcwd buildozer android debug
```
## Testing the app on Android emulator
If you have an Android device, you can download the `.apk` file onto it and install it directly. Otherwise, you can use the emulator provided by Android Studio to run the app.
1. Install Android Studio using [this guide](https://developer.android.com/studio/install).
2. Open Android Studio. Click on `More Actions` and then `Virtual Device Manager`.
3. Create a new device. Select `Pixel 3` for the device definition and `R` for the system image.
4. Start the device once it's created, and power it on.
5. Drag and drop your `.apk` file onto the emulator. It should start installing automatically.

# Troubleshooting
## Error: `PermissionError: [Errno 13] Permission denied: '/home/user/.buildozer/cache'`
Run the following command to remove and recreate the `~/.buildozer` folder.
```
rm -rf ~/.buildozer && mkdir ~/.buildozer
```

## Error: `Aidl not found, please install it.`
Make sure the following line is in your `buildozer.spec` file.
```
android.accept_sdk_license = True
```

## Error: `Build failed: Requested API target 31 is not available, install it with the SDK android tool.`
Run the following commands in the repository directory to remove all `.buildozer` folders.
```
rm -rf ~/.buildozer && mkdir ~/.buildozer
rm -rf ./src/.buildozer
```
# Resources
TODO
