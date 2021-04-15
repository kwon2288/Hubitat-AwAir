/*
*  Hubitat Driver for AwAir Elements
*  Licenses CC-0 public domain
*/

metadata {
    definition(name: "Awair Element", namespace: "awair", author: "Digital_BG", importUrl: "https://raw.githubusercontent.com/DigitalBodyGuard/Hubitat-AwAir/master/AwAir_Driver.groovy") {
        capability "Sensor"
        capability "Refresh"
        capability "Polling"
        capability "TemperatureMeasurement"
        capability "CarbonDioxideMeasurement"
        capability "RelativeHumidityMeasurement"

        attribute "pm25", "number"
        attribute "temperature", "number"
        attribute "voc", "number"
        attribute "humidity", "string"
        attribute "airQuality", "number"
        attribute "carbonDioxide", "number"

        attribute "aiq_desc", "ENUM", ["unknown", "poor", "fair", "good"]
        attribute "pm25_desc", "ENUM", ["unknown", "hazardous", "bad", "poor", "fair", "good"]
        attribute "co2_desc", "ENUM", ["unknown", "hazardous", "bad", "poor", "fair", "good"]
        attribute "voc_desc", "ENUM", ["unknown", "hazardous", "bad", "poor", "fair", "good"]
    }

    preferences {
        input("ip", "text", title: "IP Address", description: "IP of Awair Device", required: true, defaultValue: "192.168.1.3")
        input("urlPath", "text", title: "API Path", description: "Path to the Awair Air Data", required: true, defaultValue: "/air-data/latest")

        input name: "pollingInterval", type: "number", title: "Time (seconds) between status checks", defaultValue: 300

        input name: "enableAlerts_pm25", type: "bool", title: "Enable Alerts_pm25", defaultValue: true
        input name: "pm2_5LevelBad", type: "number", title: "Alert Level pm2.5", defaultValue: 40
        input name: "pm2_5LevelGood", type: "number", title: "Reset Alert Level pm2.5", defaultValue: 30

        input name: "enableAlerts_co2", type: "bool", title: "Enable Alerts_co2", defaultValue: true
        input name: "co2LevelBad", type: "number", title: "Alert Level co2", defaultValue: 1000
        input name: "co2LevelGood", type: "number", title: "Reset Alert Level co2", defaultValue: 800

        input name: "enableAlerts_voc", type: "bool", title: "Enable Alerts_voc", defaultValue: true
        input name: "vocLevelBad", type: "number", title: "Alert Level voc", defaultValue: 1000
        input name: "vocLevelGood", type: "number", title: "Reset Alert Level voc", defaultValue: 800

        input name: "enableAlerts_aiq", type: "bool", title: "Enable Alerts_aiq", defaultValue: true
        input name: "aiqLevelBad", type: "number", title: "Alert Level low airQuality", defaultValue: 60
        input name: "aiqLevelGood", type: "number", title: "Reset Alert Level high airQuality", defaultValue: 70

        input name: "txtEnable", type: "bool", title: "Enable descriptionText logging", defaultValue: false
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: false
    }
}

void installed() {
    if (logEnable) {
        log.debug "installed..."
    }

    refresh()
    runIn(2, poll)
}

def logsOff() {
    log.warn "debug logging disabled..."
    device.updateSetting("txtEnable", [value: "false", type: "bool"])
}

def refresh() {
    if (logEnable) {
        log.debug "refreshing"
    }

    fireUpdate("voc", -1, "ppb", "voc is ${-1} ppb")
    fireUpdate("pm25", -1, "ug/m3", "pm25 is ${-1} ug/m3")
    fireUpdate("airQuality", -1, "", "airQuality is ${-1}")
    fireUpdate("temperature", -1, "°${location.temperatureScale}", "Temperature is ${-1}°${location.temperatureScale}")
    fireUpdate("carbonDioxide", -1, "ppm", "carbonDioxide is ${-1} ppm")
    fireUpdate("humidity", -1, "%", "humidity is ${-1}")

    fireUpdate_small("aiq_desc", "unknown")
    fireUpdate_small("voc_desc", "unknown")
    fireUpdate_small("co2_desc", "unknown")
    fireUpdate_small("pm25_desc", "unknown")

    runIn(2, poll)
}

def poll() {
    try {
        def Params = [
                uri        : "http://" + ip,
                path       : urlPath,
                contentType: "application/json"
        ]

        asynchttpGet('receiveData', Params)

        if (logEnable) {
            log.debug "poll state"
        }
    } catch (Exception e) {
        if (logEnable) {
            log.error "error occured calling httpget ${e}"
        } else {
            log.error "error occured calling httpget"
        }
    }

    runIn(pollingInterval, poll)
}

def receiveData(response, data) {
    try {
        if (response.getStatus() == 200 || response.getStatus() == 207) {
            if (logEnable) {
                log.debug "start parsing"
            }

            awairData = parseJson(response.data)

            // VOC
            currVocDesc = getAttribute("voc_desc")
            vocLevel = awairData.voc
            newVocDesc = "unknown"

            fireUpdate("voc", vocLevel, "ppb", "voc is ${vocLevel} ppb")

            // Calculate VOC Descriptive Text
            if (vocLevel > 8332) {
                newVocDesc = "hazardous"
            } else if (vocLevel > 3333) {
                newVocDesc = "bad"
            } else if (vocLevel > 1000) {
                newVocDesc = "poor"
            } else if (vocLevel > 333) {
                newVocDesc = "fair"
            } else {
                newVocDesc = "good"
            }

            if (currVocDesc != newVocDesc) {
                fireUpdate_small("voc_desc", newVocDesc)
            }

            // PM 2.5
            currPm25Desc = getAttribute("alert_pm25")
            pm25Level = awairData.pm25
            newPm25Desc = "unknown"

            fireUpdate("pm25", pm25Level, "ug/m3", "pm25 is ${pm25Level} ug/m3")

            // Calculate PM 2.5 Descriptive Text
            if (pm25Level > 75) {
                newPm25Desc = "hazardous"
            } else if (pm25Level > 55) {
                newPm25Desc = "bad"
            } else if (pm25Level > 35) {
                newPm25Desc = "poor"
            } else if (pm25Level > 15) {
                newPm25Desc = "fair"
            } else {
                newPm25Desc = "good"
            }

            if (currPm25Desc != newPm25Desc) {
                fireUpdate_small("pm25_desc", newPm25Desc)
            }

            // AIQ Score
            currAiqDesc = getAttribute("aiq_desc") // Grab the current descriptive text for the AIQ score
            aiqScore = awairData.score
            newAiqScore = "unknown"

            fireUpdate("airQuality", aiqScore, "", "Awair Score is ${aiqScore}")

            // Calculate the text description for the Score
            if (aiqScore > 80) {
                newAiqScore = "good"
            } else if (aiqScore > 60) {
                newAiqScore = "fair"
            } else {
                newAiqScore = "poor"
            }

            if (currAiqDesc != newAiqScore) {
                fireUpdate_small("aiq_desc", newAiqScore)
            }

            // Temperature
            temperature = convertTemperatureIfNeeded(awairData.temp, "c", 1)
            fireUpdate("temperature", temperature, "°${location.temperatureScale}", "Temperature is ${temperature}°${location.temperatureScale}")

            // CO2
            currCo2Desc = getAttribute("co2_desc")
            co2Level = awairData.co2
            newCo2Desc = "unknown"

            fireUpdate("carbonDioxide", co2Level, "ppm", "carbonDioxide is ${co2Level} ppm")

            // Calculate CO2 Descriptive Text
            if (co2Level > 2500) {
                newCo2Desc = "hazardous"
            } else if (co2Level > 1500) {
                newCo2Desc = "bad"
            } else if (co2Level > 1000) {
                newCo2Desc = "poor"
            } else if (co2Level > 600) {
                newCo2Desc = "fair"
            } else {
                newCo2Desc = "good"
            }

            if (currCo2Desc != newCo2Desc) {
                fireUpdate_small("co2_desc", newCo2Desc)
            }

            // Humidity
            fireUpdate("humidity", (int) awairData.humid, "%", "humidity is ${awairData.humid}")

        } else {
            log.error "parsing error"
        }
    } catch (Exception e) {
        log.error "error #5415 : ${e}"
    }
}

void fireUpdate(name, value, unit, description) {
    result = [
            name           : name,
            value          : value,
            unit           : unit,
            descriptionText: description
    ]

    eventProcess(result)
}

void fireUpdate_small(name, value) {
    result = [
            name : name,
            value: value
    ]
    eventProcess(result)
}

def getAttribute(name) {
    return device.currentValue(name).toString()
}

void eventProcess(Map evt) {
    if (getAttribute(evt.name).toString() != evt.value.toString()) {
        evt.isStateChange = true
        sendEvent(evt)

        if (txtEnable) log.info device.getName() + " " + evt.descriptionText
        if (logEnable) log.debug "result : " + evt
    }
}
