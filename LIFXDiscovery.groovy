/**
 *
 * Copyright 2018, 2019 Robert Heyes. All Rights Reserved
 *
 *  This software if free for Private Use. You may use and modify the software without distributing it.
 *  You may not grant a sublicense to modify and distribute this software to third parties.
 *  Software is provided without warranty and your use of it is at your own risk.
 *
 */
import groovy.transform.Field

metadata {
    definition(name: 'LIFX discovery', namespace: 'robheyes', author: 'Robert Alan Heyes') {
//	capability: "Switch"
//        capability "Polling"
        capability "Refresh"
//        command "refresh"
//        command "removeChildren"
    }

    preferences {
        input "logEnable", "bool", title: "Enable debug logging", required: false
        //input "refreshBtn", "button", title: "Refresh"
    }
}
//enum States{ INITIALISING, DISCOVERING, POLLING, OPERATING}

//@Field List<Map> headerDescriptor = getDescriptor('size:2l,misc:2l,source:4l,target:8a,frame_reserved:6a,flags:1,sequence:1,protocol_reserved:8a,type:2l,protocol_reserved2:2')
//@Field List<Map> stateVersionDescriptor = getDescriptor('vendor:4l,product:4l,version:4l')
//@Field String currentState = 'DISCOVERING'
@Field List<Map> devicesFound = []

def updated() {
    log.debug "LIFX updating"
    initialize()
}

def installed() {
    log.debug "LIFX installed"
    initialize()
}

def initialize() {
//    state.sequence = 1
//    state.deviceCount = 0
//    def localIP = getHubIP()

//    log.debug "localIP: ${localIP}"
    refresh()
}

def refresh() {
//    removeChildren()
//    String subnet = parent.getSubnet()
//    if (!subnet) {
//        return
//    }
//    currentState = 'DISCOVERING'
//    1.upto(254) {
//        def packet = makeVersionPacket([0, 0, 0, 0, 0, 0] as byte[])
//        def ipAddress = subnet + it
//        sendPacket(packet.buffer, ipAddress)
//    }


    // maybe change the state to OPERATING after a period?
}

//def removeChildren() {
//    log.debug "Removing child devices"
//    childDevices.each {
//        if (it != null) {
//            deleteChildDevice(it.getDeviceNetworkId())
//        }
//    }
//}

def parse(String description) {
//    logDebug("Description = ${description}")
    Map deviceParams = parseDeviceParameters(description)
    logDebug"Params ${deviceParams}"
//    theClass = deviceParams.ip.getClass()
//    logDebug("ip ${deviceParams.ip} of ${theClass}")
    ip = parent.convertIpLong(deviceParams.ip as String)
    mac = hubitat.helper.HexUtils.hexStringToIntArray(deviceParams.mac)
//	logDebug("Mac: ${mac}")
    def parsed = parent.parseHeader(deviceParams)
    logDebug"Parsed ${parsed}"
//    def theType = lookupMessageType(parsed.type)
//    logDebug("Got message of type ${theType}")
//    def descriptor = responseDescriptor()[parsed.type] ?: ''
    final String payload = deviceParams.payload
    switch (parsed.type) {
        case messageTypes().DEVICE.STATE_VERSION.type:
            parent.createDeviceDefinition(parsed, ip, mac)
            break
        case messageTypes().DEVICE.STATE_LABEL.type:
            def data = parseBytes(lookupDescriptorForDeviceAndType('DEVICE', 'STATE_LABEL'), parsed.remainder as List<Long>)
            logDebug "data = ${data}"
            def devices = devicesFound as LinkedList<Map>
            def device = devices.find { it.ip == ip }
            logDebug "Device is now ${device}"
            device?.label = data.label
            state.devicesFound[ip] = device
            break
        case messageTypes().LIGHT.STATE.type:
            logDebug('looking for descriptor')
            def desc = lookupDescriptorForDeviceAndType('LIGHT', 'STATE')
            logDebug("Descriptor: ${desc}")
            def data = parseBytes(desc, parsed.remainder as List<Long>)
            logDebug("State data: ${data}")
            break
        case messageTypes().DEVICE.STATE_GROUP.type:
            break
        case messageTypes().DEVICE.STATE_LOCATION.type:
            break
        case messageTypes().DEVICE.STATE_WIFI_INFO.type:
            break
        case messageTypes().DEVICE.STATE_INFO.type:
            break
    }
}

private Map parseDeviceParameters(String description) {
    Map deviceParams = new HashMap()
    description.findAll(~/(\w+):(\w+)/) {
        deviceParams.putAt(it[1], it[2])
    }
    deviceParams
}

static Map lookupDeviceAndType(String device, String type) {
    parent.lookupDeviceAndType(device, type)
//    return messageTypes()[device][type]
}

static String lookupDescriptorForDeviceAndType(String device, String type) {
    parent.lookupDescriptorForDeviceAndType(device, type)
//    return lookupDeviceAndType(device, type).descriptor
}

//
//Map lookupMessageType(messageType) {
//    def result = [name: "unknown message type ${messageType}"]
//    messageTypes().each { key, value ->
//        value.each {
//            kind, descriptor ->
//                if (descriptor.type == messageType) {
//                    result = [name: sprintf('%s.%s', [key, kind]), descriptor: responseDescriptor()[type] ?: 'none']
//                }
//        }
//
//    }
//    return result
//}

Map parseHeader(Map deviceParams) {
    parent.parseHeader(deviceParams)
/*
    parseBytes(headerDescriptor, (hubitat.helper.HexUtils.hexStringToIntArray(deviceParams.payload) as List<Long>).each {
        it & 0xff
    })
*/
}

def requestExtraInfo(Map data) {
    def device = data.device
    logDebug("Trying to send a get state to ${device}")
    def packet = makeGetStatePacket()
    //sendPacket(packet.buffer, device.ip)
}

//private void createDeviceDefinition(Map parsed, String ip, int[] mac) {
////            logDebug("It's a state version message")
//    log.debug("Creating device for ip address ${ip} and mac ${mac}")
//    def version = parseBytes(stateVersionDescriptor, parsed.remainder as List<Long>)
////            logDebug("Version = ${version}")
//    def device = parent.deviceVersion(version)
//    device.putAt('ip', ip)
//    device.putAt('mac', mac)
//    logDebug("Device descriptor = ${device}")
//    if (null == getChildDevice(device.ip)) {
//        addChildDevice('robheyes', device.deviceName, device.ip)
//    }
//}


//
//def poll() {
//    logInfo('Polling')
//    def packet = makeGetLabelPacket()
//    logDebug(packet)
//    sendPacket(packet.buffer, "192.168.1.45", true)
//
//    logDebug "Sent packet with sequence ${packet.sequence}"
//}
//

Map<String, Map<String, Map>> messageTypes() {
    parent.messageTypes()
/*    final def color = 'hue:2l,saturation:2l,brightness:2l,kelvin:2l'
    final def types = [
            DEVICE: [
                    GET_SERVICE        : [type: 2, descriptor: ''],
                    STATE_SERVICE      : [type: 3, descriptor: 'service:1;port:4l'],
                    GET_HOST_INFO      : [type: 12, descriptor: ''],
                    STATE_HOST_INFO    : [type: 13, descriptor: 'signal:4l;tx:4l;rx:4l,reservedHost:2l'],
                    GET_HOST_FIRMWARE  : [type: 14, descriptor: ''],
                    STATE_HOST_FIRMWARE: [type: 15, descriptor: 'build:8l;reservedFirmware:8l;version:4l'],
                    GET_WIFI_INFO      : [type: 16, descriptor: ''],
                    STATE_WIFI_INFO    : [type: 17, descriptor: 'signal:4l;tx:4l;rx:4l,reservedWifi:2l'],
                    GET_WIFI_FIRMWARE  : [type: 18, descriptor: ''],
                    STATE_WIFI_FIRMWARE: [type: 19, descriptor: 'build:8l;reservedFirmware:8l;version:4l'],
                    GET_POWER          : [type: 20, descriptor: ''],
                    SET_POWER          : [type: 21, descriptor: 'level:2l'],
                    STATE_POWER        : [type: 22, descriptor: 'level:2l'],
                    GET_LABEL          : [type: 23, descriptor: ''],
                    SET_LABEL          : [type: 24, descriptor: 'label:32s'],
                    STATE_LABEL        : [type: 25, descriptor: 'label:32s'],
                    GET_VERSION        : [type: 32, descriptor: ''],
                    STATE_VERSION      : [type: 33, descriptor: 'vendor:4l;product:4l;version:4l'],
                    GET_INFO           : [type: 34, descriptor: ''],
                    STATE_INFO         : [type: 35, descriptor: 'time:8l;uptime:8l;downtime:8l'],
                    ACKNOWLEDGEMENT    : [type: 45, descriptor: ''],
                    GET_LOCATION       : [type: 48, descriptor: ''],
                    SET_LOCATION       : [type: 49, descriptor: 'location:16a;label:32s;updated_at:8l'],
                    STATE_LOCATION     : [type: 50, descriptor: 'location:16a;label:32s;updated_at:8l'],
                    GET_GROUP          : [type: 51, descriptor: ''],
                    SET_GROUP          : [type: 52, descriptor: 'group:16a;label:32s;updated_at:8l'],
                    STATE_GROUP        : [type: 53, descriptor: 'group:16a;label:32s;updated_at:8l'],
                    ECHO_REQUEST       : [type: 58, descriptor: 'payload:64a'],
                    ECHO_RESPONSE      : [type: 59, descriptor: 'payload:64a'],
            ],
            LIGHT : [
                    GET_STATE            : [type: 101, descriptor: ''],
                    SET_COLOR            : [type: 102, descriptor: "reservedColor:1;${color};duration:4l"],
                    SET_WAVEFORM         : [type: 103, descriptor: "reservedWaveform:1;transient:1;${color};period:4l;cycles:4l;skew_ratio:2l;waveform:1"],
                    SET_WAVEFORM_OPTIONAL: [type: 119, descriptor: "reservedWaveform:1;transient:1;${color};period:4l;cycles:4l;skew_ratio:2l;waveform:1;set_hue:1;set_saturation:1;set_brightness:1;set_kelvin:1"],
                    STATE                : [type: 107, descriptor: "${color};reserved1State:2l;power:2l;label:32s;reserved2state:8l"],
                    GET_POWER            : [type: 116, descriptor: ''],
                    SET_POWER            : [type: 117, descriptor: 'level:2l;duration:4l'],
                    STATE_POWER          : [type: 118, descriptor: 'level:2l'],
                    GET_INFRARED         : [type: 120, descriptor: ''],
                    STATE_INFRARED       : [type: 121, descriptor: 'brightness:2l'],
                    SET_INFRARED         : [type: 122, descriptor: 'brightness:2l'],
            ]
    ]
    return types*/
}

/*
private def static deviceVersion(Map device) {
    switch (device.product) {
        case 1:
            return [
                    name      : 'Original 1000',
                    deviceName: 'LIFX Color',
                    features  : [color: true, infrared: false, multizone: false, temperature_range: [min: 2500, max: 9000], chain: false]
            ]
        case 3:
            return [
                    name      : 'Color 650',
                    deviceName: 'LIFX Color',
                    features  : [color: true, infrared: false, multizone: false, temperature_range: [min: 2500, max: 9000], chain: false]
            ]
        case 10:
            return [
                    name      : 'White 800 (Low Voltage)',
                    deviceName: 'LIFX White',
                    features  : [color: false, infrared: false, multizone: false, temperature_range: [min: 2700, max: 6500], chain: false]
            ]
        case 11:
            return [
                    name      : 'White 800 (High Voltage)',
                    deviceName: 'LIFX White',
                    features  : [color: false, infrared: false, multizone: false, temperature_range: [min: 2700, max: 6500], chain: false]
            ]
        case 18:
            return [
                    name      : 'White 900 BR30 (Low Voltage)',
                    deviceName: 'LIFX White',
                    features  : [color: false, infrared: false, multizone: false, temperature_range: [min: 2700, max: 6500], chain: false]
            ]
        case 20:
            return [
                    name      : 'Color 1000 BR30',
                    deviceName: 'LIFX Color',
                    features  : [color: true, infrared: false, multizone: false, temperature_range: [min: 2500, max: 9000], chain: false]
            ]
        case 22:
            return [
                    name      : 'Color 1000',
                    deviceName: 'LIFX Color',
                    features  : [color: true, infrared: false, multizone: false, temperature_range: [min: 2500, max: 9000], chain: false]
            ]
        case 27:
        case 43:
            return [
                    name      : 'LIFX A19',
                    deviceName: 'LIFX Color',
                    features  : [color: true, infrared: false, multizone: false, temperature_range: [min: 2500, max: 9000], chain: false]
            ]
        case 28:
        case 44:
            return [
                    name      : 'LIFX BR30',
                    deviceName: 'LIFX Color',
                    features  : [color: true, infrared: false, multizone: false, temperature_range: [min: 2500, max: 9000], chain: false]
            ]
        case 29:
        case 45:
            return [
                    name      : 'LIFX+ A19',
                    deviceName: 'LIFX+ Color',
                    features  : [color: true, infrared: true, multizone: false, temperature_range: [min: 2500, max: 9000], chain: false]
            ]
        case 30:
        case 46:
            return [
                    name      : 'LIFX+ BR30',
                    deviceName: 'LIFX+ Color',
                    features  : [color: true, infrared: true, multizone: false, temperature_range: [min: 2500, max: 9000], chain: false]
            ]
        case 31:
            return [
                    name      : 'LIFX Z',
                    deviceName: 'LIFX Z',
                    features  : [color: true, infrared: false, multizone: true, temperature_range: [min: 2500, max: 9000], chain: false]
            ]
        case 32:
            return [
                    name      : 'LIFX Z 2',
                    deviceName: 'LIFX Z',
                    features  : [color: true, infrared: false, multizone: true, temperature_range: [min: 2500, max: 9000], chain: false]
            ]
        case 36:
        case 37:
            return [
                    name      : 'LIFX Downlight',
                    deviceName: 'LIFX Color',
                    features  : [color: true, infrared: false, multizone: false, temperature_range: [min: 2500, max: 9000], chain: false]
            ]
        case 38:
        case 56:
            return [
                    name      : 'LIFX Beam',
                    deviceName: 'LIFX Beam',
                    features  : [color: true, infrared: false, multizone: true, temperature_range: [min: 2500, max: 9000], chain: true]
            ]
        case 49:
            return [
                    name      : 'LIFX Mini',
                    deviceName: 'LIFX Color',
                    features  : [color: true, infrared: false, multizone: false, temperature_range: [min: 2500, max: 9000], chain: false]
            ]
        case 50:
        case 60:
            return [
                    name      : 'LIFX Mini Day and Dusk',
                    deviceName: 'LIFX Day and Dusk',
                    features  : [color: false, infrared: false, multizone: false, temperature_range: [min: 1500, max: 4000], chain: true]
            ]
        case 51:
        case 61:
            return [
                    name      : 'LIFX Mini White',
                    deviceName: 'LIFX White Mono',
                    features  : [color: false, infrared: false, multizone: false, temperature_range: [min: 2700, max: 2700], chain: false]
            ]
        case 52:
            return [
                    name      : 'LIFX GU10',
                    deviceName: 'LIFX Color',
                    features  : [color: true, infrared: false, multizone: false, temperature_range: [min: 2500, max: 9000], chain: false]
            ]
        case 55:
            return [
                    name      : 'LIFX Tile',
                    deviceName: 'LIFX Tile',
                    features  : [color: true, infrared: false, multizone: false, temperature_range: [min: 2500, max: 9000], chain: true]
            ]

        case 59:
            return [
                    name      : 'LIFX Mini Color',
                    deviceName: 'LIFX Color',
                    features  : [color: true, infrared: false, multizone: false, temperature_range: [min: 2500, max: 9000], chain: true]
            ]
        default:
            return [name: "Unknown LIFX device with product id ${device.product}"]
    }
}
*/

Map parseBytes(String descriptor, List<Long> bytes) {
    parent.parseBytes(descriptor, bytes)
/*
    log.debug("Looking for descriptor for ${descriptor}")
    def realDescriptor = getDescriptor(descriptor)
    return parseBytes(realDescriptor, bytes)
*/
}

Map parseBytes(List<Map> descriptor, List<Long> bytes) {
    parent.parseBytes(descriptor, bytes)
/*    Map result = new HashMap();
    int offset = 0
    descriptor.each { item ->
        int nextOffset = offset + (item.bytes as int)

        List<Long> data = bytes.subList(offset, nextOffset)
        assert (data.size() == item.bytes as int)
        offset = nextOffset
        // assume big endian for now
        if ('A' == item.endian) {
            result.put(item.name, data)
            return result
        }
        if ('S' == item.endian) {
            result.put(item.name, new String((data.findAll { it != 0 }) as byte[]))
            return result
        }
        if ('B' != item.endian) {
            data = data.reverse()
        }

        BigInteger value = 0
        data.each { value = (value * 256) + it }
        switch (item.bytes) {
            case 1:
                result.put(item.name, (value & 0xFF) as byte)
                break
            case 2:
                result.put(item.name, (value & 0xFFFF) as short)
                break
            case 3: case 4:
                result.put(item.name, (value & 0xFFFFFFFF) as int)
                break
            default: // this should complain if longer than 8 bytes
                result.put(item.name, (value & 0xFFFFFFFFFFFFFFFF) as long)
        }
    }
    if (offset < bytes.size()) {
        result.put('remainder', bytes[offset..-1])
    }
    return result*/
}

//@Field Map<String, List<Map>> cachedDescriptors

List<Map> getDescriptor(String desc) {
    parent.getDescriptor(desc)
/*
    if (null == cachedDescriptors) {
        cachedDescriptors = new HashMap<String, List<Map>>()
    }
    List<Map> candidate = cachedDescriptors.get(desc)
    if (candidate) {
        logDebug('Found candidate')
    } else {
        candidate = makeDescriptor(desc)
        cachedDescriptors[desc] = (candidate)
    }
    candidate
*/
}

/*
private static List<Map> makeDescriptor(String desc) {
    desc.findAll(~/(\w+):(\d+)([aAbBlLsS]?)/) {
        full ->
            [
                    endian: full[3].toUpperCase(),
                    bytes : full[2],
                    name  : full[1],
            ]
    }
}

private String getSubnet() {
    def ip = getHubIP()
    def m = ip =~ /^(\d{1,3}\.\d{1,3}\.\d{1,3}\.)\d{1,3}/
    if (!m) {
        logWarn('ip does not match pattern')
        return null
    }
    return m.group(1)
}
*/

/*private static Long makeTarget(List macAddress) {
    return macAddress.inject(0L) { Long current, Long val -> current * 256 + val }
}*/

// NB this is called by the LIFXMasterApp - do not delete
def sendPacket(List buffer, String ipAddress, boolean wantLog = false) {
    def rawBytes = asByteArray(buffer)
//    logDebug "raw bytes: ${rawBytes}"
    String stringBytes = hubitat.helper.HexUtils.byteArrayToHexString(rawBytes)
    if (wantLog) {
        logDebug "sending bytes: ${stringBytes} to ${ipAddress}"
    }
    sendHubCommand(
            new hubitat.device.HubAction(
                    stringBytes,
                    hubitat.device.Protocol.LAN,
                    [
                            type              : hubitat.device.HubAction.Type.LAN_TYPE_UDPCLIENT,
                            destinationAddress: ipAddress + ":56700",
                            encoding          : hubitat.device.HubAction.Encoding.HEX_STRING
                    ]
            )
    )
}

/*
private Map makeGetDevicePacket() {
    def buffer = []
    def getServiceSequence = parent.makePacket(buffer, [0, 0, 0, 0, 0, 0] as byte[], getTypeFor('DEVICE', 'GET_SERVICE'), false, true, [])
    return [sequence: getServiceSequence, buffer: buffer]
}
*/

private static Integer getTypeFor(String dev, String act) {
    parent.getTypeFor(dev, act)
//    lookupDeviceAndType(dev, act).type as Integer
}
/*

private Map makeEchoPacket(byte[] target) {
    def payload = []
    fill(payload, 0xAA as byte, 64)
    def buffer = []
    def echoSequence = parent.makePacket(buffer, target, getTypeFor('DEVICE', 'ECHO_REQUEST'), false, false, payload)
    return [sequence: echoSequence, buffer: buffer]
}

private Map makeVersionPacket(byte[] target) {
    def buffer = []
    def echoSequence = parent.makePacket(buffer, target, getTypeFor('DEVICE', 'GET_VERSION'))
    return [sequence: echoSequence, buffer: buffer]
}
*/

Map makeGetLabelPacket() {
    parent.makeGetLabelPacket()
/*
    def buffer = []
    def labelSequence = parent.makePacket(buffer, [0, 0, 0, 0, 0, 0] as byte[], getTypeFor('DEVICE', 'GET_LABEL'))
    return [sequence: labelSequence, buffer: buffer]
*/
}

Map makeGetStatePacket() {
    parent.makeGetStatePacket()
/*
    def buffer = []
    def stateSequence = parent.makePacket(buffer, [0, 0, 0, 0, 0, 0] as byte[], getTypeFor('LIGHT', 'GET_STATE'))
    return [sequence: stateSequence, buffer: buffer]
*/
}

//private String getHubIP() {
//    def hub = location.hubs[0]
//
//    hub.localIP
//}
//
//// fills the buffer with the LIFX packet
//byte makePacket(List buffer, byte[] targetAddress, int messageType, Boolean ackRequired = false, Boolean responseRequired = false, List payload = []) {
//    def lastSequence = sequenceNumber()
//    parent.createFrame(buffer, targetAddress.every { it == 0 })
//    parent.createFrameAddress(buffer, targetAddress, ackRequired, responseRequired, lastSequence)
//    parent.createProtocolHeader(buffer, messageType as short)
//    parent.createPayload(buffer, payload as byte[])
//
//    parent.put(buffer, 0, buffer.size() as short)
//    return lastSequence
//}

//private byte sequenceNumber() {
//    state.sequence = (state.sequence + 1) % 128
//}
//
//private static def createFrame(List buffer, boolean tagged) {
//    parent.add(buffer, 0 as short)
//    parent.add(buffer, 0x00 as byte)
//    parent.add(buffer, (tagged ? 0x34 : 0x14) as byte)
//    parent.add(buffer, parent.lifxSource())
//}

//
//private int lifxSource() {
//    0x48454C44 // = HELD: Hubitat Elevation LIFX Device
//}

//private static def createFrameAddress(List buffer, byte[] target, boolean ackRequired, boolean responseRequired, byte sequenceNumber) {
//    parent.add(buffer, target)
//    parent.add(buffer, 0 as short)
//    parent.fill(buffer, 0 as byte, 6)
//    parent.add(buffer, ((ackRequired ? 0x02 : 0) | (responseRequired ? 0x01 : 0)) as byte)
//    parent.add(buffer, sequenceNumber)
//}
//
//private static def createProtocolHeader(List buffer, short messageType) {
//    parent.fill(buffer, 0 as byte, 8)
//    parent.add(buffer, messageType)
//    parent.add(buffer, 0 as short)
//}
//
//private static def createPayload(List buffer, byte[] payload) {
//    parent.add(buffer, payload)
//}

static byte[] asByteArray(List buffer) {
    (buffer.each { it as byte }) as byte[]
}

//static void add(List buffer, byte value) {
//    buffer.add(Byte.toUnsignedInt(value))
//}
//
//static void put(List buffer, int index, byte value) {
//    buffer.set(index, Byte.toUnsignedInt(value))
//}
//
//static void add(List buffer, short value) {
//    def lower = value & 0xff
//    add(buffer, lower as byte)
//    add(buffer, ((value - lower) >>> 8) as byte)
//}
//
//static void put(List buffer, int index, short value) {
//    def lower = value & 0xff
//    put(buffer, index, lower as byte)
//    put(buffer, index + 1, ((value - lower) >>> 8) as byte)
//}
//
//static void add(List buffer, int value) {
//    def lower = value & 0xffff
//    add(buffer, lower as short)
//    add(buffer, Integer.divideUnsigned(value - lower, 0x10000) as short)
//}
//
//static void add(List buffer, long value) {
//    def lower = value & 0xffffffff
//    add(buffer, lower as int)
//    add(buffer, Long.divideUnsigned(value - lower, 0x100000000) as int)
//}
//
//static void add(List buffer, byte[] values) {
//    for (value in values) {
//        add(buffer, value)
//    }
//}
//
//static void fill(List buffer, byte value, int count) {
//    for (int i = 0; i < count; i++) {
//        add(buffer, value)
//    }
//}

private void logDebug(msg) {
    log.debug(msg)
}

private void logInfo(msg) {
    log.info(msg)
}

private void logWarn(String msg) {
    log.warn(msg)
}