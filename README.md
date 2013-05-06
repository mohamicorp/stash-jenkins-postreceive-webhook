# Stash Post-Receive Webhook for Jenkins

After making commits to Stash, notify Jenkins that a new build has been created.

## Requirements

+  **Git Plugin** - Jenkins needs to have the [Git Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Git+Plugin) installed in Jenkins

## Setup

Once installed, follow these steps:
-  Navigate to a repository in Stash.
-  Hit the *Settings* link
-  In the left-navigation, hit the *Hooks* link
-  For the **Stash Post-Receive Webhook to Jenkins**, click the *Enable* button.
-  Enter the URL to your Jenkins instance
-  Enter the URL to the Git repository, as it is configured in Jenkins for your project.  If you're using ssh in Jenkins, use the SSH clone URL here.  Otherwise, use the HTTP URL.
-  Submit the form.
-  Commit some code and watch it trigger a build!

## Release Notes
### Version 1.2-SNAPSHOT
-  Use Stash base url by default and allow override for SSH/HTTPS or custom ports etc.
-  Fire notification after pull-request have been merged
-  Added a plugin logo
-  Requires Stash 2.3.0

### Version 1.1
-  Added an option to "Skip SSL Certificate Validation", allowing for plugin to communicate with Jenkins instance using a self-signed cert (setting is on Hooks settings for each project)
-  Handle Jenkins urls with trailing slashes more gracefully (thanks [ellingbo](https://github.com/ellingbo))

### Version 1
-  Initial release