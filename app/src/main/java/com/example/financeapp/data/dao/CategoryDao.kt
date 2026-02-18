package com.example.financeapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.financeapp.data.entity.CategoryEntity
import com.example.financeapp.data.entity.CategoryType
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteById(categoryId: Long)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY name ASC")
    suspend fun getAllNow(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name ASC")
    fun getByType(type: CategoryType): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
    suspend fun getByIdNow(categoryId: Long): CategoryEntity?

    @Query("SELECT * FROM categories WHERE type = :type AND LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getByNameAndTypeNow(name: String, type: CategoryType): CategoryEntity?

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCount(): Int
}
