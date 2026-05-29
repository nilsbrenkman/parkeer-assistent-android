package nl.parkeerassistent.amsterdam.data.repository

import nl.parkeerassistent.amsterdam.data.model.AddVisitorRequest
import nl.parkeerassistent.amsterdam.data.model.Response
import nl.parkeerassistent.amsterdam.data.model.Visitor
import nl.parkeerassistent.amsterdam.data.remote.VisitorApi

interface VisitorRepository {
    suspend fun getVisitors(): List<Visitor>
    suspend fun addVisitor(license: String, name: String): Response
    suspend fun deleteVisitor(id: Long): Response
}

class VisitorRepositoryImpl(private val api: VisitorApi) : VisitorRepository {
    override suspend fun getVisitors(): List<Visitor> = api.getVisitors().visitors
    override suspend fun addVisitor(license: String, name: String): Response =
        api.addVisitor(AddVisitorRequest(license, name))
    override suspend fun deleteVisitor(id: Long): Response = api.deleteVisitor(id)
}
