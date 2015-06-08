# Stash Webhook for Jenkins

After making commits to Stash, notify Jenkins that a new build has been created.

## Requirements

+  **Git Plugin** - Jenkins needs to have the [Git Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Git+Plugin) installed in Jenkins and the Poll SCM option must be enabled

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

## Troubleshooting

- Check your log file for any exceptions
- Be sure you have polling turned on for your project in Jenkins (just enable it... the schedule can be empty)
- Verify that the URLs for your repository match in Jenkins and the webhook settings.  They MUST be identical.
- Still stuck? Check out the [this wiki page](https://github.com/Nerdwin15/stash-jenkins-postreceive-webhook/wiki/Debug) or open an issue

## Rate the Plugin

If you found this plugin useful, please consider leaving us a rating on our [Atlassian Marketplace listing](https://marketplace.atlassian.com/plugins/com.nerdwin15.stash-stash-webhook-jenkins). Thanks!

## Donate to Development

If you've benefited from this open-source project, please take a moment and donate to its continued maintenance and development. 

Forget buying me a beer (don't drink anyways)... they're going to pay off student loans, save up for a house, college funds, and who knows? maybe a toy or two.

- Square Cash: [Send email to donate@nerdwin15.com](mailto:donate@nerdwin15.com?cc=cash@square.com&subject=$XXX&body=Thanks%20for%20the%20plugin!)
- PayPal: [![Donate](https://www.paypalobjects.com/en_US/i/btn/btn_donate_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=9BZWAS9K95SGQ)

## Release Notes

### Version 2.6.0
- Added ability to not send commit hash in Jenkins notification
- Reverted pull-request notifications back to where it was before

### Version 2.5.1
- Fixed a Javascript error when opening plugin settings
- Added datacenter compatible flag

### Version 2.5.0
- **Git Plugin version 2.0.2 or greater in Jenkins is highly recommended**
- No longer requires polling to be enabled for the git repository in Jenkins setup (REVERTED)
- Added more metadata in webhook notification to Jenkins, including the sha1
- Allows "Trigger Build" button to trigger a build multiple times

Special shoutout to [loa](https://github.com/loa) for his pull request, work, and patience

### Version 2.4.2
- Fixed syntax error in Javascript for the "Trigger Build" button (thanks [dojcsak](https://github.com/dojcsak) for the pull request)

### Version 2.4.1
- Stash 3.0 support

### Version 2.4.0
- Added ability to whitelist or blacklist specific branches for triggering of Jenkins.  (whitelist = branch HAS to be in list to trigger; blacklist = branch is IGNORED if in the list)

### Version 2.3.1
- Move Jenkins event notification off to its own thread pool to prevent Stash's event thread from blocking (thanks [charleso](https://github.com/charleso) for the pull request)
- Fixed compatibility issue with versions of Stash before 2.8 (thanks [xuey90](https://github.com/xuey90) for the pull request)

### Version 2.3.0
- Created more helpful error messages during testing of the webhook on the configuration screen.

### Version 2.2.2
- Fixed NPE that occurs when event has a null user. Was seen using the SVN Mirror plugin.

### Version 2.2.1
- Fixed bug in which notifications no longer were sent when pull request is merged

### Version 2.2
- Added button to Pull Request page to trigger a build. Note that Jenkins will still only build if there is an actual change to build.

### Version 2.1
- Trigger webhooks when pull requests are opened, reopened, or updated (thanks [stupchiy](https://github.com/stupchiy) for the research and [lordmatanza](https://github.com/lordmatanza) for the pull request)

### Version 2.0.1
- Fixed bug in which non-admin users were unable to access webhook settings

### Version 2.0
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
