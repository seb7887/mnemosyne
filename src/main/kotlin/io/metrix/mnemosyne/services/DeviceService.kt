package io.metrix.mnemosyne.services

import io.metrix.mnemosyne.entities.DeviceEntity
import io.metrix.mnemosyne.repositories.DeviceRepository
import mnemosyne.DeviceResponse
import mnemosyne.DevicesResponse
import mnemosyne.Location
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*

@Service
class DeviceService(private val deviceRepository: DeviceRepository) {
    private val targetNotFound = "Device not found"

    fun createDevice(apiKey: String, deviceType: String, createdBy: UUID): DeviceResponse {
        val newDevice = DeviceEntity(
            apiKey = apiKey,
            type = deviceType,
            createdBy = createdBy,
            updatedBy = createdBy
        )

        deviceRepository.save(newDevice)

        return buildDeviceResponse(newDevice)
    }

    fun findDeviceById(id: UUID): DeviceResponse {
        val device = findById(id) ?: throw Exception(targetNotFound)

        return buildDeviceResponse(device)
    }

    fun findDevicesByNode(nodeId: UUID): DevicesResponse {
        val devices = deviceRepository.findAllByNodeId(nodeId).map {
            buildDeviceResponse(it)
        }

        return buildDevicesResponse(devices)
    }

    fun update(apiKey: String, location: Location, nodeId: UUID, updatedBy: UUID): DeviceResponse {
        val device = deviceRepository.findByApiKey(apiKey) ?: throw Exception(targetNotFound)
        val geometryFactory = GeometryFactory()
        val coordinate = Coordinate(location.longitude, location.latitude)
        val point = geometryFactory.createPoint(coordinate)
        point.srid = 4326

        device.location = point
        device.nodeId = nodeId
        device.updatedBy = updatedBy
        device.updatedAt = OffsetDateTime.now()

        deviceRepository.save(device)

        return buildDeviceResponse(device)
    }

    fun delete(id: UUID) {
        val device = findById(id) ?: throw Exception("Device not found")
        deviceRepository.delete(device)
    }

    private fun findById(id: UUID): DeviceEntity? {
        return deviceRepository.findById(id)
    }

    private fun buildDeviceResponse(device: DeviceEntity): DeviceResponse {
        val location = if (device.location != null) {
            Location.newBuilder()
                .setLatitude(device.location!!.coordinate.y)
                .setLongitude(device.location!!.coordinate.x)
                .build()
        } else {
            Location.newBuilder().build()
        }

        return DeviceResponse.newBuilder()
            .setId(device.id.toString())
            .setDeviceType(device.type)
            .setNodeId(device.nodeId.toString())
            .setLocation(location)
            .setCreatedBy(device.createdBy.toString())
            .setCreatedAt(device.createdAt.toString())
            .setUpdatedBy(device.updatedBy.toString())
            .setUpdatedAt(device.updatedAt.toString())
            .build()
    }

    private fun buildDevicesResponse(devices: List<DeviceResponse>): DevicesResponse {
        return DevicesResponse.newBuilder()
            .addAllDevices(devices)
            .setTotal(devices.count().toLong())
            .build()
    }
}