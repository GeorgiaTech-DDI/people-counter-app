# People Counter App

**[Task Board](https://trello.com/b/FXZQRIFY/people-counter-app)**

# Documentation

## Setting up Android Studio and Android emulator
1. Install Android Studio using [this guide](https://developer.android.com/studio/install).
1. Open Android Studio. Click on `More Actions` or `⋮` and then `Virtual Device Manager`.
2. Create a new device. Select `Pixel 3` for the device definition and `R` for the system image.

## Setting up AWS Amplify
1. Install AWS Amplify using [this guide](https://docs.amplify.aws/lib/project-setup/prereq/q/platform/android/).

2. Initialize an Amplify app in the project directory. Check [this guide](https://docs.amplify.aws/lib/project-setup/create-application/q/platform/android/) for reference.

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

3. Run the following commands to add Authentication resources to the project. Check [this guide](https://docs.amplify.aws/lib/auth/getting-started/q/platform/android/) and [this guide](https://docs.amplify.aws/lib/auth/guest_access/q/platform/android/) for reference.
```
$ amplify add auth

? Do you want to use the default authentication and security configuration?
  `Default configuration`
? How do you want users to be able to sign in?
  `Username`
? Do you want to configure advanced settings?
  `No, I am done.`

$ amplify update auth

  What do you want to do?
    `Walkthrough all the auth configurations`
  Select the authentication/authorization services that you want to use:
    `User Sign-Up, Sign-In, connected with AWS IAM controls`
  Allow unauthenticated logins? (Provides scoped down permissions that you can control via AWS IAM)
    `Yes`
  Do you want to enable 3rd party authentication providers in your identity pool?
    `No`
  Do you want to add User Pool Groups?
    `No`
  Do you want to add an admin queries API?
    `No`
  Multifactor authentication (MFA) user login options:
    `OFF`
  Email based user registration/forgot password:
    `Enabled (Requires per-user email entry at registration)`
  Specify an email verification subject: Your verification code
  Specify an email verification message: Your verification code is {####}
  Do you want to override the default password policy for this User Pool? No
  Specify the app's refresh token expiration period (in days): 30
  Do you want to specify the user attributes this app can read and write? No
  Do you want to enable any of the following capabilities? <PRESS ENTER>
  Do you want to use an OAuth flow?
    `No`
  ? Do you want to configure Lambda Triggers for Cognito? No
  ? Which triggers do you want to enable for Cognito? <PRESS ENTER>
```
4. Run the following commands to add Predictions resources to the project. Check [this guide](https://docs.amplify.aws/lib/predictions/label-image/q/platform/android/) for reference.

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
5. Run `amplify push` to push changes and provision backend resources on AWS.

6. You will most likely get errors. To fix these, find all `parameters.json` files in the `amplify` directory, and add the following to the JSON.
```
"access": "auth",
"type": "ALL"
```

7. Rerun the `amplify push` command.

8. Go to your AWS console, and open `IAM`. Click `Roles` on the sidebar. Find the role that looks similar to `amplify-peoplecounter-dev-170738-unauthRole`. It should start with `amplify`, include the project name, and end with `unauthRole`.

9. Add the `AmazonRekognitionFullAccess` permission policy to this role.

10. You can now run the app within Android Studio with access to AWS Rekognition.

# Resources
TODO