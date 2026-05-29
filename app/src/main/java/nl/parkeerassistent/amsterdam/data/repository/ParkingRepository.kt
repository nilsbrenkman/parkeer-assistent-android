package nl.parkeerassistent.amsterdam.data.repository

import nl.parkeerassistent.amsterdam.data.model.AddParkingRequest
import nl.parkeerassistent.amsterdam.data.model.Parking
import nl.parkeerassistent.amsterdam.data.model.ParkingResponse
import nl.parkeerassistent.amsterdam.data.model.Response
import nl.parkeerassistent.amsterdam.data.remote.ParkingApi

interface ParkingRepository {
    suspend fun getParking(): ParkingResponse
    suspend fun addParking(request: AddParkingRequest): Response
    suspend fun stopParking(id: Long): Response
    suspend fun getHistory(): List<Parking>
}

class ParkingRepositoryImpl(private val api: ParkingApi) : ParkingRepository {
    override suspend fun getParking(): ParkingResponse = api.getParking()
    override suspend fun addParking(request: AddParkingRequest): Response = api.addParking(request)
    override suspend fun stopParking(id: Long): Response = api.stopParking(id)
    override suspend fun getHistory(): List<Parking> = api.getHistory().history
}
