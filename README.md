# TwitchFollowsBot
Bot  was made as a small commercial project, since admins changed the way offers are shown it does not work anymore. 
Bot was made with Java using Selenium WebDriver.
Bot was built to executable jar, compiled to an .exe file using JSmooth and sent to buyers with needed files and proper config.

## Changelog:


### v 1.0
- working version

### v 1.1

- Console does not exist on crash, now prints appropiate message. Useful for bug reports.
- Fixed clicking on "view offers" - bot waited 15 seconds after clicking an offer despite no visibility of the offer.
- Fixed multiple clicks on "view offers" - bot now informs about multiple offers being shown and success of clicking in each one. Bot could've crashed if there was an error clicking one of the offers.
- Twitter refresh improvment - Fixed possibility of bot crash, when Twitter page didn't reload correctly. 
- Added minimal delay between clicking - Rarely bot informed about clicking new offer, but in fact nothing happened.

### v 1.2

- Added browser profile - from now on you don't need to log in after each restart of the bot.
- Added ublock to prevent ads from loading (Twitch ads was playing before stream was loaded)
- Fixed Twitter refresh bug where bot crashed when Twitter could not reload.
- Clicking multiple offers has double check whether all of the offers were clicked
