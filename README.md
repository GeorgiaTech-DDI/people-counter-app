# People Counter App

**[Task Board](https://trello.com/b/FXZQRIFY/people-counter-app)**

## Setup
**Note**: These instructions require access to a Keepass
database containing credentials for the People Counter project. For more information, contact Amy Truong at [amytruong@gatech.edu](mailto:amytruong@gatech.edu).

### Installing Android Studio
1. Install Android Studio using [this guide](https://developer.android.com/studio/install).
1. Open Android Studio. Click on `More Actions` or `⋮` and then `Virtual Device Manager`.
2. Create a new device. Select `Pixel 3` for the device definition and `R` for the system image.

### Configuring Amplify CLI

1. Install the Amplify CLI using [this guide](https://docs.amplify.aws/cli/start/install/).
2. Run `amplify configure` and press `Enter` until you are prompted for an access key ID.
3. Enter the access key ID and secret access key from the `AWS Access Key` entry in the Keepass database. The access key ID is the username and the secret access key is the password.
4. Leave the default region name as `us-east-1` and default output format as `None`.
```
> aws configure

AWS Access Key ID [None]: <ACCESS KEY ID>
AWS Secret Access Key [None]: <SECRET ACCESS KEY>
Default region name [us-east-1]: <ENTER>
Default output format [None]: <ENTER>
```

### Running the app
1. Clone the repository.
2. Run `amplify init` within the new directory.
3. Set the environment name to `dev`, default editor to `Android Studio`, and authentication method to `AWS profile`.
```
> amplify init

? Enter a name for the environment: dev
? Choose your default editor: Android Studio
Using default provider  awscloudformation
? Select the authentication method you want to use: AWS profile
```
4. When prompted for a profile to use, choose `default`.
```
? Please choose the profile you want to use: default
⚠️ Failed to resolve AppId. Skipping parameter download.
√ Initialized provider successfully.
✅ Initialized your environment successfully.

Your project has been successfully initialized and connected to the cloud!
```
5. Once the environment has been initialized successfully, open the project in Android Studio. You can now run the app in the emulator created previously or on an Android phone with USB debugging enabled.

# Resources
TODO