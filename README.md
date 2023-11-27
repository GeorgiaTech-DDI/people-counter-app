# People Counter App
## Setup
### Installing Android Studio
1. Install Android Studio using [this guide](https://developer.android.com/studio/install).
1. Open Android Studio. Click on `More Actions` or `⋮` and then `Virtual Device Manager`.
2. Create a new device. Select `Pixel 3` for the device definition and `R` for the system image.

### Installing Amplify CLI
1. Create an AWS account if you do not have one.
2. Follow the steps in [this guide](https://docs.amplify.aws/android/start/getting-started/installation/) to install Amplify CLI and create a new user.

### Configuring Amplify
1. Clone the repository.
2. Delete the `amplify` folder from  the repository.
3. Run `amplify init` to reinitialize the Amplify project.
```
$ amplify init

? Enter a name for the project
  `PeopleCounter`
? Initialize the project with the above configuration?
  `No`
? Enter a name for the environment
  `dev`
? Choose your default editor:
  `Android Studio`
? Choose the type of app that you're building
  `android`
? Where is your Res directory:
  `app/src/main/res`
? Select the authentication method you want to use:
  `AWS profile`
? Please choose the profile you want to use
  `default`
```
4. Run `amplify add auth` to [set up Amplify Auth](https://docs.amplify.aws/android/build-a-backend/auth/set-up-auth/).
```
$ amplify add auth

? Do you want to use the default authentication and security configuration?
  `Default configuration`
? How do you want users to be able to sign in?
  `Username`
? Do you want to configure advanced settings?
  `No, I am done.`
```
5. Run `amplify update auth` to [allow unauthenticated logins](https://docs.amplify.aws/android/build-a-backend/auth/enable-guest-access/).
```
$ amplify update auth

  What do you want to do?
    `Walkthrough all the auth configurations`
  Select the authentication/authorization services that you want to use:
    `User Sign-Up, Sign-In, connected with AWS IAM controls`
  Allow unauthenticated logins? (Provides scoped down permissions that you can control via AWS IAM)
    `Yes`
  [proceed through the rest of the steps choosing the values you want - default is usually "No"]
```
4. Run `amplify add predictions` to [set up Amplify Predictions](https://docs.amplify.aws/android/build-a-backend/more-features/predictions/label-image/).

```
$ amplify add predictions

? Please select from one of the categories below (Use arrow keys)
  `Identify`
? What would you like to identify? (Use arrow keys)
  `Identify Labels`
? Provide a friendly name for your resource
  `labelObjects`
? Would you like use the default configuration? (Use arrow keys)
  `Default Configuration`
? Who should have access? (Use arrow keys)
  `Auth and Guest users`
```
6. Run `amplify push` to push changes and provision backend resources on AWS.
7. You will  likely get errors. To fix these, find all `parameters.json` files in the `amplify` directory, and add the following to the JSON.
```
"access": "auth",
"type": "ALL"
```
8. Rerun the `amplify push` command.
9. Go to your AWS console, and open `IAM`. Click `Roles` on the sidebar. Find the role that looks similar to `amplify-peoplecounter-dev-170738-unauthRole`. It should start with `amplify`, include the project name, and end with `unauthRole`.
10. Add the `AmazonRekognitionFullAccess` permission policy to this role.

### Configuring Google Cloud Platform
1. Create a Google account if you do not have one.
2. Create a new project in Google Cloud console.
3. Enable `Google Sheets API` for this project by clicking [this link](https://console.cloud.google.com/flows/enableapi?apiid=sheets.googleapis.com).
4. Follow the steps in [this guide](https://developers.google.com/workspace/guides/configure-oauth-consent) to configure the OAuth consent screen. You can skip adding scopes.
5. Go to [the Credentials page](https://console.cloud.google.com/apis/credentials) to authorize credentials for the Android app.
6. Click `Create Credentials` > `OAuth client ID`.
7. Click `Application type` > `Android`.
8. Enter `Android` for the client name.
9. Enter `vip.smart3makerspaces.peoplecounter` for the package name.
10. Follow [this guide](https://support.google.com/cloud/answer/6158849?authuser=5#installedapplications&android) to get the SHA-1 certificate fingerprint.
11. Click `Create` to finish creating the credentials.

**Setup is now complete, and you can run the app within the Android Studio emulator.**

### With KeePass database
**Note**: Ignore this section if you do not have access to the KeePass database used in Fall 2023.
#### Installing Amplify CLI

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

#### Running the app
1. Clone the repository.
2. Run `amplify init` within the new directory.
3. Set the environment name to `dev`, default editor to `Android Studio`, and authentication method to `AWS profile`.
```
> amplify init

? Do you want to use an existing environment? Yes
Choose the environment you would like to use:
❯ dev
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
