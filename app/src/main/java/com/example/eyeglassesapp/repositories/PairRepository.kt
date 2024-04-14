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
    suspend fun getPairById(pairId: Int): PairEntity?{
        return pairDao.getPairById(pairId)
    }
}