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
    WHERE LOWER(rfid) = :rfidLower AND isThere != :status
    """
    )
    fun updateIsThere(rfidLower: String, status: Boolean)

    @Query(
        """
    SELECT * FROM assets
    WHERE (:isThere IS NULL OR isThere = :isThere)
    ORDER BY id ASC
"""
    )
    fun getFilteredAssets(
        isThere: Boolean?
    ): PagingSource<Int, AssetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(assets: List<AssetEntity>)

    @Query("SELECT scanCode FROM assets ORDER BY id DESC LIMIT 1")
    suspend fun getScanCode(): String

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

    @Query("SELECT isThere FROM assets WHERE LOWER(rfid) = :epc LIMIT 1")
    fun isAssetThere(epc: String): Boolean
}