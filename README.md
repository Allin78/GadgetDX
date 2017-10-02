Gadgetbridge
============

Gadgetbridge is an Android (4.4+) application which will allow you to use your
Pebble, Mi Band, Amazfit Bit and HPlus device (and more) without the vendor's closed source application
and without the need to create an account and transmit any of your data to the
vendor's servers.


[Homepage](https://gadgetbridge.org)

[Blog](https://blog.gadgetbridge.org)

[![Donate](https://liberapay.com/assets/widgets/donate.svg)](https://liberapay.com/Gadgetbridge/donate)

[![Build](https://travis-ci.org/Freeyourgadget/Gadgetbridge.svg?branch=master)](https://travis-ci.org/Freeyourgadget/Gadgetbridge)

## Download

[![Gadgetbridge on F-Droid](/Get_it_on_F-Droid.svg.png?raw=true "Download from F-Droid")](https://f-droid.org/repository/browse/?fdid=nodomain.freeyourgadget.gadgetbridge)

[List of changes](https://github.com/Freeyourgadget/Gadgetbridge/blob/master/CHANGELOG.md)

## Supported Devices
* Pebble, Pebble Steel, Pebble Time, Pebble Time Steel, Pebble Time Round [Wiki section about this device](https://github.com/Freeyourgadget/Gadgetbridge/wiki/Pebble)
* Pebble 2 (add the device from within Gadgetbridge!) [Wiki section about pebble](https://github.com/Freeyourgadget/Gadgetbridge/wiki/Pebble), most parts apply to Pebble 2 as well
* Mi Band, Mi Band 1A, Mi Band 1S [Wiki section about this device](https://github.com/Freeyourgadget/Gadgetbridge/wiki/Mi-Band)
* Mi Band 2 [Wiki section about mi band](https://github.com/Freeyourgadget/Gadgetbridge/wiki/Mi-Band), some parts apply to mi band 2 as well
* Amazfit Bip (WIP) [Wiki section about the Amazfit Bip](https://github.com/Freeyourgadget/Gadgetbridge/wiki/Amazfit-Bip)
* HPlus Devices (e.g. ZeBand) [Wiki section about this device](https://github.com/Freeyourgadget/Gadgetbridge/wiki/HPlus)
* Teclast H30 (WIP)
* NO.1 F1 (WIP)
* Liveview
* Vibratissimo (experimental)

## Features

Please see [FEATURES.md](https://github.com/Freeyourgadget/Gadgetbridge/blob/master/FEATURES.md)

## Getting Started (Pebble)

1. Pair your Pebble through the Android's Bluetooth Settings or Gadgetbridge. Pebble 2 MUST be paired though Gadgetbridge (tap on the + in Control Center)
2. Start Gadgetbridge, tap on the device you want to connect to
3. To test, choose "Debug" from the menu and play around

For more information read [this wiki article](https://github.com/Freeyourgadget/Gadgetbridge/wiki/Pebble-Getting-Started) 

## How to use (Mi Band 1+2)

* When starting Gadgetbridge the first time, it will automatically
  attempt to discover and pair your Mi Band. Alternatively you can invoke discovery
  manually via the "+" button. It will ask you for some personal info that appears
  to be needed for proper steps calculation on the band. If you do not provide these,
  some hardcoded default "dummy" values will be used instead. 

  When your Mi Band starts to vibrate and blink during the pairing process,
  tap it quickly a few times in a row to confirm the pairing with the band.

1. Configure other notifications as desired
2. Go back to the "Gadgetbridge" activity
3. Tap the Mi Band item to connect if you're not connected yet
4. To test, chose "Debug" from the menu and play around

**Known Issues:**

* The initial connection to a Mi Band sometimes takes a little patience. Try to connect a few times, wait, 
  and try connecting again. This only happens until you have "bonded" with the Mi Band, i.e. until it 
  knows your MAC address. This behavior may also only occur with older firmware versions.
* If you use other apps like Mi Fit, and "bonding" with Gadgetbridge does not work, please
  try to unpair the band in the other app and try again with Gadgetbridge.
* While all Mi Band devices are supported, some firmware versions might work better than others.
  You can consult the [projects wiki pages](https://github.com/Freeyourgadget/Gadgetbridge/wiki/Mi-Band) 
  to check if your firmware version is fully supported or if an upgrade/downgrade might be beneficial.

## Features (Liveview)

* set time (automatically upon connection)
* display notifications and vibrate

## Authors
### Core Team (in order of first code contribution)

* Andreas Shimokawa
* Carsten Pfeiffer
* Daniele Gobbetti

### Additional device support

* João Paulo Barraca (HPlus)
* Vitaly Svyastyn (NO.1 F1)
* Sami Alaoui (Teclast H30)

## Contribute

Contributions are welcome, be it feedback, bugreports, documentation, translation, research or code. Feel free to work
on any of the open [issues](https://github.com/Freeyourgadget/Gadgetbridge/issues?q=is%3Aopen+is%3Aissue);
just leave a comment that you're working on one to avoid duplicated work.

Translations can be contributed via https://hosted.weblate.org/projects/freeyourgadget/gadgetbridge/

## Do you have further questions or feedback?

Feel free to open an issue on our issue tracker, but please:
- do not use the issue tracker as a forum, do not ask for ETAs and read the issue conversation before posting
- use the search functionality to ensure that your question wasn't already answered. Don't forget to check the **closed** issues as well!
- remember that this is a community project, people are contributing in their free time because they like doing so: don't take the fun away! Be kind and constructive.

## Having problems?

0. Phone crashing during device discovery? Disable Privacy Guard (or similarly named functionality) during discovery.
1. Open Gadgetbridge's settings and check the option to write log files
2. Reproduce the problem you encountered
3. Check the logfile at /sdcard/Android/data/nodomain.freeyourgadget.gadgetbridge/files/gadgetbridge.log
4. File an issue at https://github.com/Freeyourgadget/Gadgetbridge/issues/new and possibly provide the logfile

Alternatively you may use the standard logcat functionality to access the log.

