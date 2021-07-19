# Awair Element Driver for Hubitat

## About
This is a Hubitat driver for the [Awair Element](https://www.getawair.com/products/element)
air quality monitor. It uses the local API available in the device for better reliability and ease of setup.

## Features
This driver supports the following measurements provided by the Awair Element:

- AIQ Score
- Temperature in local degrees
- Relative Humidity in percent
- Carbon Dioxide (CO2) level in ppm
- Chemicals (TVOC) level in ppb
- PM2.5 level in ug/m3

It also supports calculation of the AQI score, based on US EPA levels, with some limitations.

## Driver Installation
Add the contents of the AwAir_Driver.groovy file as a custom driver in Hubitat. This can be done easily with the following steps:

1. Launch the management site of your Hubitat hub.
2. Go into the Drivers Code section and choose the "New Driver" button.
3. Choose "Import" and fill in the URL
   `https://raw.githubusercontent.com/DigitalBodyGuard/Hubitat-AwAir/master/AwAir_Driver.groovy`
4. Choose to save the driver.

## Adding the Device
**Note:** The driver requires knowing the IP of your Awair Element device. It is recommended that you use static DHCP
reservations as a change in IP will require reconfiguring the device.

### Enabling Element Local API
Before adding an Awair Element to Hubitat, you need to enable its local API support. This is on a per-device basis, so
you will need to follow these steps for each Element you want to add.

1. Open the Awair Home application and go to the device you wish to set up in Hubitat.
2. From the device screen, go to the "Awair+" section and then choose "Awair APIs".
3. From the API options, choose "Local API" and then "Enable Local API". There is no indicator that this is enabled
   other than the popup alert when you choose the enable option.

### Creating the Device in Hubitat
This driver doesn't use a companion application, so you will need to create your device manually. This is a
straightforward process that can be done by following these steps:

1. Launch the management site of your Hubitat hub.
2. Go to the devices section and choose the `Add Virtual Device` button.
3. In the `Device Name` field, enter the name you want to give to your Awair device.
4. Leave the Network ID field alone, and change the `Type` to "AwAir".
5. Save the device. This will create the device in Hubitat and allow you to access the device preferences.
6. Configure the `IP Address` preference to the IP of your Awair Element device. The leading "http://" is required, so
   do not remove it.
7. Choose "Save Preferences". You are done. On the next poll of the device, the device states will be updated with their
   actual values.

## Driver Preferences
The driver supports the following custom preferences to configure:

- **IP Address**: The IP address to your Awair Element device. The default value is the example `192.168.1.3`.
  You will want to change this to match the IP of your Element.
- **Path Address**: The path to the JSON-encoded report of air quality. This defaults to `/air-data/latest` and should
  not need to be changed.
- **Time between status checks**: The number of seconds between polls of the Awair device. Setting this too low can
  result in stability issues with Hubitat. The default is 300 seconds (5 minutes).
  
## AQI Limitations
While this driver will calculate an AQI, it has a few important limitations:

- This driver uses the US EPA calculation and scale of 0 - 500 for AQI.
- The AQI provided is based on the PM2.5 values only.
- While AQI is normally a 24-hour calculation, the AQI shown is calculated on an average of the most recent PM2.5 readings.

## License
This driver is released under the Creative Commons CC0 license.
