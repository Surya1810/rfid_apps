package com.partnership.bjbdocumenttrackerreader.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.partnership.bjbdocumenttrackerreader.data.local.entity.AssetEntity
import com.partnership.bjbdocumenttrackerreader.data.model.AssetStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    @Query(
        """
        UPDATE assets 
        SET isThere = :status 
        WHERE rfid = :rfidNumber AND isThere != :status
    """
    )
    fun updateIsThere(rfidNumber: String, status: Boolean)

    @Query("SELECT * FROM assets ORDER BY noDoc ASC")
    fun getAssetsPaging(): PagingSource<Int, AssetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(assets: List<AssetEntity>)

    @Query("SELECT COUNT(*) FROM assets WHERE isThere = 1")
    fun observeDetectedAssets(): Flow<Int>

    @Query("SELECT rfid FROM assets")
    suspend fun getAllRfidNumber(): List<String>

    @Query("DELETE FROM assets")
    suspend fun deleteAllAssets()

    @Query("SELECT COUNT(*) FROM assets")
    fun observeAllAssets(): Flow<Int>

    @Query("SELECT * FROM assets WHERE rfid = :rfidNumber LIMIT 1")
    suspend fun getAssetByRfid(rfidNumber: String): AssetEntity?

    @Query("SELECT id, isThere FROM assets")
    suspend fun getStockOpnameItems(): List<AssetStatus>

    @Query("SELECT isThere FROM assets WHERE rfid = :epc LIMIT 1")
    fun isAssetThere(epc: String): Boolean
}