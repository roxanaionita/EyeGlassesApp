package com.example.eyeglassesapp.repositories

import com.example.eyeglassesapp.dao.PairDao
import com.example.eyeglassesapp.entities.PairEntity

class PairRepository (private val pairDao: PairDao) {

    suspend fun insertPair(pair: PairEntity): Long{
        return pairDao.insertPair(pair)
    }
    suspend fun updatePair(pair: PairEntity){
        pairDao.updatePair(pair)
    }
    suspend fun deletePair(pair: PairEntity){
        pairDao.deletePair(pair)
    }
    suspend fun getAllPairs(): List<PairEntity>{
        return pairDao.getAllPairs()
    }
    suspend fun getPairById(pairId: Long): PairEntity?{
        return pairDao.getPairById(pairId)
    }
    suspend fun getPairByDetails(frameId: Int, lensId: Int, rightDiopter: Double, leftDiopter: Double): PairEntity? {
        return pairDao.getPairByDetails(frameId, lensId, rightDiopter, leftDiopter)
    }
    suspend fun updatePairWithOrder(pairId: Long, orderId: Long, quantity: Int) {
        pairDao.updatePairWithOrder(pairId, orderId, quantity)
    }

    suspend fun getPairsByOrderId(orderId: Int): List<PairEntity> {
        return pairDao.getPairsByOrderId(orderId)
    }
}