# Stash Webhook for Jenkins

After making commits to Stash, notify Jenkins that a new build has been created.

## Requirements

+  **Git Plugin** - Jenkins needs to have the [Git Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Git+Plugin) installed in Jenkins

## Setup

Once installed, follow these steps:
-  Navigate to a repository in Stash.
-  Hit the *Settings* link
-  In the left-navigation, hit the *Hooks* link
-  For the **Stash Webhook to Jenkins**, click the *Enable* button.
-  Enter the URL to your Jenkins instance
-  Select the method that clone method that Jenkins is using (HTTP or SSH).
-  If using HTTP, enter the username that Jenkins is using to clone your repository.
-  Submit the form.
-  Commit some code and watch it trigger a build!

## Release Notes
### Version 2.0-SNAPSHOT
-  Made setup of the Jenkins repository easier by adding SSH/HTTP quick select
-  Fire notification after pull-requests have been merged
-  Added a plugin logo
-  Added test button to the Hook Configuration display
-  Added ability to not send notifications if commits/merges are made by specified Stash users
-  Requires Stash 2.3.0
-  Renamed plugin to **Stash Webhook for Jenkins**

### Version 1.1
-  Added an option to "Skip SSL Certificate Validation", allowing for plugin to communicate with Jenkins instance using a self-signed cert (setting is on Hooks settings for each project)
-  Handle Jenkins urls with trailing slashes more gracefully (thanks [ellingbo](https://github.com/ellingbo))

### Version 1
-  Initial release