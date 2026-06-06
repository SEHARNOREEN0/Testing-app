package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class LuminaScreen {
    HOME, PEOPLE, MEMORIES, FACE_SEARCH, AI_REVIEW, SETTINGS
}

enum class SelfieScanState {
    IDLE, ONBOARDING, DETECTING, SCANNING, SEARCHING, FINISHED
}

data class LuminaUiState(
    val photos: List<Photo> = emptyList(),
    val people: List<Person> = emptyList(),
    val clusters: List<FaceCluster> = emptyList(),
    val searchHistory: List<SearchHistory> = emptyList(),
    val currentScreen: LuminaScreen = LuminaScreen.HOME,
    val selectedPersonFilter: Person? = null,
    val searchQuery: String = "",
    val toleranceThreshold: Float = 0.70f,
    val selectedPhotoDetail: Photo? = null,
    val selfieScanState: SelfieScanState = SelfieScanState.IDLE,
    val scanProgress: Float = 0f,
    val matchResults: List<Photo> = emptyList(),
    val lastScannedLabel: String = "Unknown Face",
    val isDemoCameraActive: Boolean = false,
    val namingClusterId: Int? = null,
    val notificationMessage: String? = null,
    val selectedTagFilter: String? = null,
    val selectedYearFilter: String? = null
)

class LuminaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LuminaRepository
    private val _state = MutableStateFlow(LuminaUiState())
    val state: StateFlow<LuminaUiState> = _state.asStateFlow()

    init {
        val database = LuminaDatabase.getDatabase(application, viewModelScope)
        repository = LuminaRepository(database)

        // Read flows from database and combine into state
        viewModelScope.launch {
            combine(
                repository.allPhotos,
                repository.allPeople,
                repository.allClusters,
                repository.searchHistory
            ) { photos, people, clusters, history ->
                _state.update { current ->
                    current.copy(
                        photos = photos,
                        people = people,
                        clusters = clusters,
                        searchHistory = history
                    )
                }
            }.collect()
        }
    }

    fun showToast(msg: String) {
        viewModelScope.launch {
            _state.update { it.copy(notificationMessage = msg) }
            delay(2500)
            _state.update { it.copy(notificationMessage = null) }
        }
    }

    fun navigateTo(screen: LuminaScreen) {
        _state.update { it.copy(currentScreen = screen) }
    }

    fun setPersonFilter(person: Person?) {
        _state.update { current ->
            if (current.selectedPersonFilter?.id == person?.id) {
                current.copy(selectedPersonFilter = null)
            } else {
                current.copy(selectedPersonFilter = person)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun setTagFilter(tag: String?) {
        _state.update { current ->
            if (current.selectedTagFilter == tag) {
                current.copy(selectedTagFilter = null)
            } else {
                current.copy(selectedTagFilter = tag)
            }
        }
    }

    fun setYearFilter(year: String?) {
        _state.update { current ->
            if (current.selectedYearFilter == year) {
                current.copy(selectedYearFilter = null)
            } else {
                current.copy(selectedYearFilter = year)
            }
        }
    }

    fun setToleranceThreshold(value: Float) {
        _state.update { it.copy(toleranceThreshold = value) }
    }

    fun selectPhotoForDetail(photo: Photo?) {
        _state.update { it.copy(selectedPhotoDetail = photo) }
    }

    fun toggleFavorite(photoId: Int, isCurrentlyFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(photoId, !isCurrentlyFav)
            showToast(if (!isCurrentlyFav) "Added to Favorites" else "Removed from Favorites")
        }
    }

    fun startSelfieSearch() {
        _state.update { it.copy(selfieScanState = SelfieScanState.ONBOARDING, isDemoCameraActive = true) }
    }

    fun cancelSelfieSearch() {
        _state.update { it.copy(selfieScanState = SelfieScanState.IDLE, isDemoCameraActive = false, scanProgress = 0f, matchResults = emptyList()) }
    }

    fun proceedToCapture() {
        viewModelScope.launch {
            _state.update { it.copy(selfieScanState = SelfieScanState.DETECTING, scanProgress = 0.1f) }
            
            // Phase 1: Face Detection (1.5 seconds)
            for (i in 1..15) {
                delay(100)
                _state.update { it.copy(scanProgress = 0.1f + (i * 0.02f)) }
            }
            
            // Phase 2: Embedding Scanning (1.8 seconds)
            _state.update { it.copy(selfieScanState = SelfieScanState.SCANNING, scanProgress = 0.4f) }
            for (i in 1..18) {
                delay(100)
                _state.update { it.copy(scanProgress = 0.4f + (i * 0.02f)) }
            }
            
            // Phase 3: High-Speed DB Query (1.2 seconds)
            _state.update { it.copy(selfieScanState = SelfieScanState.SEARCHING, scanProgress = 0.8f) }
            for (i in 1..12) {
                delay(100)
                _state.update { it.copy(scanProgress = 0.8f + (i * 0.015f)) }
            }

            // Completed! Let's choose a matching persona/person from the database
            val randomPersonIndex = (0..4).random()
            val availablePeople = _state.value.people
            val matchedPerson = if (availablePeople.isNotEmpty()) availablePeople[randomPersonIndex % availablePeople.size] else null
            
            val matchingPhotos = if (matchedPerson != null) {
                _state.value.photos.filter { it.detectedPeople.contains(matchedPerson.name) }
            } else {
                _state.value.photos.take(3)
            }

            val labelName = matchedPerson?.name ?: "Guest Match"
            val randomAccuracy = (940..998).random() / 1000f

            // Insert into search history
            repository.insertHistory(
                SearchHistory(
                    timestamp = System.currentTimeMillis(),
                    resultsCount = matchingPhotos.size,
                    confidence = randomAccuracy,
                    brushIndex = (0..8).random(),
                    labelName = labelName
                )
            )

            _state.update { current ->
                current.copy(
                    selfieScanState = SelfieScanState.FINISHED,
                    scanProgress = 1.0f,
                    matchResults = matchingPhotos,
                    lastScannedLabel = labelName,
                    isDemoCameraActive = false
                )
            }
            showToast("Face Search Complete: Found ${matchingPhotos.size} memories!")
        }
    }

    // AI Review: Naming face cluster
    fun startNamingCluster(clusterId: Int) {
        _state.update { it.copy(namingClusterId = clusterId) }
    }

    fun submitClusterName(clusterId: Int, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val clusters = _state.value.clusters
            val targetCluster = clusters.find { it.id == clusterId } ?: return@launch

            // 1. Insert a new person record
            val brushIdx = (0..5).random()
            val newPerson = Person(
                name = name,
                relation = "Family Member",
                photoCount = targetCluster.size,
                lastSeen = "Just now",
                confidence = targetCluster.confidence,
                yearsActive = "2026",
                eventsActive = 3,
                soloCount = targetCluster.size / 3,
                groupCount = targetCluster.size - (targetCluster.size / 3),
                faceBrushIndex = brushIdx
            )
            repository.insertPerson(newPerson)

            // 2. Generate custom matching photos for this newly named person so they immediately see items in the photo grid
            val baseTime = System.currentTimeMillis()
            val generatedPhotos = listOf(
                Photo(
                    title = "Afternoon coffee with $name",
                    date = "2026-05-18",
                    event = "Spontaneous Cafe Visit",
                    location = "Oakland, CA",
                    confidence = targetCluster.confidence - 0.02f,
                    isFavorite = false,
                    brushIndex = (0..8).random(),
                    detectedPeople = name,
                    tags = "Coffee, Cafe, $name, Smiles, Candid"
                ),
                Photo(
                    title = "$name's backyard welcome toast",
                    date = "2026-06-01",
                    event = "Summer Solstice BBQ",
                    location = "Home Backyard",
                    confidence = targetCluster.confidence,
                    isFavorite = true,
                    brushIndex = (0..8).random(),
                    detectedPeople = "$name, Daniel Williams",
                    tags = "Barbecue, Celebrations, Welcoming, $name"
                )
            )
            repository.insertPhotos(generatedPhotos)

            // 3. Delete from face clusters list
            repository.deleteCluster(clusterId)

            _state.update { it.copy(namingClusterId = null) }
            showToast("Successfully trained AI. $name is now registered in your index!")
        }
    }

    fun ignoreCluster(clusterId: Int) {
        viewModelScope.launch {
            repository.deleteCluster(clusterId)
            showToast("Cluster ignored. Face index updated.")
        }
    }

    fun uploadPhotoWithLabels(
        title: String,
        event: String,
        location: String,
        tags: String,
        labeledPeople: List<String>,
        newPeopleToRegister: List<Pair<String, String>> // Name, Relation
    ) {
        viewModelScope.launch {
            // 1. Register any new people in the database first
            for (p in newPeopleToRegister) {
                val exists = _state.value.people.any { it.name.equals(p.first, true) }
                if (!exists) {
                    val brushIdx = (0..5).random()
                    val newPerson = Person(
                        name = p.first,
                        relation = p.second.ifBlank { "Family Member" },
                        photoCount = 1,
                        lastSeen = "Just now",
                        confidence = 0.985f,
                        yearsActive = "2026",
                        eventsActive = 1,
                        soloCount = 0,
                        groupCount = 1,
                        faceBrushIndex = brushIdx
                    )
                    repository.insertPerson(newPerson)
                }
            }

            // 2. Insert the actual Photo
            val brushIdx = (0..8).random()
            val detectedPeopleStr = labeledPeople.filter { it.isNotBlank() }.joinToString(", ")
            val resolvedTags = if (tags.isBlank()) "Uploaded" else tags

            val photo = Photo(
                title = title.ifBlank { "Newly Cataloged Memory" },
                date = "2026-06-06",
                event = event.ifBlank { "Personal Archive" },
                location = location.ifBlank { "Secure Cloud Vault" },
                confidence = 0.992f,
                isFavorite = false,
                brushIndex = brushIdx,
                detectedPeople = detectedPeopleStr,
                tags = resolvedTags
            )
            repository.insertPhoto(photo)
            showToast("Successfully cataloged and secured face index for ${labeledPeople.size} members!")
        }
    }

    fun addManualPhoto(title: String, event: String, location: String, peopleNames: String, tagsCsv: String) {
        viewModelScope.launch {
            val brushIdx = (0..8).random()
            val photo = Photo(
                title = title.ifBlank { "Unidentified Snapshot" },
                date = "2026-06-06",
                event = event.ifBlank { "Uncategorized" },
                location = location.ifBlank { "Unknown Location" },
                confidence = 0.991f,
                isFavorite = false,
                brushIndex = brushIdx,
                detectedPeople = peopleNames,
                tags = tagsCsv
            )
            repository.insertPhoto(photo)
            showToast("Successfully cataloged new memory!")
        }
    }
}
