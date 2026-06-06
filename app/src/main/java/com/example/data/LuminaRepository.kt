package com.example.data

import kotlinx.coroutines.flow.Flow

class LuminaRepository(private val database: LuminaDatabase) {

    val allPhotos: Flow<List<Photo>> = database.photoDao().getAllPhotos()
    val favoritePhotos: Flow<List<Photo>> = database.photoDao().getFavoritePhotos()
    val allPeople: Flow<List<Person>> = database.personDao().getAllPeople()
    val allClusters: Flow<List<FaceCluster>> = database.faceClusterDao().getAllClusters()
    val searchHistory: Flow<List<SearchHistory>> = database.searchHistoryDao().getSearchHistory()

    // Photo actions
    suspend fun insertPhoto(photo: Photo) {
        database.photoDao().insertPhoto(photo)
    }

    suspend fun insertPhotos(photos: List<Photo>) {
        database.photoDao().insertPhotos(photos)
    }

    suspend fun toggleFavorite(photoId: Int, isFavorite: Boolean) {
        database.photoDao().updateFavorite(photoId, isFavorite)
    }

    suspend fun deletePhoto(photoId: Int) {
        database.photoDao().deletePhoto(photoId)
    }

    // People actions
    suspend fun insertPerson(person: Person) {
        database.personDao().insertPerson(person)
    }

    suspend fun deletePerson(personId: Int) {
        database.personDao().deletePerson(personId)
    }

    // Cluster actions
    suspend fun insertCluster(cluster: FaceCluster) {
        database.faceClusterDao().insertCluster(cluster)
    }

    suspend fun deleteCluster(clusterId: Int) {
        database.faceClusterDao().deleteCluster(clusterId)
    }

    // History actions
    suspend fun insertHistory(history: SearchHistory) {
        database.searchHistoryDao().insertHistory(history)
    }

    suspend fun clearHistory() {
        database.searchHistoryDao().clearHistory()
    }
}
