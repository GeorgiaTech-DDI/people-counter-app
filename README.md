# People Counter App

## Start here!
- [ ] Download the Keepass database and ask the team lead for the password
- [ ] Follow the setup instructions to setup Amplify and Android Studio
- [ ] Verify that you can run the app in an emulator or Android device
- [ ] Add yourself to the [Kanban board](https://trello.com/invite/b/1864iZjN/ATTIc5b18ffcf89125d7957e20f84d16dac89FB0D3D5/development-stories)

## Quick links
- [Kanban board](https://trello.com/b/1864iZjN/development-stories)

## Setup
**Note**: These instructions require access to a Keepass
database containing credentials for the People Counter project. For more information, contact Amy Truong at [amytruong@gatech.edu](mailto:amytruong@gatech.edu).

### Installing Android Studio
1. Install Android Studio using [this guide](https://developer.android.com/studio/install).
1. Open Android Studio. Click on `More Actions` or `⋮` and then `Virtual Device Manager`.
2. Create a new device. Select `Pixel 3` for the device definition and `R` for the system image.

### Installing Amplify CLI

1. Install the Amplify CLI using [this guide](https://docs.amplify.aws/cli/start/install/).
2. Run `amplify configure`. Choose `us-east-1` as the AWS region and press `Enter` until you are prompted for an access key ID.
3. Enter the access key ID and secret access key from the `AWS Access Key` entry in the Keepass database. The access key ID is the username and the secret access key is the password.
4. Keep the profile name as `default` unless you already have a `default` profile that you don't want to override.
```
> amplify configure
Follow these steps to set up access to your AWS account:

Sign in to your AWS administrator account:
https://console.aws.amazon.com/
Press Enter to continue

Specify the AWS Region
? region:  us-east-1
Follow the instructions at
https://docs.amplify.aws/cli/start/install/#configure-the-amplify-cli

to complete the user creation in the AWS console
https://console.aws.amazon.com/iamv2/home#/users/create
Press Enter to continue

Enter the access key of the newly created user:
? accessKeyId:  <ACCESS KEY ID>
? secretAccessKey:  <SECRET ACCESS KEY>
This would update/create the AWS Profile in your local machine
? Profile Name:  default

Successfully set up the new user.
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
4. When prompted for a profile to use, choose `default` or the name of the profile you created previously.
```
? Please choose the profile you want to use: default
⚠️ Failed to resolve AppId. Skipping parameter download.
√ Initialized provider successfully.
✅ Initialized your environment successfully.

Your project has been successfully initialized and connected to the cloud!
```
5. Once the environment has been initialized successfully, open the project in Android Studio. You can now run the app in the emulator or on an Android phone with USB debugging enabled.

## Resources
TODO